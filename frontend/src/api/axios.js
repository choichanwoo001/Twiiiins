import axios from 'axios'
import { useAppStore } from '../stores'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000 // 10초 타임아웃
})

// 요청 인터셉터: JWT 토큰 추가
apiClient.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // multipart/form-data인 경우 Content-Type을 자동으로 설정하지 않음
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 응답 인터셉터: 에러 처리 및 토큰 갱신
apiClient.interceptors.response.use(
  response => {
    // 성공 응답 처리
    return response
  },
  error => {
    const appStore = useAppStore()
    
    // 네트워크 오류 처리
    if (!error.response) {
      appStore.showError('네트워크 연결을 확인해주세요.')
      return Promise.reject({
        code: 'NETWORK_ERROR',
        message: '네트워크 연결을 확인해주세요.',
        originalError: error
      })
    }

    const { status, data } = error.response
    
    // HTTP 상태 코드별 에러 처리
    switch (status) {
      case 401:
        // 인증 실패 - 토큰 제거 및 로그인 페이지로 리다이렉트
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        appStore.showError('로그인이 필요합니다.')
        // Vue Router를 사용하여 로그인 페이지로 리다이렉트
        if (window.location.pathname !== '/admin') {
          window.location.href = '/admin'
        }
        break
        
      case 403:
        // 권한 없음
        appStore.showError('접근 권한이 없습니다.')
        break
        
      case 404:
        // 리소스 없음
        appStore.showError('요청한 리소스를 찾을 수 없습니다.')
        break
        
      case 422:
        // 유효성 검사 실패
        if (data && data.error && data.error.message) {
          appStore.showError(data.error.message)
        } else {
          appStore.showError('입력 데이터를 확인해주세요.')
        }
        break
        
      case 429:
        // 요청 제한 초과
        appStore.showError('요청이 너무 많습니다. 잠시 후 다시 시도해주세요.')
        break
        
      case 500:
        // 서버 내부 오류
        appStore.showError('서버 오류가 발생했습니다. 관리자에게 문의해주세요.')
        break
        
      default:
        // 기타 오류
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

