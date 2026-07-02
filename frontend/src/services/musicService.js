import axios from '../api/axios'
import { unwrapApiResponse } from './apiResponse'
import {
  buildMusicCreatePayload,
  buildMusicUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const musicService = {
  async getAllMusic() {
    const response = await axios.get('/media/music')
    return unwrapApiResponse(response)
  },

  // 음악 상세 조회
  async getMusicById(id) {
    const response = await axios.get(`/media/music/${id}`)
    return unwrapApiResponse(response)
  },

  // 음악 검색
  async searchMusic(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/music', { params })
    return unwrapApiResponse(response)
  },

  async createMusic(musicData) {
    const payload = buildMusicCreatePayload(musicData)
    const response = await axios.post('/media/music', payload)
    return unwrapApiResponse(response)
  },

  async updateMusic(id, musicData) {
    const payload = buildMusicUpdatePayload(musicData)
    const response = await axios.put(`/media/music/${id}`, payload)
    return unwrapApiResponse(response)
  },

  async deleteMusic(id) {
    await axios.delete(`/media/music/${id}`)
  }
}
