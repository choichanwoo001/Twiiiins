import axios from '../api/axios'
import { unwrapApiResponse } from './apiResponse'
import {
  buildEquipmentCreatePayload,
  buildEquipmentUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const equipmentService = {
  async getAllEquipment() {
    const response = await axios.get('/media/equipment')
    return unwrapApiResponse(response)
  },

  // 장비 상세 조회
  async getEquipmentById(id) {
    const response = await axios.get(`/media/equipment/${id}`)
    return unwrapApiResponse(response)
  },

  // 장비 검색
  async searchEquipment(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/equipment', { params })
    return unwrapApiResponse(response)
  },

  async createEquipment(equipmentData) {
    const payload = buildEquipmentCreatePayload(equipmentData)
    const response = await axios.post('/media/equipment', payload)
    return unwrapApiResponse(response)
  },

  async updateEquipment(id, equipmentData) {
    const payload = buildEquipmentUpdatePayload(equipmentData)
    const response = await axios.put(`/media/equipment/${id}`, payload)
    return unwrapApiResponse(response)
  },

  async deleteEquipment(id) {
    await axios.delete(`/media/equipment/${id}`)
  }
}
