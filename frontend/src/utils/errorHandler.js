/**
 * 에러 처리 유틸리티 함수들
 */

/**
 * 네트워크 오류의 구체적인 원인을 파악
 * @param {Object} error - 에러 객체
 * @returns {string} 구체적인 에러 원인
 */
function getNetworkErrorDetail(error) {
  // Timeout error
  if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
    return 'Timeout'
  }

  // CORS error
  if (error.message?.includes('CORS') || error.message?.includes('cross-origin')) {
    return 'CORS Policy Violation'
  }

  // Network connection error
  if (error.code === 'ERR_NETWORK' || error.code === 'ECONNREFUSED') {
    return 'Server Connection Failed'
  }

  // DNS error
  if (error.code === 'ENOTFOUND' || error.code === 'EAI_AGAIN') {
    return 'DNS Lookup Failed'
  }

  // Other network errors
  if (error.code) {
    return `Network Error (${error.code})`
  }

  return 'Network Connection Error'
}

/**
 * Convert API error to user-friendly message
 * @param {Object} error - Error object
 * @returns {string} User-friendly error message
 */
export function getErrorMessage(error) {
  if (error?.status && error?.message) {
    return error.message
  }

  if (!error?.response && !error?.code && error?.message) {
    return error.message
  }

  // Network error
  if (!error.response) {
    const errorDetail = getNetworkErrorDetail(error)

    // Timeout specific message
    if (errorDetail === 'Timeout') {
      return 'Request timed out. Please try again.'
    }

    // CORS error
    if (errorDetail === 'CORS Policy Violation') {
      return 'Request blocked by CORS policy.'
    }

    // Server connection failed
    if (errorDetail === 'Server Connection Failed') {
      return 'Cannot connect to server. Please check server status.'
    }

    // DNS error
    if (errorDetail === 'DNS Lookup Failed') {
      return 'Server address not found. Please check network settings.'
    }

    return `A network error occurred. (${errorDetail})`
  }

  const { status, data } = error.response

  // If server provides error message
  if (data && data.error && data.error.message) {
    return data.error.message
  }

  // Default messages by HTTP status code
  switch (status) {
    case 400:
      return 'Bad Request.'
    case 401:
      return 'Login required.'
    case 403:
      return 'Access denied.'
    case 404:
      return 'Requested resource not found.'
    case 422:
      return 'Please check input data.'
    case 429:
      return 'Too many requests. Please try again later.'
    case 500:
      return 'Server error occurred. Please contact administrator.'
    case 502:
      return 'Bad Gateway.'
    case 503:
      return 'Service unavailable. Please try again later.'
    case 504:
      return 'Gateway Timeout.'
    default:
      return `An error occurred. (${status})`
  }
}

/**
 * Error logging
 * @param {Object} error - Error object
 * @param {string} context - Context where error occurred
 */
export function logError(error, context = '') {
  const errorInfo = {
    context,
    timestamp: new Date().toISOString(),
    message: error.message || 'Unknown Error',
    code: error.code,
    status: error.status || error.response?.status,
    url: error.config?.url || error.request?.url,
    method: error.config?.method?.toUpperCase(),
  }

  // Network error
  if (!error.response) {
    const networkDetail = getNetworkErrorDetail(error)
    errorInfo.type = 'NETWORK_ERROR'
    errorInfo.detail = networkDetail
    errorInfo.fullMessage = `[${context}] Network Error - ${networkDetail}: ${error.message || 'Unknown'}`

    console.error(errorInfo.fullMessage, {
      ...errorInfo,
      stack: error.stack,
      originalError: error.originalError || error
    })
    return
  }

  // HTTP response error
  const { status, data } = error.response
  errorInfo.type = 'HTTP_ERROR'
  errorInfo.status = status
  errorInfo.responseData = data

  // If server error message exists
  if (data?.error?.message) {
    errorInfo.serverMessage = data.error.message
    errorInfo.serverCode = data.error.code
  }

  errorInfo.fullMessage = `[${context}] HTTP ${status} Error: ${data?.error?.message || error.message || 'Unknown'}`

  console.error(errorInfo.fullMessage, {
    ...errorInfo,
    requestUrl: error.config?.url,
    requestMethod: error.config?.method?.toUpperCase(),
    requestData: error.config?.data,
    responseHeaders: error.response?.headers,
    originalError: error.originalError || error
  })
}
