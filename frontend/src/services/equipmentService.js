import axios from '../api/axios'

export const equipmentService = {
  // 장비 목록 조회
  async getAllEquipment() {
    const response = await axios.get('/api/media/equipment')
    return response.data
  },

  // 장비 상세 조회
  async getEquipmentById(id) {
    const response = await axios.get(`/api/media/equipment/${id}`)
    return response.data
  },

  // 장비 검색
  async searchEquipment(filters) {
    const params = new URLSearchParams()
    if (filters.name) params.append('name', filters.name)
    
    const response = await axios.get(`/api/media/equipment?${params.toString()}`)
    return response.data
  },

  // 장비 생성
  async createEquipment(equipmentData) {
    const response = await axios.post('/api/media/equipment', equipmentData)
    return response.data
  },

  // 장비 수정
  async updateEquipment(id, equipmentData) {
    const response = await axios.put(`/api/media/equipment/${id}`, equipmentData)
    return response.data
  },

  // 장비 삭제
  async deleteEquipment(id) {
    await axios.delete(`/api/media/equipment/${id}`)
  }
}
