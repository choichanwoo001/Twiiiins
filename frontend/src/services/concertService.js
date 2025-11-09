import axios from '../api/axios'
import { cachedApiCall, createCacheKey } from '../utils/apiCache'
import { getErrorMessage, logError } from '../utils/errorHandler'
import {
  buildConcertCreatePayload,
  buildConcertUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const concertService = {
  // 콘서트 목록 조회
  async getAllConcerts() {
    const cacheKey = createCacheKey('/concerts')
    return cachedApiCall(
      async () => {
        try {
          const response = await axios.get('/concerts')
          return response.data.data // 표준화된 응답에서 data 추출
        } catch (error) {
          logError(error, 'getAllConcerts')
          throw new Error(getErrorMessage(error))
        }
      },
      cacheKey,
      2 * 60 * 1000 // 2분 캐시
    )
  },

  // 콘서트 상세 조회
  async getConcertById(id) {
    try {
      const response = await axios.get(`/concerts/${id}`)
      return response.data.data // 표준화된 응답에서 data 추출
    } catch (error) {
      logError(error, 'getConcertById')
      throw new Error(getErrorMessage(error))
    }
  },

  // 콘서트 검색
  async searchConcerts(filters) {
    try {
      const params = sanitizeQueryParams(filters)
      const response = await axios.get('/concerts', { params })
      return response.data.data // 표준화된 응답에서 data 추출
    } catch (error) {
      logError(error, 'searchConcerts')
      throw new Error(getErrorMessage(error))
    }
  },

  // 콘서트 생성
  async createConcert(concertData) {
    try {
      const payload = buildConcertCreatePayload(concertData)
      const response = await axios.post('/concerts', payload)
      // 콘서트 목록 캐시 무효화
      const { apiCache } = await import('../utils/apiCache')
      apiCache.deletePattern('^/concerts')
      return response.data.data // 표준화된 응답에서 data 추출
    } catch (error) {
      logError(error, 'createConcert')
      throw new Error(getErrorMessage(error))
    }
  },

  // 콘서트 수정
  async updateConcert(id, concertData) {
    try {
      const payload = buildConcertUpdatePayload(concertData)
      const response = await axios.put(`/concerts/${id}`, payload)
      // 콘서트 목록 캐시 무효화
      const { apiCache } = await import('../utils/apiCache')
      apiCache.deletePattern('^/concerts')
      return response.data.data // 표준화된 응답에서 data 추출
    } catch (error) {
      logError(error, 'updateConcert')
      throw new Error(getErrorMessage(error))
    }
  },

  // 콘서트 삭제
  async deleteConcert(id) {
    try {
      await axios.delete(`/concerts/${id}`)
      // 콘서트 목록 캐시 무효화
      const { apiCache } = await import('../utils/apiCache')
      apiCache.deletePattern('^/concerts')
    } catch (error) {
      logError(error, 'deleteConcert')
      throw new Error(getErrorMessage(error))
    }
  },

  // Past Event로 이동
  async moveToPastEvent(id) {
    try {
      const response = await axios.put(`/concerts/${id}/move-to-past`)
      const { apiCache } = await import('../utils/apiCache')
      apiCache.deletePattern('^/concerts')
      return response.data.data // 표준화된 응답에서 data 추출
    } catch (error) {
      logError(error, 'moveToPastEvent')
      throw new Error(getErrorMessage(error))
    }
  },

  // Upcoming으로 이동
  async moveToUpcomingEvent(id) {
    try {
      const response = await axios.put(`/concerts/${id}/move-to-upcoming`)
      const { apiCache } = await import('../utils/apiCache')
      apiCache.deletePattern('^/concerts')
      return response.data.data // 표준화된 응답에서 data 추출
    } catch (error) {
      logError(error, 'moveToUpcomingEvent')
      throw new Error(getErrorMessage(error))
    }
  },

  // 자동 이동 실행
  async triggerAutoMove() {
    try {
      const response = await axios.put('/concerts/auto-move-past', {
        currentDate: new Date().toISOString().split('T')[0]
      })
      const { apiCache } = await import('../utils/apiCache')
      apiCache.deletePattern('^/concerts')
      return response.data.data // 표준화된 응답에서 data 추출
    } catch (error) {
      logError(error, 'triggerAutoMove')
      throw new Error(getErrorMessage(error))
    }
  }
}
