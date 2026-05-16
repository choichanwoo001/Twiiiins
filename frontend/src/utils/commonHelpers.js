// 공통 유틸리티 함수들

/**
 * 날짜 포맷팅
 * @param {Date|string} date - 날짜
 * @param {string} format - 포맷 ('date', 'datetime', 'time', 'relative', 'short', 'numeric')
 * @param {string} locale - 로케일 ('ko-KR', 'en-US' 등, 기본값: 'ko-KR')
 * @returns {string} 포맷된 날짜 문자열
 */
export const formatDate = (date, format = 'date', locale = 'ko-KR') => {
  if (!date) return ''

  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const options = {
    date: {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    },
    short: {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    },
    numeric: {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    },
    news: {
      year: '2-digit',
      month: '2-digit',
      day: '2-digit'
    },
    datetime: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    },
    time: {
      hour: '2-digit',
      minute: '2-digit'
    }
  }

  if (format === 'relative') {
    return getRelativeTime(d)
  }

  const formatted = d.toLocaleDateString(locale, options[format] || options.date)

  if (format === 'short' || format === 'numeric') {
    return formatted.replace(/\//g, '.')
  }

  if (format === 'news') {
    const day = String(d.getDate()).padStart(2, '0')
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const year = String(d.getFullYear()).slice(-2)
    return `${day}.${month}.${year}`
  }

  return formatted
}

/**
 * 상대적 시간 표시
 * @param {Date} date - 날짜
 * @returns {string} 상대적 시간 문자열
 */
const getRelativeTime = (date) => {
  const now = new Date()
  const diff = now - date
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (hours < 24) return `${hours}시간 전`
  if (days < 7) return `${days}일 전`

  return formatDate(date, 'date')
}

/**
 * 상대 URL을 절대 URL로 변환
 * @param {string} url - 변환할 URL
 * @returns {string} 절대 URL
 */
export const toAbsoluteUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) {
    return url
  }
  if (import.meta.env.DEV) {
    const API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')
    return `${API_BASE}${url.startsWith('/') ? '' : '/'}${url}`
  }
  return url.startsWith('/') ? url : `/${url}`
}
