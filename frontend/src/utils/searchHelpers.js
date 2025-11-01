// 검색 필터링 공통 유틸리티

/**
 * 검색 필터를 기반으로 데이터를 필터링하는 함수
 * @param {Array} data - 필터링할 데이터 배열
 * @param {Object} filters - 검색 필터 객체 { key: value }
 * @returns {Array} 필터링된 데이터 배열
 */
export function filterData(data, filters) {
  if (!data || !Array.isArray(data)) {
    return []
  }

  if (!filters || Object.keys(filters).length === 0) {
    return data
  }

  return data.filter(item => {
    // 모든 필터 조건이 일치해야 함 (AND 조건)
    return Object.keys(filters).every(key => {
      const filterValue = filters[key]
      
      // 필터 값이 없으면 해당 필터는 무시
      if (!filterValue || filterValue === '') {
        return true
      }

      // 아이템의 해당 키 값 가져오기
      const itemValue = getNestedValue(item, key)
      
      // 문자열로 변환하여 대소문자 구분 없이 검색
      const searchText = String(filterValue).toLowerCase().trim()
      const itemText = String(itemValue || '').toLowerCase().trim()
      
      // 포함 여부 검사
      return itemText.includes(searchText)
    })
  })
}

/**
 * 중첩된 객체에서 키 경로를 통해 값을 가져오는 함수
 * @param {Object} obj - 대상 객체
 * @param {String} path - 키 경로 (예: 'user.name')
 * @returns {*} 값
 */
function getNestedValue(obj, path) {
  if (!obj || !path) {
    return null
  }

  const keys = path.split('.')
  let value = obj

  for (const key of keys) {
    if (value === null || value === undefined) {
      return null
    }
    value = value[key]
  }

  return value
}

/**
 * 검색 필터 초기화 함수
 * @param {Array} filterConfig - 필터 설정 배열 [{ key: 'title', defaultValue: '' }]
 * @returns {Object} 초기화된 필터 객체
 */
export function createSearchFilters(filterConfig) {
  const filters = {}
  filterConfig.forEach(filter => {
    filters[filter.key] = filter.defaultValue || ''
  })
  return filters
}

/**
 * 검색 필터 리셋 함수
 * @param {Object} filters - 현재 필터 객체
 * @param {Array} filterConfig - 필터 설정 배열
 */
export function resetSearchFilters(filters, filterConfig) {
  if (!filters || !filterConfig) {
    return
  }
  
  filterConfig.forEach(filter => {
    filters[filter.key] = filter.defaultValue || ''
  })
}
