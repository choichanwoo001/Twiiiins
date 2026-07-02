import axios from '../api/axios'
import { unwrapApiResponse } from './apiResponse'
import {
  buildVideoCreatePayload,
  buildVideoUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const videoService = {
  async getAllVideos() {
    const response = await axios.get('/media/videos')
    return unwrapApiResponse(response)
  },

  async getVideo(id) {
    const response = await axios.get(`/media/videos/${id}`)
    return unwrapApiResponse(response)
  },

  async createVideo(data) {
    const payload = buildVideoCreatePayload(data)
    const response = await axios.post('/media/videos', payload)
    return unwrapApiResponse(response)
  },

  async updateVideo(id, data) {
    const payload = buildVideoUpdatePayload(data)
    const response = await axios.put(`/media/videos/${id}`, payload)
    return unwrapApiResponse(response)
  },

  async deleteVideo(id) {
    const response = await axios.delete(`/media/videos/${id}`)
    return unwrapApiResponse(response)
  },

  async searchVideos(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/videos', { params })
    return unwrapApiResponse(response)
  }
}
