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

  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl
  }

  if (import.meta.env.DEV) {
    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    const path = imageUrl.startsWith('/') ? imageUrl : `/${imageUrl}`
    return `${baseURL}${path}`
  }
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
      <text x="50%" y="50%" text-anchor="middle" dy=".3em" fill="#999" font-family="Jost, sans-serif" font-size="14">
        No Image
      </text>
    </svg>
  `
  return `data:image/svg+xml,${encodeURIComponent(svg)}`
}
