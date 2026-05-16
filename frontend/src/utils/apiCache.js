// API 캐시 유틸리티
class ApiCache {
  constructor(defaultTtl = 5 * 60 * 1000) { // 기본 5분 TTL
    this.cache = new Map()
    this.defaultTtl = defaultTtl
  }

  /**
   * 캐시에서 데이터 조회
   * @param {string} key - 캐시 키
   * @returns {any|null} 캐시된 데이터 또는 null
   */
  get(key) {
    const item = this.cache.get(key)
    if (!item) return null

    // 항목별 TTL 확인
    const ttl = item.ttl ?? this.defaultTtl
    if (Date.now() - item.timestamp > ttl) {
      this.cache.delete(key)
      return null
    }

    return item.data
  }

  /**
   * 캐시에 데이터 저장
   * @param {string} key - 캐시 키
   * @param {any} data - 저장할 데이터
   * @param {number|null} ttl - 항목별 TTL (ms), null이면 기본값 사용
   */
  set(key, data, ttl = null) {
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }

  /**
   * 특정 키 삭제
   * @param {string} key - 삭제할 키
   */
  delete(key) {
    this.cache.delete(key)
  }

  /**
   * 패턴에 맞는 키들 삭제
   * @param {string} pattern - 삭제할 패턴 (정규식)
   */
  deletePattern(pattern) {
    const regex = new RegExp(pattern)
    for (const key of this.cache.keys()) {
      if (regex.test(key)) {
        this.cache.delete(key)
      }
    }
  }

  /**
   * 전체 캐시 클리어
   */
  clear() {
    this.cache.clear()
  }

  /**
   * 캐시 크기 반환
   * @returns {number} 캐시된 항목 수
   */
  size() {
    return this.cache.size
  }
}

// 전역 캐시 인스턴스
export const apiCache = new ApiCache()

// 캐시 키 생성 헬퍼
export const createCacheKey = (endpoint, params = {}) => {
  const sortedParams = Object.keys(params)
    .sort()
    .reduce((result, key) => {
      result[key] = params[key]
      return result
    }, {})
  
  return `${endpoint}:${JSON.stringify(sortedParams)}`
}

// 캐시된 API 호출 래퍼
export const cachedApiCall = async (apiFunction, cacheKey, ttl = null) => {
  const cached = apiCache.get(cacheKey)
  if (cached) {
    return cached
  }

  const result = await apiFunction()
  apiCache.set(cacheKey, result, ttl)

  return result
}
