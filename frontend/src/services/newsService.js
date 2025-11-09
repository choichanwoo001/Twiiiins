import axios from '../api/axios'
import {
  buildNewsCreatePayload,
  buildNewsUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

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
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/news', { params })
    return response.data.data || response.data
  },

  // 뉴스 생성
  async createNews(newsData) {
    const payload = buildNewsCreatePayload(newsData)
    const response = await axios.post('/media/news', payload)
    return response.data.data || response.data
  },

  // 뉴스 수정
  async updateNews(id, newsData) {
    const payload = buildNewsUpdatePayload(newsData)
    const response = await axios.put(`/media/news/${id}`, payload)
    return response.data.data || response.data
  },

  // 뉴스 삭제
  async deleteNews(id) {
    await axios.delete(`/media/news/${id}`)
  },

  // 뉴스 사진 업로드
  async uploadNewsImages(newsId, files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })

    const response = await axios.post(`/media/news/${newsId}/images`, formData)
    return response.data.data || response.data
  }
}
