// 이미지 최적화 유틸리티

/**
 * 이미지 URL 생성 및 최적화
 * @param {string} imageUrl - 원본 이미지 URL
 * @param {Object} options - 최적화 옵션
 * @returns {string} 최적화된 이미지 URL
 */
export const getOptimizedImageUrl = (imageUrl, options = {}) => {
  if (!imageUrl) {
    return getPlaceholderImage(options.width, options.height)
  }

  // 이미 전체 URL인 경우
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }

  // 상대 경로인 경우
  // 프로덕션에서는 상대 경로를 그대로 사용 (현재 페이지 도메인 기준)
  // 개발 환경에서만 localhost를 사용
  if (import.meta.env.DEV) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const path = imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`
    return `${baseURL}${path}`
  }
  // 프로덕션: 상대 경로 그대로 반환
  return imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`
}

/**
 * 플레이스홀더 이미지 생성
 * @param {number} width - 이미지 너비
 * @param {number} height - 이미지 높이
 * @returns {string} 플레이스홀더 이미지 URL
 */
export const getPlaceholderImage = (width = 150, height = 150) => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}">
      <rect width="${width}" height="${height}" fill="#ddd"/>
      <text x="50%" y="50%" text-anchor="middle" dy=".3em" fill="#999" font-family="Arial, sans-serif" font-size="14">
        No Image
      </text>
    </svg>
  `
  return `data:image/svg+xml,${encodeURIComponent(svg)}`
}

/**
 * 이미지 로딩 상태 관리
 * @param {Ref} loadingState - 로딩 상태 ref
 * @param {Ref} errorState - 에러 상태 ref
 * @returns {Object} 이미지 로딩 핸들러
 */
export const createImageLoader = (loadingState, errorState) => {
  return {
    onLoad: () => {
      loadingState.value = false
      errorState.value = false
    },
    onError: () => {
      loadingState.value = false
      errorState.value = true
    },
    onLoadStart: () => {
      loadingState.value = true
      errorState.value = false
    }
  }
}

/**
 * 이미지 지연 로딩을 위한 Intersection Observer 설정
 * @param {Element} element - 관찰할 요소
 * @param {Function} callback - 콜백 함수
 * @param {Object} options - 옵션
 */
export const setupLazyLoading = (element, callback, options = {}) => {
  const defaultOptions = {
    root: null,
    rootMargin: '50px',
    threshold: 0.1
  }

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        callback(entry.target)
        observer.unobserve(entry.target)
      }
    })
  }, { ...defaultOptions, ...options })

  observer.observe(element)
  return observer
}

/**
 * 이미지 프리로딩
 * @param {string[]} imageUrls - 프리로드할 이미지 URL 배열
 * @returns {Promise} 프리로드 완료 Promise
 */
export const preloadImages = (imageUrls) => {
  const promises = imageUrls.map(url => {
    return new Promise((resolve, reject) => {
      const img = new Image()
      img.onload = resolve
      img.onerror = reject
      img.src = url
    })
  })

  return Promise.allSettled(promises)
}

/**
 * 이미지 크기 조정을 위한 URL 파라미터 추가
 * @param {string} imageUrl - 원본 이미지 URL
 * @param {Object} resizeOptions - 리사이즈 옵션
 * @returns {string} 리사이즈된 이미지 URL
 */
export const addResizeParams = (imageUrl, resizeOptions = {}) => {
  if (!imageUrl || !resizeOptions.width) return imageUrl

  const url = new URL(imageUrl)
  if (resizeOptions.width) url.searchParams.set('w', resizeOptions.width)
  if (resizeOptions.height) url.searchParams.set('h', resizeOptions.height)
  if (resizeOptions.quality) url.searchParams.set('q', resizeOptions.quality)
  if (resizeOptions.format) url.searchParams.set('f', resizeOptions.format)

  return url.toString()
}
