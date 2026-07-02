import axios from '../api/axios'
import { unwrapApiResponse } from './apiResponse'
import {
  buildContactCreatePayload,
  buildContactUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const contactService = {
  async getAllContacts() {
    const response = await axios.get('/media/contacts')
    return unwrapApiResponse(response)
  },

  // 연락처 상세 조회
  async getContactById(id) {
    const response = await axios.get(`/media/contacts/${id}`)
    return unwrapApiResponse(response)
  },

  // 연락처 검색
  async searchContacts(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/contacts', { params })
    return unwrapApiResponse(response)
  },

  async createContact(contactData) {
    const payload = buildContactCreatePayload(contactData)
    const response = await axios.post('/media/contacts', payload)
    return unwrapApiResponse(response)
  },

  async updateContact(id, contactData) {
    const payload = buildContactUpdatePayload(contactData)
    const response = await axios.put(`/media/contacts/${id}`, payload)
    return unwrapApiResponse(response)
  },

  async deleteContact(id) {
    await axios.delete(`/media/contacts/${id}`)
  }
}
