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
      <text x="50%" y="50%" text-anchor="middle" dy=".3em" fill="#999" font-family="Josefin Sans, sans-serif" font-size="14">
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

/**
 * 이미지 행 높이 계산 관련 유틸리티
 */

/**
 * 요소의 flex-basis 값을 추출
 * @param {HTMLElement} imageItem - 이미지 아이템 요소
 * @returns {number} flex-basis 값
 */
export const getFlexBasis = (imageItem) => {
  if (!imageItem) return 1
  const flexValue = window.getComputedStyle(imageItem).flex
  const match = flexValue.match(/^(\d+)/)
  return match ? parseInt(match[1]) : 1
}

/**
 * 행 내 모든 이미지의 flex-basis 총합 계산
 * @param {HTMLElement} row - 행 요소
 * @param {NodeList|Array} images - 이미지 요소들
 * @returns {number} 총 flex-basis 값
 */
export const getTotalFlex = (row, images) => {
  if (!row || !images) return 0
  let total = 0
  images.forEach((img) => {
    const imageItem = img.closest('.image-item')
    if (imageItem) {
      total += getFlexBasis(imageItem)
    }
  })
  return total
}

/**
 * 이미지 행의 높이를 계산하고 설정
 * @param {HTMLElement} row - 행 요소
 * @param {NodeList|Array} images - 이미지 요소들
 * @param {number} rowIndex - 행 인덱스 (0부터 시작)
 */
/**
 * 이미지 행의 높이를 계산하고 각 이미지의 너비를 비율에 맞게 설정
 * 같은 행의 이미지들은 같은 높이를 가지며, 각 이미지의 비율에 따라 너비가 결정됨
 * flex-wrap으로 자동 줄바꿈이 되므로, 각 행을 개별적으로 계산
 * @param {HTMLElement} row - 행 요소
 * @param {NodeList|Array} images - 이미지 요소들
 * @param {number} rowIndex - 행 인덱스 (0부터 시작, 사용하지 않음)
 */
export const calculateRowHeight = (row, images, rowIndex) => {
  if (!row || !images || images.length === 0) return

  const gap = 8 // 0.5rem = 8px
  const rowWidth = row.offsetWidth
  const minHeight = 150 // 최소 높이
  const maxHeight = 300 // 최대 높이 (이미지 축소를 위해)

  // 각 이미지의 비율 정보 수집
  const imageData = []
  images.forEach((img) => {
    if (img.naturalWidth && img.naturalHeight) {
      const imageItem = img.closest('.image-item')
      if (imageItem) {
        const aspectRatio = img.naturalWidth / img.naturalHeight
        imageData.push({
          img,
          aspectRatio,
          imageItem
        })
      }
    }
  })

  if (imageData.length === 0) {
    return
  }

  // flex-wrap으로 자동 줄바꿈이 되므로, 각 행을 개별적으로 계산
  // 이미지들을 실제 위치(top 값)를 기준으로 행으로 그룹화
  const rows = []
  const processed = new Set()

  imageData.forEach((data, index) => {
    if (processed.has(index)) return

    // 현재 이미지의 행 찾기
    const currentTop = data.imageItem.offsetTop
    const currentRow = [data]
    processed.add(index)

    // 같은 행에 있는 다른 이미지들 찾기
    imageData.forEach((otherData, otherIndex) => {
      if (processed.has(otherIndex)) return
      if (Math.abs(otherData.imageItem.offsetTop - currentTop) < 5) {
        currentRow.push(otherData)
        processed.add(otherIndex)
      }
    })

    rows.push(currentRow)
  })

  // 각 행의 높이와 이미지 너비 계산
  rows.forEach((rowImages) => {
    if (rowImages.length === 0) return

    // 행의 모든 이미지가 같은 높이를 가지도록 최적 높이 계산
    const totalAspectRatio = rowImages.reduce((sum, data) => sum + data.aspectRatio, 0)
    const calculatedHeight = (rowWidth - (rowImages.length - 1) * gap) / totalAspectRatio
    // 이미지 축소를 위해 최대 높이 제한, 최소 높이 보장
    const finalHeight = Math.min(Math.max(calculatedHeight, minHeight), maxHeight)

    // 각 이미지 아이템의 너비와 높이를 비율에 맞게 설정
    rowImages.forEach(({ aspectRatio, imageItem }) => {
      const itemWidth = finalHeight * aspectRatio
      imageItem.style.width = `${itemWidth}px`
      imageItem.style.height = `${finalHeight}px`
    })
  })
}