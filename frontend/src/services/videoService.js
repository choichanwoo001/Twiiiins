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