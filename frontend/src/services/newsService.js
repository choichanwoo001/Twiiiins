import axios from '../api/axios'
import { unwrapApiResponse } from './apiResponse'
import {
  buildNewsCreatePayload,
  buildNewsUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const newsService = {
  async getAllNews() {
    const response = await axios.get('/media/news')
    return unwrapApiResponse(response)
  },

  // 뉴스 상세 조회
  async getNewsById(id) {
    const response = await axios.get(`/media/news/${id}`)
    return unwrapApiResponse(response)
  },

  // 뉴스 검색
  async searchNews(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/news', { params })
    return unwrapApiResponse(response)
  },

  async createNews(newsData) {
    const payload = buildNewsCreatePayload(newsData)
    const response = await axios.post('/media/news', payload)
    return unwrapApiResponse(response)
  },

  async updateNews(id, newsData) {
    const payload = buildNewsUpdatePayload(newsData)
    const response = await axios.put(`/media/news/${id}`, payload)
    return unwrapApiResponse(response)
  },

  async deleteNews(id) {
    await axios.delete(`/media/news/${id}`)
  },

  async uploadNewsImages(newsId, files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    const response = await axios.post(`/media/news/${newsId}/images`, formData)
    return unwrapApiResponse(response)
  }
}
