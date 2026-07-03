import axios from '../api/axios'
import { cachedApiCall, createCacheKey, apiCache } from '../utils/apiCache'
import { getErrorMessage, logError } from '../utils/errorHandler'
import { unwrapApiResponse } from './apiResponse'
import {
  buildConcertCreatePayload,
  buildConcertUpdatePayload
} from './payloadMappers'

const invalidateConcertCache = () => {
  apiCache.deletePattern('^/concerts')
}

export const concertService = {
  async getAllConcerts() {
    const cacheKey = createCacheKey('/concerts')
    return cachedApiCall(
      async () => {
        try {
          const response = await axios.get('/concerts')
          return unwrapApiResponse(response)
        } catch (error) {
          logError(error, 'getAllConcerts')
          throw new Error(getErrorMessage(error))
        }
      },
      cacheKey,
      2 * 60 * 1000
    )
  },

  // 콘서트 상세 조회
  async getConcertById(id) {
    try {
      const response = await axios.get(`/concerts/${id}`)
      return unwrapApiResponse(response)
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
      return unwrapApiResponse(response)
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
      invalidateConcertCache()
      return unwrapApiResponse(response)
    } catch (error) {
      logError(error, 'createConcert')
      throw new Error(getErrorMessage(error))
    }
  },

  async updateConcert(id, concertData) {
    try {
      const payload = buildConcertUpdatePayload(concertData)
      const response = await axios.put(`/concerts/${id}`, payload)
      // 콘서트 목록 캐시 무효화
      invalidateConcertCache()
      return unwrapApiResponse(response)
    } catch (error) {
      logError(error, 'updateConcert')
      throw new Error(getErrorMessage(error))
    }
  },

  async deleteConcert(id) {
    await axios.delete(`/concerts/${id}`)
    invalidateConcertCache()
  },

  async moveToPastEvent(id) {
    try {
      const response = await axios.put(`/concerts/${id}/move-to-past`)
      invalidateConcertCache()
      return unwrapApiResponse(response)
    } catch (error) {
      logError(error, 'moveToPastEvent')
      throw new Error(getErrorMessage(error))
    }
  },

  async moveToUpcomingEvent(id) {
    try {
      const response = await axios.put(`/concerts/${id}/move-to-upcoming`)
      invalidateConcertCache()
      return unwrapApiResponse(response)
    } catch (error) {
      logError(error, 'moveToUpcomingEvent')
      throw new Error(getErrorMessage(error))
    }
  },

  async triggerAutoMove() {
    try {
      const response = await axios.put('/concerts/auto-move-past', {
        currentDate: new Date().toISOString().split('T')[0]
      })
      invalidateConcertCache()
      return unwrapApiResponse(response)
    } catch (error) {
      logError(error, 'triggerAutoMove')
      throw new Error(getErrorMessage(error))
    }
  }
}
