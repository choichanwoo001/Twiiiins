import axios from '../api/axios'

export const photoService = {
  // 사진 그룹 목록 조회
  async getAllPhotoGroups() {
    const response = await axios.get('/api/media/photo-groups')
    return response.data
  },

  // 사진 그룹 상세 조회
  async getPhotoGroupById(id) {
    const response = await axios.get(`/api/media/photo-groups/${id}`)
    return response.data
  },

  // 사진 그룹 검색
  async searchPhotoGroups(filters) {
    const params = new URLSearchParams()
    if (filters.title) params.append('title', filters.title)
    
    const response = await axios.get(`/api/media/photo-groups?${params.toString()}`)
    return response.data
  },

  // 사진 그룹 생성
  async createPhotoGroup(photoGroupData) {
    const response = await axios.post('/api/media/photo-groups', photoGroupData)
    return response.data
  },

  // 사진 그룹 수정
  async updatePhotoGroup(id, photoGroupData) {
    const response = await axios.put(`/api/media/photo-groups/${id}`, photoGroupData)
    return response.data
  },

  // 사진 그룹 삭제
  async deletePhotoGroup(id) {
    await axios.delete(`/api/media/photo-groups/${id}`)
  },

  // 그룹별 사진 목록 조회
  async getPhotosByGroupId(groupId) {
    const response = await axios.get(`/api/media/photo-groups/${groupId}/photos`)
    return response.data
  },

  // 사진 상세 조회
  async getPhotoById(id) {
    const response = await axios.get(`/api/media/photos/${id}`)
    return response.data
  },

  // 사진 생성
  async createPhoto(groupId, photoData) {
    const response = await axios.post(`/api/media/photo-groups/${groupId}/photos`, photoData)
    return response.data
  },

  // 사진 수정
  async updatePhoto(id, photoData) {
    const response = await axios.put(`/api/media/photos/${id}`, photoData)
    return response.data
  },

  // 사진 삭제
  async deletePhoto(id) {
    await axios.delete(`/api/media/photos/${id}`)
  }
}
