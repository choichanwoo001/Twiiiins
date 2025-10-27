import axios from '../api/axios'

export const videoService = {
  // 비디오 목록 조회
  async getAllVideos() {
    const response = await axios.get('/api/media/videos')
    return response.data
  },

  // 비디오 상세 조회
  async getVideoById(id) {
    const response = await axios.get(`/api/media/videos/${id}`)
    return response.data
  },

  // 비디오 검색
  async searchVideos(filters) {
    const params = new URLSearchParams()
    if (filters.title) params.append('title', filters.title)
    
    const response = await axios.get(`/api/media/videos?${params.toString()}`)
    return response.data
  },

  // 비디오 생성
  async createVideo(videoData) {
    const response = await axios.post('/api/media/videos', videoData)
    return response.data
  },

  // 비디오 수정
  async updateVideo(id, videoData) {
    const response = await axios.put(`/api/media/videos/${id}`, videoData)
    return response.data
  },

  // 비디오 삭제
  async deleteVideo(id) {
    await axios.delete(`/api/media/videos/${id}`)
  }
}
