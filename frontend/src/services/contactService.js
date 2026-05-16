import axios from '../api/axios'
import {
  buildContactCreatePayload,
  buildContactUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const contactService = {
  async getAllContacts() {
    const response = await axios.get('/media/contacts')
    return response.data.data
  },

  async searchContacts(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/contacts', { params })
    return response.data.data
  },

  async createContact(contactData) {
    const payload = buildContactCreatePayload(contactData)
    const response = await axios.post('/media/contacts', payload)
    return response.data.data
  },

  async updateContact(id, contactData) {
    const payload = buildContactUpdatePayload(contactData)
    const response = await axios.put(`/media/contacts/${id}`, payload)
    return response.data.data
  },

  async deleteContact(id) {
    await axios.delete(`/media/contacts/${id}`)
  }
}
