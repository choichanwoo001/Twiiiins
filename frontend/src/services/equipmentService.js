import axios from '../api/axios'
import {
  buildEquipmentCreatePayload,
  buildEquipmentUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const equipmentService = {
  // 장비 목록 조회
  async getAllEquipment() {
    const response = await axios.get('/media/equipment')
    return response.data.data || response.data
  },

  // 장비 상세 조회
  async getEquipmentById(id) {
    const response = await axios.get(`/media/equipment/${id}`)
    return response.data.data || response.data
  },

  // 장비 검색
  async searchEquipment(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/equipment', { params })
    return response.data.data || response.data
  },

  // 장비 생성
  async createEquipment(equipmentData) {
    const payload = buildEquipmentCreatePayload(equipmentData)
    const response = await axios.post('/media/equipment', payload)
    return response.data.data || response.data
  },

  // 장비 수정
  async updateEquipment(id, equipmentData) {
    const payload = buildEquipmentUpdatePayload(equipmentData)
    const response = await axios.put(`/media/equipment/${id}`, payload)
    return response.data.data || response.data
  },

  // 장비 삭제
  async deleteEquipment(id) {
    await axios.delete(`/media/equipment/${id}`)
  }
}
