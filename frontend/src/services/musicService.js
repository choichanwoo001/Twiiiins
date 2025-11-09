import axios from '../api/axios'
import {
  buildMusicCreatePayload,
  buildMusicUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const musicService = {
  // 음악 목록 조회
  async getAllMusic() {
    const response = await axios.get('/media/music')
    return response.data.data || response.data
  },

  // 음악 상세 조회
  async getMusicById(id) {
    const response = await axios.get(`/media/music/${id}`)
    return response.data.data || response.data
  },

  // 음악 검색
  async searchMusic(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/music', { params })
    return response.data.data || response.data
  },

  // 음악 생성
  async createMusic(musicData) {
    const payload = buildMusicCreatePayload(musicData)
    const response = await axios.post('/media/music', payload)
    return response.data.data || response.data
  },

  // 음악 수정
  async updateMusic(id, musicData) {
    const payload = buildMusicUpdatePayload(musicData)
    const response = await axios.put(`/media/music/${id}`, payload)
    return response.data.data || response.data
  },

  // 음악 삭제
  async deleteMusic(id) {
    await axios.delete(`/media/music/${id}`)
  }
}
