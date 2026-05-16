import axios from '../api/axios'
import {
  buildVideoCreatePayload,
  buildVideoUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const videoService = {
  async getAllVideos() {
    const response = await axios.get('/media/videos')
    return response.data.data
  },

  async createVideo(data) {
    const payload = buildVideoCreatePayload(data)
    const response = await axios.post('/media/videos', payload)
    return response.data.data
  },

  async updateVideo(id, data) {
    const payload = buildVideoUpdatePayload(data)
    const response = await axios.put(`/media/videos/${id}`, payload)
    return response.data.data
  },

  async deleteVideo(id) {
    await axios.delete(`/media/videos/${id}`)
  },

  async searchVideos(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/videos', { params })
    return response.data.data
  }
}