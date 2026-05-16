import { ref } from 'vue'

// 공통 CRUD 헬퍼 함수들

/**
 * 공통 CRUD 작업을 위한 믹스인
 * @param {Object} service - 서비스 객체
 * @param {string} itemName - 아이템 이름 (에러 메시지용)
 * @returns {Object} CRUD 메서드들
 */
export const createCrudHelpers = (service, itemName) => {
  return {
    /**
     * 목록 로드
     * @param {Ref} items - 아이템 배열 ref
     * @param {Ref} loading - 로딩 상태 ref
     * @param {Ref} error - 에러 상태 ref
     */
    async loadItems(items, loading, error) {
      try {
        loading.value = true
        error.value = null
        const data = await service.getAll()
        items.value = data
      } catch (err) {
        error.value = `${itemName} 목록을 불러오는데 실패했습니다.`
      } finally {
        loading.value = false
      }
    },

    /**
     * 아이템 생성
     * @param {Object} itemData - 생성할 아이템 데이터
     * @param {Ref} items - 아이템 배열 ref
     * @param {Ref} loading - 로딩 상태 ref
     * @param {Ref} error - 에러 상태 ref
     * @returns {Object} 생성된 아이템
     */
    async createItem(itemData, items, loading, error) {
      try {
        loading.value = true
        error.value = null
        const newItem = await service.create(itemData)
        items.value.push(newItem)
        return newItem
      } catch (err) {
        error.value = `${itemName} 생성에 실패했습니다.`
        throw err
      } finally {
        loading.value = false
      }
    },

    /**
     * 아이템 수정
     * @param {number} id - 아이템 ID
     * @param {Object} itemData - 수정할 아이템 데이터
     * @param {Ref} items - 아이템 배열 ref
     * @param {Ref} loading - 로딩 상태 ref
     * @param {Ref} error - 에러 상태 ref
     * @returns {Object} 수정된 아이템
     */
    async updateItem(id, itemData, items, loading, error) {
      try {
        loading.value = true
        error.value = null
        const updatedItem = await service.update(id, itemData)
        const index = items.value.findIndex(item => item.id === id)
        if (index !== -1) {
          items.value[index] = updatedItem
        }
        return updatedItem
      } catch (err) {
        error.value = `${itemName} 수정에 실패했습니다.`
        throw err
      } finally {
        loading.value = false
      }
    },

    /**
     * 아이템 삭제
     * @param {number} id - 아이템 ID
     * @param {Ref} items - 아이템 배열 ref
     * @param {Ref} loading - 로딩 상태 ref
     * @param {Ref} error - 에러 상태 ref
     */
    async deleteItem(id, items, loading, error) {
      try {
        loading.value = true
        error.value = null
        await service.delete(id)
        items.value = items.value.filter(item => item.id !== id)
      } catch (err) {
        error.value = `${itemName} 삭제에 실패했습니다.`
        throw err
      } finally {
        loading.value = false
      }
    },

    /**
     * 아이템 검색
     * @param {Object} filters - 검색 필터
     * @param {Ref} items - 아이템 배열 ref
     * @param {Ref} loading - 로딩 상태 ref
     * @param {Ref} error - 에러 상태 ref
     */
    async searchItems(filters, items, loading, error) {
      try {
        loading.value = true
        error.value = null
        const data = await service.search(filters)
        items.value = data
      } catch (err) {
        error.value = `${itemName} 검색에 실패했습니다.`
      } finally {
        loading.value = false
      }
    }
  }
}

/**
 * 폼 유효성 검사 헬퍼
 * @param {Object} form - 폼 데이터
 * @param {Array} rules - 유효성 검사 규칙
 * @returns {Object} 유효성 검사 결과
 */
export const validateForm = (form, rules) => {
  const errors = {}
  let isValid = true

  for (const [field, rule] of Object.entries(rules)) {
    const value = form[field]
    
    if (rule.required && (!value || value.toString().trim() === '')) {
      errors[field] = `${rule.label || field}은(는) 필수입니다.`
      isValid = false
    } else if (rule.minLength && value && value.length < rule.minLength) {
      errors[field] = `${rule.label || field}은(는) 최소 ${rule.minLength}자 이상이어야 합니다.`
      isValid = false
    } else if (rule.maxLength && value && value.length > rule.maxLength) {
      errors[field] = `${rule.label || field}은(는) 최대 ${rule.maxLength}자까지 입력 가능합니다.`
      isValid = false
    } else if (rule.pattern && value && !rule.pattern.test(value)) {
      errors[field] = rule.message || `${rule.label || field} 형식이 올바르지 않습니다.`
      isValid = false
    } else if (rule.custom && typeof rule.custom === 'function') {
      const customError = rule.custom(value, form)
      if (customError) {
        errors[field] = customError
        isValid = false
      }
    }
  }

  return { errors, isValid }
}

/**
 * 공통 에러 핸들러
 * @param {Error} error - 에러 객체
 * @param {string} defaultMessage - 기본 에러 메시지
 * @returns {string} 사용자 친화적인 에러 메시지
 */
export const handleError = (error, defaultMessage = '오류가 발생했습니다.') => {
  if (error.response) {
    // 서버 응답 에러
    const status = error.response.status
    const message = error.response.data?.message || error.response.data?.error
    
    switch (status) {
      case 400:
        return message || '잘못된 요청입니다.'
      case 401:
        return '인증이 필요합니다.'
      case 403:
        return '권한이 없습니다.'
      case 404:
        return '요청한 리소스를 찾을 수 없습니다.'
      case 500:
        return '서버 오류가 발생했습니다.'
      default:
        return message || defaultMessage
    }
  } else if (error.request) {
    // 네트워크 에러
    return '네트워크 연결을 확인해주세요.'
  } else {
    // 기타 에러
    return error.message || defaultMessage
  }
}

/**
 * 로딩 상태 관리 컴포지션
 * @returns {Object} 로딩 상태 관리 객체
 */
export const useLoadingState = () => {
  const isLoading = ref(false)
  const error = ref(null)

  const setLoading = (loading) => {
    isLoading.value = loading
  }

  const setError = (errorMessage) => {
    error.value = errorMessage
  }

  const clearError = () => {
    error.value = null
  }

  const withLoading = async (asyncFunction) => {
    try {
      setLoading(true)
      clearError()
      return await asyncFunction()
    } catch (err) {
      setError(handleError(err))
      throw err
    } finally {
      setLoading(false)
    }
  }

  return {
    isLoading,
    error,
    setLoading,
    setError,
    clearError,
    withLoading
  }
}
