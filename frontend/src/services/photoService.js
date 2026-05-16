import axios from '../api/axios'
import {
  buildPhotoGroupCreatePayload,
  buildPhotoGroupUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const photoService = {
  async getAllPhotoGroups() {
    const response = await axios.get('/media/photo-groups')
    return response.data.data
  },

  async getPhotoGroupById(id) {
    const response = await axios.get(`/media/photos/groups/${id}`)
    return response.data.data
  },

  async createPhotoGroup(data) {
    const payload = buildPhotoGroupCreatePayload(data)
    const response = await axios.post('/media/photos/groups', payload)
    return response.data.data
  },

  async updatePhotoGroup(id, data) {
    const payload = buildPhotoGroupUpdatePayload(data)
    const response = await axios.put(`/media/photos/groups/${id}`, payload)
    return response.data.data
  },

  async deletePhotoGroup(id) {
    await axios.delete(`/media/photos/groups/${id}`)
  },

  async searchPhotoGroups(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/photo-groups', { params })
    return response.data.data
  },

  async uploadPhotos(groupId, files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    const response = await axios.post(`/media/photos/groups/${groupId}/photos`, formData)
    return response.data.data
  },

  async deletePhoto(photoId) {
    await axios.delete(`/media/photos/${photoId}`)
  }
}