import axios from '../api/axios'
import { cachedApiCall, createCacheKey, apiCache } from '../utils/apiCache'
import {
  buildConcertCreatePayload,
  buildConcertUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const concertService = {
  async getAllConcerts() {
    const cacheKey = createCacheKey('/concerts')
    return cachedApiCall(
      async () => {
        const response = await axios.get('/concerts')
        return response.data.data
      },
      cacheKey,
      2 * 60 * 1000
    )
  },

  async getConcertById(id) {
    const response = await axios.get(`/concerts/${id}`)
    return response.data.data
  },

  async searchConcerts(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/concerts', { params })
    return response.data.data
  },

  async createConcert(concertData) {
    const payload = buildConcertCreatePayload(concertData)
    const response = await axios.post('/concerts', payload)
    apiCache.deletePattern('^/concerts')
    return response.data.data
  },

  async updateConcert(id, concertData) {
    const payload = buildConcertUpdatePayload(concertData)
    const response = await axios.put(`/concerts/${id}`, payload)
    apiCache.deletePattern('^/concerts')
    return response.data.data
  },

  async deleteConcert(id) {
    await axios.delete(`/concerts/${id}`)
    apiCache.deletePattern('^/concerts')
  },

  async moveToPastEvent(id) {
    const response = await axios.put(`/concerts/${id}/move-to-past`)
    apiCache.deletePattern('^/concerts')
    return response.data.data
  },

  async moveToUpcomingEvent(id) {
    const response = await axios.put(`/concerts/${id}/move-to-upcoming`)
    apiCache.deletePattern('^/concerts')
    return response.data.data
  },

  async triggerAutoMove() {
    const response = await axios.put('/concerts/auto-move-past', {
      currentDate: new Date().toISOString().split('T')[0]
    })
    apiCache.deletePattern('^/concerts')
    return response.data.data
  }
}
