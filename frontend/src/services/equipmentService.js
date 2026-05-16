import axios from '../api/axios'
import {
  buildEquipmentCreatePayload,
  buildEquipmentUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const equipmentService = {
  async getAllEquipment() {
    const response = await axios.get('/media/equipment')
    return response.data.data
  },

  async searchEquipment(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/equipment', { params })
    return response.data.data
  },

  async createEquipment(equipmentData) {
    const payload = buildEquipmentCreatePayload(equipmentData)
    const response = await axios.post('/media/equipment', payload)
    return response.data.data
  },

  async updateEquipment(id, equipmentData) {
    const payload = buildEquipmentUpdatePayload(equipmentData)
    const response = await axios.put(`/media/equipment/${id}`, payload)
    return response.data.data
  },

  async deleteEquipment(id) {
    await axios.delete(`/media/equipment/${id}`)
  }
}
