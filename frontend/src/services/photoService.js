import axios from '../api/axios'
import { unwrapApiResponse } from './apiResponse'
import {
  buildPhotoGroupCreatePayload,
  buildPhotoGroupUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const photoService = {
  async getAllPhotoGroups() {
    const response = await axios.get('/media/photo-groups')
    return unwrapApiResponse(response)
  },

  async getPhotoGroupById(id) {
    const response = await axios.get(`/media/photos/groups/${id}`)
    return unwrapApiResponse(response)
  },

  async createPhotoGroup(data) {
    const payload = buildPhotoGroupCreatePayload(data)
    const response = await axios.post('/media/photos/groups', payload)
    return unwrapApiResponse(response)
  },

  async updatePhotoGroup(id, data) {
    const payload = buildPhotoGroupUpdatePayload(data)
    const response = await axios.put(`/media/photos/groups/${id}`, payload)
    return unwrapApiResponse(response)
  },

  async deletePhotoGroup(id) {
    const response = await axios.delete(`/media/photos/groups/${id}`)
    return unwrapApiResponse(response)
  },

  async searchPhotoGroups(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/photo-groups', { params })
    return unwrapApiResponse(response)
  },

  async uploadPhotos(groupId, files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    const response = await axios.post(`/media/photos/groups/${groupId}/photos`, formData)
    return unwrapApiResponse(response)
  },

  async deletePhoto(photoId) {
    const response = await axios.delete(`/media/photos/${photoId}`)
    return unwrapApiResponse(response)
  }
}
