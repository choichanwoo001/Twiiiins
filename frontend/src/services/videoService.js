import axios from '../api/axios'

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
    const response = await axios.post('/media/videos', data)
    return response.data.data || response.data
  },

  async updateVideo(id, data) {
    const response = await axios.put(`/media/videos/${id}`, data)
    return response.data.data || response.data
  },

  async deleteVideo(id) {
    const response = await axios.delete(`/media/videos/${id}`)
    return response.data.data || response.data
  },

  async searchVideos(filters) {
    const params = new URLSearchParams()
    if (filters.title) params.append('title', filters.title)
    
    const response = await axios.get(`/media/videos?${params.toString()}`)
    return response.data.data || response.data
  }
}