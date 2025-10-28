/**
 * 에러 처리 유틸리티 함수들
 */

/**
 * API 에러를 사용자 친화적인 메시지로 변환
 * @param {Object} error - 에러 객체
 * @returns {string} 사용자 친화적인 에러 메시지
 */
export function getErrorMessage(error) {
  // 네트워크 오류
  if (!error.response) {
    return '네트워크 연결을 확인해주세요.'
  }

  const { status, data } = error.response

  // 서버에서 제공하는 에러 메시지가 있는 경우
  if (data && data.error && data.error.message) {
    return data.error.message
  }

  // HTTP 상태 코드별 기본 메시지
  switch (status) {
    case 400:
      return '잘못된 요청입니다.'
    case 401:
      return '로그인이 필요합니다.'
    case 403:
      return '접근 권한이 없습니다.'
    case 404:
      return '요청한 리소스를 찾을 수 없습니다.'
    case 422:
      return '입력 데이터를 확인해주세요.'
    case 429:
      return '요청이 너무 많습니다. 잠시 후 다시 시도해주세요.'
    case 500:
      return '서버 오류가 발생했습니다. 관리자에게 문의해주세요.'
    default:
      return `오류가 발생했습니다. (${status})`
  }
}

/**
 * 에러 로깅
 * @param {Object} error - 에러 객체
 * @param {string} context - 에러가 발생한 컨텍스트
 */
export function logError(error, context = '') {
  console.error(`[${context}] 에러 발생:`, {
    message: error.message,
    code: error.code,
    status: error.status,
    stack: error.stack,
    originalError: error.originalError
  })
}

/**
 * 재시도 가능한 에러인지 확인
 * @param {Object} error - 에러 객체
 * @returns {boolean} 재시도 가능 여부
 */
export function isRetryableError(error) {
  if (!error.response) {
    return true // 네트워크 오류는 재시도 가능
  }

  const { status } = error.response
  return status >= 500 || status === 429 // 서버 오류나 요청 제한은 재시도 가능
}

/**
 * 에러 타입별 처리 전략
 * @param {Object} error - 에러 객체
 * @returns {Object} 처리 전략
 */
export function getErrorStrategy(error) {
  if (!error.response) {
    return {
      type: 'network',
      retryable: true,
      userMessage: '네트워크 연결을 확인해주세요.',
      action: 'retry'
    }
  }

  const { status } = error.response

  switch (status) {
    case 401:
      return {
        type: 'auth',
        retryable: false,
        userMessage: '로그인이 필요합니다.',
        action: 'redirect'
      }
    case 403:
      return {
        type: 'permission',
        retryable: false,
        userMessage: '접근 권한이 없습니다.',
        action: 'show'
      }
    case 404:
      return {
        type: 'not_found',
        retryable: false,
        userMessage: '요청한 리소스를 찾을 수 없습니다.',
        action: 'show'
      }
    case 422:
      return {
        type: 'validation',
        retryable: false,
        userMessage: '입력 데이터를 확인해주세요.',
        action: 'show'
      }
    case 429:
      return {
        type: 'rate_limit',
        retryable: true,
        userMessage: '요청이 너무 많습니다. 잠시 후 다시 시도해주세요.',
        action: 'retry'
      }
    case 500:
      return {
        type: 'server',
        retryable: true,
        userMessage: '서버 오류가 발생했습니다. 관리자에게 문의해주세요.',
        action: 'retry'
      }
    default:
      return {
        type: 'unknown',
        retryable: false,
        userMessage: `오류가 발생했습니다. (${status})`,
        action: 'show'
      }
  }
}
