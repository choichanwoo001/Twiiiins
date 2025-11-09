import axios from '../api/axios'
import {
  buildVideoCreatePayload,
  buildVideoUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const videoService = {
  async getAllVideos() {
    const response = await axios.get('/media/videos')
    return response.data.data || response.data
  },

  async getVideo(id) {
    const response = await axios.get(`/media/videos/${id}`)
    return response.data.data || response.data
  },

  async createVideo(data) {
    const payload = buildVideoCreatePayload(data)
    const response = await axios.post('/media/videos', payload)
    return response.data.data || response.data
  },

  async updateVideo(id, data) {
    const payload = buildVideoUpdatePayload(data)
    const response = await axios.put(`/media/videos/${id}`, payload)
    return response.data.data || response.data
  },

  async deleteVideo(id) {
    const response = await axios.delete(`/media/videos/${id}`)
    return response.data.data || response.data
  },

  async searchVideos(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/videos', { params })
    return response.data.data || response.data
  }
}