import axios from '../api/axios'

export const newsService = {
  // 뉴스 목록 조회
  async getAllNews() {
    const response = await axios.get('/media/news')
    return response.data.data || response.data
  },

  // 뉴스 상세 조회
  async getNewsById(id) {
    const response = await axios.get(`/media/news/${id}`)
    return response.data.data || response.data
  },

  // 뉴스 검색
  async searchNews(filters) {
    const params = new URLSearchParams()
    if (filters.title) params.append('title', filters.title)
    if (filters.startDate) params.append('startDate', filters.startDate)
    if (filters.endDate) params.append('endDate', filters.endDate)
    
    const response = await axios.get(`/media/news?${params.toString()}`)
    return response.data.data || response.data
  },

  // 뉴스 생성
  async createNews(newsData) {
    const response = await axios.post('/media/news', newsData)
    return response.data.data || response.data
  },

  // 뉴스 수정
  async updateNews(id, newsData) {
    const response = await axios.put(`/media/news/${id}`, newsData)
    return response.data.data || response.data
  },

  // 뉴스 삭제
  async deleteNews(id) {
    await axios.delete(`/media/news/${id}`)
  }
}
