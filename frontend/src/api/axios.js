import axios from 'axios'
import { useAppStore } from '../stores'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 60000 // 60초 타임아웃 (파일 업로드를 위해 증가)
})

// 요청 인터셉터: JWT 토큰 추가
apiClient.interceptors.request.use(
  config => {
    const appStore = useAppStore()
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // multipart/form-data인 경우 Content-Type을 자동으로 설정하지 않음
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
      // 파일 업로드인 경우 타임아웃을 더 길게 설정 (2분)
      config.timeout = 120000
      
      // 업로드 진행률 추적
      appStore.setUploading(true)
      appStore.setUploadProgress(0)
      
      config.onUploadProgress = (progressEvent) => {
        if (progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          appStore.setUploadProgress(progress)
        }
      }
    }
    return config
  },
  error => {
    const appStore = useAppStore()
    appStore.setUploading(false)
    return Promise.reject(error)
  }
)

// 응답 인터셉터: 에러 처리 및 토큰 갱신
apiClient.interceptors.response.use(
  response => {
    const appStore = useAppStore()
    // 업로드 완료 시 상태 초기화
    if (response.config.data instanceof FormData) {
      appStore.setUploading(false)
      appStore.setUploadProgress(100)
      // 잠시 후 프로그레스 바 숨기기
      setTimeout(() => {
        appStore.setUploadProgress(0)
      }, 500)
    }
    // 성공 응답 처리
    return response
  },
  error => {
    const appStore = useAppStore()
    
    // 업로드 중 에러 발생 시 상태 초기화
    if (error.config && error.config.data instanceof FormData) {
      appStore.setUploading(false)
      appStore.setUploadProgress(0)
    }
    
    // 네트워크 오류 처리
    if (!error.response) {
      let errorCode = 'NETWORK_ERROR'
      let errorMessage = '네트워크 연결을 확인해주세요.'
      
      // 타임아웃 오류 구분
      if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
        errorCode = 'TIMEOUT_ERROR'
        errorMessage = '요청 시간이 초과되었습니다. 파일 크기를 확인하거나 다시 시도해주세요.'
      }
      // CORS 오류
      else if (error.message?.includes('CORS') || error.message?.includes('cross-origin')) {
        errorCode = 'CORS_ERROR'
        errorMessage = 'CORS 정책 위반으로 요청이 차단되었습니다.'
      }
      // 서버 연결 실패
      else if (error.code === 'ERR_NETWORK' || error.code === 'ECONNREFUSED') {
        errorCode = 'CONNECTION_REFUSED'
        errorMessage = '서버에 연결할 수 없습니다. 서버 상태를 확인해주세요.'
      }
      // DNS 오류
      else if (error.code === 'ENOTFOUND' || error.code === 'EAI_AGAIN') {
        errorCode = 'DNS_ERROR'
        errorMessage = '서버 주소를 찾을 수 없습니다. 네트워크 설정을 확인해주세요.'
      }
      
      // 상세 로깅
      console.error(`[API 요청 실패] ${errorCode} - URL: ${error.config?.url}, Method: ${error.config?.method?.toUpperCase()}, Message: ${error.message}, Code: ${error.code}`)
      
      appStore.showError(errorMessage)
      return Promise.reject({
        code: errorCode,
        message: errorMessage,
        originalError: error
      })
    }

    const { status, data } = error.response
    
    // 상세 로깅
    const requestInfo = {
      url: error.config?.url,
      method: error.config?.method?.toUpperCase(),
      status: status,
      serverMessage: data?.error?.message,
      serverCode: data?.error?.code
    }
    console.error(`[API 응답 오류] HTTP ${status} - URL: ${requestInfo.url}, Method: ${requestInfo.method}, Server Message: ${requestInfo.serverMessage || '없음'}`)
    
    // HTTP 상태 코드별 에러 처리
    switch (status) {
      case 400:
        // 잘못된 요청
        if (data && data.error && data.error.message) {
          appStore.showError(data.error.message)
        } else {
          appStore.showError('잘못된 요청입니다.')
        }
        break
        
      case 401:
        // 인증 실패 - 토큰 제거 및 로그인 페이지로 리다이렉트
        console.warn('[인증 실패] 토큰이 만료되었거나 유효하지 않습니다.')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        appStore.logout()
        // 관리자 페이지에서만 로그인 페이지로 리다이렉트
        if (window.location.pathname.startsWith('/admin')) {
          window.location.href = '/login'
        }
        // 일반 페이지에서는 에러 메시지만 표시 (페이지 접근은 허용)
        break
        
      case 403:
        // 권한 없음
        console.warn('[권한 없음] 접근 권한이 없습니다.')
        appStore.showError('접근 권한이 없습니다.')
        break
        
      case 404:
        // 리소스 없음
        console.warn(`[리소스 없음] 요청한 리소스를 찾을 수 없습니다: ${requestInfo.url}`)
        appStore.showError('요청한 리소스를 찾을 수 없습니다.')
        break
        
      case 422:
        // 유효성 검사 실패
        console.warn(`[유효성 검사 실패] 입력 데이터 오류: ${data?.error?.message || '없음'}`)
        if (data && data.error && data.error.message) {
          appStore.showError(data.error.message)
        } else {
          appStore.showError('입력 데이터를 확인해주세요.')
        }
        break
        
      case 429:
        // 요청 제한 초과
        console.warn('[요청 제한] 요청이 너무 많습니다.')
        appStore.showError('요청이 너무 많습니다. 잠시 후 다시 시도해주세요.')
        break
        
      case 500:
        // 서버 내부 오류
        console.error(`[서버 오류] 내부 서버 오류 발생: ${data?.error?.message || '없음'}`)
        appStore.showError('서버 오류가 발생했습니다. 관리자에게 문의해주세요.')
        break
        
      case 502:
        // 게이트웨이 오류
        console.error('[게이트웨이 오류] 서버 게이트웨이 오류 발생')
        appStore.showError('서버 게이트웨이 오류가 발생했습니다.')
        break
        
      case 503:
        // 서비스 사용 불가
        console.error('[서비스 사용 불가] 서비스를 사용할 수 없습니다.')
        appStore.showError('서비스를 사용할 수 없습니다. 잠시 후 다시 시도해주세요.')
        break
        
      case 504:
        // 게이트웨이 타임아웃
        console.error('[게이트웨이 타임아웃] 서버 응답 시간 초과')
        appStore.showError('서버 응답 시간이 초과되었습니다.')
        break
        
      default:
        // 기타 오류
        console.error(`[기타 오류] HTTP ${status}: ${data?.error?.message || '알 수 없음'}`)
        if (data && data.error && data.error.message) {
          appStore.showError(data.error.message)
        } else {
          appStore.showError(`오류가 발생했습니다. (${status})`)
        }
    }

    // 표준화된 에러 응답 반환
    return Promise.reject({
      code: data?.error?.code || `HTTP_${status}`,
      message: data?.error?.message || `오류가 발생했습니다. (${status})`,
      status: status,
      originalError: error
    })
  }
)

export default apiClient

