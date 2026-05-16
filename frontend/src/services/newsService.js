import axios from '../api/axios'
import {
  buildNewsCreatePayload,
  buildNewsUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const newsService = {
  async getAllNews() {
    const response = await axios.get('/media/news')
    return response.data.data
  },

  async searchNews(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/news', { params })
    return response.data.data
  },

  async createNews(newsData) {
    const payload = buildNewsCreatePayload(newsData)
    const response = await axios.post('/media/news', payload)
    return response.data.data
  },

  async updateNews(id, newsData) {
    const payload = buildNewsUpdatePayload(newsData)
    const response = await axios.put(`/media/news/${id}`, payload)
    return response.data.data
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
    return response.data.data
  }
}
