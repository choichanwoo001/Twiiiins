import axios from '../api/axios'
import {
  buildPhotoGroupCreatePayload,
  buildPhotoGroupUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const photoService = {
  // 사진 그룹 관련
  async getAllPhotoGroups() {
    const response = await axios.get('/media/photo-groups')
    return response.data.data || response.data
  },

  async getPhotoGroup(id) {
    const response = await axios.get(`/media/photos/groups/${id}`)
    return response.data.data || response.data
  },

  async createPhotoGroup(data) {
    const payload = buildPhotoGroupCreatePayload(data)
    const response = await axios.post('/media/photos/groups', payload)
    return response.data.data || response.data
  },

  async updatePhotoGroup(id, data) {
    const payload = buildPhotoGroupUpdatePayload(data)
    const response = await axios.put(`/media/photos/groups/${id}`, payload)
    return response.data.data || response.data
  },

  async deletePhotoGroup(id) {
    const response = await axios.delete(`/media/photos/groups/${id}`)
    return response.data.data || response.data
  },

  async searchPhotoGroups(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/photo-groups', { params })
    return response.data.data || response.data
  },

  // 사진 관련
  async uploadPhotos(groupId, files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })

    // axios 인터셉터에서 FormData일 때 Content-Type을 자동으로 제거하므로 헤더 설정 불필요
    const response = await axios.post(`/media/photos/groups/${groupId}/photos`, formData)
    return response.data.data || response.data
  },

  async deletePhoto(photoId) {
    const response = await axios.delete(`/media/photos/${photoId}`)
    return response.data.data || response.data
  }
}