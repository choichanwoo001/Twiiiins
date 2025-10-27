import axios from '../api/axios'

export const videoService = {
  async getAllVideos() {
    const response = await axios.get('/api/media/videos')
    return response.data
  },

  async getVideo(id) {
    const response = await axios.get(`/api/media/videos/${id}`)
    return response.data
  },

  async createVideo(data) {
    const response = await axios.post('/api/media/videos', data)
    return response.data
  },

  async updateVideo(id, data) {
    const response = await axios.put(`/api/media/videos/${id}`, data)
    return response.data
  },

  async deleteVideo(id) {
    const response = await axios.delete(`/api/media/videos/${id}`)
    return response.data
  },

  async searchVideos(filters) {
    // 현재는 클라이언트 사이드 필터링
    const allVideos = await this.getAllVideos()
    return allVideos.filter(video => {
      if (filters.title && !video.title.toLowerCase().includes(filters.title.toLowerCase())) {
        return false
      }
      return true
    })
  }
}