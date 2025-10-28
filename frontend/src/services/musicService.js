import axios from '../api/axios'

export const musicService = {
  // 음악 목록 조회
  async getAllMusic() {
    const response = await axios.get('/api/media/music')
    return response.data.data || response.data
  },

  // 음악 상세 조회
  async getMusicById(id) {
    const response = await axios.get(`/api/media/music/${id}`)
    return response.data.data || response.data
  },

  // 음악 검색
  async searchMusic(filters) {
    const params = new URLSearchParams()
    if (filters.title) params.append('title', filters.title)
    if (filters.artist) params.append('artist', filters.artist)
    
    const response = await axios.get(`/api/media/music?${params.toString()}`)
    return response.data.data || response.data
  },

  // 음악 생성
  async createMusic(musicData) {
    const response = await axios.post('/api/media/music', musicData)
    return response.data.data || response.data
  },

  // 음악 수정
  async updateMusic(id, musicData) {
    const response = await axios.put(`/api/media/music/${id}`, musicData)
    return response.data.data || response.data
  },

  // 음악 삭제
  async deleteMusic(id) {
    await axios.delete(`/api/media/music/${id}`)
  }
}
