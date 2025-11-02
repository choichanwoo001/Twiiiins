// 공통 유틸리티 함수들

/**
 * 날짜 포맷팅
 * @param {Date|string} date - 날짜
 * @param {string} format - 포맷 ('date', 'datetime', 'time', 'relative')
 * @returns {string} 포맷된 날짜 문자열
 */
export const formatDate = (date, format = 'date') => {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const options = {
    date: {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
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

  return d.toLocaleDateString('ko-KR', options[format] || options.date)
}

/**
 * 상대적 시간 표시
 * @param {Date} date - 날짜
 * @returns {string} 상대적 시간 문자열
 */
export const getRelativeTime = (date) => {
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
 * 문자열 자르기
 * @param {string} str - 원본 문자열
 * @param {number} length - 자를 길이
 * @param {string} suffix - 접미사
 * @returns {string} 잘린 문자열
 */
export const truncateString = (str, length = 50, suffix = '...') => {
  if (!str || str.length <= length) return str
  return str.substring(0, length) + suffix
}

/**
 * 숫자 포맷팅 (천 단위 구분)
 * @param {number} num - 숫자
 * @returns {string} 포맷된 숫자 문자열
 */
export const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  return num.toLocaleString('ko-KR')
}

/**
 * 파일 크기 포맷팅
 * @param {number} bytes - 바이트 크기
 * @returns {string} 포맷된 파일 크기
 */
export const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * URL에서 파일명 추출
 * @param {string} url - URL
 * @returns {string} 파일명
 */
export const getFileNameFromUrl = (url) => {
  if (!url) return ''
  return url.split('/').pop().split('?')[0]
}

/**
 * 파일 확장자 추출
 * @param {string} filename - 파일명
 * @returns {string} 확장자
 */
export const getFileExtension = (filename) => {
  if (!filename) return ''
  return filename.split('.').pop().toLowerCase()
}

/**
 * 이미지 파일 여부 확인
 * @param {string} filename - 파일명
 * @returns {boolean} 이미지 파일 여부
 */
export const isImageFile = (filename) => {
  const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg']
  const extension = getFileExtension(filename)
  return imageExtensions.includes(extension)
}

/**
 * 비디오 파일 여부 확인
 * @param {string} filename - 파일명
 * @returns {boolean} 비디오 파일 여부
 */
export const isVideoFile = (filename) => {
  const videoExtensions = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm']
  const extension = getFileExtension(filename)
  return videoExtensions.includes(extension)
}

/**
 * 디바운스 함수
 * @param {Function} func - 실행할 함수
 * @param {number} delay - 지연 시간 (ms)
 * @returns {Function} 디바운스된 함수
 */
export const debounce = (func, delay) => {
  let timeoutId
  return (...args) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => func.apply(null, args), delay)
  }
}

/**
 * 스로틀 함수
 * @param {Function} func - 실행할 함수
 * @param {number} limit - 제한 시간 (ms)
 * @returns {Function} 스로틀된 함수
 */
export const throttle = (func, limit) => {
  let inThrottle
  return (...args) => {
    if (!inThrottle) {
      func.apply(null, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

/**
 * 객체 깊은 복사
 * @param {any} obj - 복사할 객체
 * @returns {any} 복사된 객체
 */
export const deepClone = (obj) => {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime())
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  if (typeof obj === 'object') {
    const clonedObj = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        clonedObj[key] = deepClone(obj[key])
      }
    }
    return clonedObj
  }
  return obj
}

/**
 * 객체 병합
 * @param {Object} target - 대상 객체
 * @param {...Object} sources - 소스 객체들
 * @returns {Object} 병합된 객체
 */
export const mergeObjects = (target, ...sources) => {
  if (!sources.length) return target
  const source = sources.shift()

  if (isObject(target) && isObject(source)) {
    for (const key in source) {
      if (isObject(source[key])) {
        if (!target[key]) Object.assign(target, { [key]: {} })
        mergeObjects(target[key], source[key])
      } else {
        Object.assign(target, { [key]: source[key] })
      }
    }
  }

  return mergeObjects(target, ...sources)
}

/**
 * 객체 여부 확인
 * @param {any} item - 확인할 항목
 * @returns {boolean} 객체 여부
 */
const isObject = (item) => {
  return item && typeof item === 'object' && !Array.isArray(item)
}

/**
 * 로컬 스토리지 헬퍼
 */
export const storage = {
  set(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      // 로컬 스토리지 저장 실패 (용량 초과 등)
    }
  },

  get(key, defaultValue = null) {
    try {
      const item = localStorage.getItem(key)
      return item ? JSON.parse(item) : defaultValue
    } catch (error) {
      // 로컬 스토리지 읽기 실패
      return defaultValue
    }
  },

  remove(key) {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      // 로컬 스토리지 삭제 실패
    }
  },

  clear() {
    try {
      localStorage.clear()
    } catch (error) {
      // 로컬 스토리지 전체 삭제 실패
    }
  }
}

/**
 * 쿠키 헬퍼
 */
export const cookies = {
  set(name, value, days = 7) {
    const expires = new Date()
    expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000))
    document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/`
  },

  get(name) {
    const nameEQ = name + '='
    const ca = document.cookie.split(';')
    for (let i = 0; i < ca.length; i++) {
      let c = ca[i]
      while (c.charAt(0) === ' ') c = c.substring(1, c.length)
      if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length)
    }
    return null
  },

  remove(name) {
    document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`
  }
}
