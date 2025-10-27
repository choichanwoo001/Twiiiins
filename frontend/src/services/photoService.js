import axios from '../api/axios'

export const photoService = {
  // 사진 그룹 관련
  async getAllPhotoGroups() {
    const response = await axios.get('/api/media/photo-groups')
    return response.data
  },

  async getPhotoGroup(id) {
    const response = await axios.get(`/api/media/photo-groups/${id}`)
    return response.data
  },

  async createPhotoGroup(data) {
    const response = await axios.post('/api/media/photo-groups', data)
    return response.data
  },

  async updatePhotoGroup(id, data) {
    const response = await axios.put(`/api/media/photo-groups/${id}`, data)
    return response.data
  },

  async deletePhotoGroup(id) {
    const response = await axios.delete(`/api/media/photo-groups/${id}`)
    return response.data
  },

  async searchPhotoGroups(filters) {
    // 현재는 클라이언트 사이드 필터링
    const allGroups = await this.getAllPhotoGroups()
    return allGroups.filter(group => {
      if (filters.title && !group.title.toLowerCase().includes(filters.title.toLowerCase())) {
        return false
      }
      return true
    })
  },

  // 사진 관련
  async uploadPhotos(groupId, files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })

    const response = await axios.post(`/api/media/photos/${groupId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    return response.data
  },

  async deletePhoto(photoId) {
    const response = await axios.delete(`/api/media/photos/${photoId}`)
    return response.data
  }
}