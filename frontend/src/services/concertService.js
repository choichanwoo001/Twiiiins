import axios from '../api/axios'
import { cachedApiCall, createCacheKey } from '../utils/apiCache'

export const concertService = {
  // 콘서트 목록 조회
  async getAllConcerts() {
    const cacheKey = createCacheKey('/api/concerts')
    return cachedApiCall(
      async () => {
        const response = await axios.get('/api/concerts')
        return response.data
      },
      cacheKey,
      2 * 60 * 1000 // 2분 캐시
    )
  },

  // 콘서트 상세 조회
  async getConcertById(id) {
    const response = await axios.get(`/api/concerts/${id}`)
    return response.data
  },

  // 콘서트 검색
  async searchConcerts(filters) {
    const params = new URLSearchParams()
    if (filters.name) params.append('name', filters.name)
    if (filters.location) params.append('location', filters.location)
    if (filters.startDate) params.append('startDate', filters.startDate)
    if (filters.endDate) params.append('endDate', filters.endDate)
    
    const response = await axios.get(`/api/concerts?${params.toString()}`)
    return response.data
  },

  // 콘서트 생성
  async createConcert(concertData) {
    const response = await axios.post('/api/concerts', concertData)
    // 콘서트 목록 캐시 무효화
    const { apiCache } = await import('../utils/apiCache')
    apiCache.deletePattern('^/api/concerts')
    return response.data
  },

  // 콘서트 수정
  async updateConcert(id, concertData) {
    const response = await axios.put(`/api/concerts/${id}`, concertData)
    // 콘서트 목록 캐시 무효화
    const { apiCache } = await import('../utils/apiCache')
    apiCache.deletePattern('^/api/concerts')
    return response.data
  },

  // 콘서트 삭제
  async deleteConcert(id) {
    await axios.delete(`/api/concerts/${id}`)
    // 콘서트 목록 캐시 무효화
    const { apiCache } = await import('../utils/apiCache')
    apiCache.deletePattern('^/api/concerts')
  },

  // Past Event로 이동
  async moveToPastEvent(id) {
    await axios.put(`/api/concerts/${id}/move-past`)
  },

  // Upcoming으로 이동
  async moveToUpcomingEvent(id) {
    await axios.put(`/api/concerts/${id}/move-upcoming`)
  },

  // 자동 이동 실행
  async triggerAutoMove() {
    await axios.post('/api/concerts/auto-move')
  }
}
