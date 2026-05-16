import axios from '../api/axios'
import {
  buildMusicCreatePayload,
  buildMusicUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const musicService = {
  async getAllMusic() {
    const response = await axios.get('/media/music')
    return response.data.data
  },

  async searchMusic(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/music', { params })
    return response.data.data
  },

  async createMusic(musicData) {
    const payload = buildMusicCreatePayload(musicData)
    const response = await axios.post('/media/music', payload)
    return response.data.data
  },

  async updateMusic(id, musicData) {
    const payload = buildMusicUpdatePayload(musicData)
    const response = await axios.put(`/media/music/${id}`, payload)
    return response.data.data
  },

  async deleteMusic(id) {
    await axios.delete(`/media/music/${id}`)
  }
}
