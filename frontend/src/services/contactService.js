import axios from '../api/axios'

export const contactService = {
  // 연락처 목록 조회
  async getAllContacts() {
    const response = await axios.get('/api/media/contacts')
    return response.data.data || response.data
  },

  // 연락처 상세 조회
  async getContactById(id) {
    const response = await axios.get(`/api/media/contacts/${id}`)
    return response.data.data || response.data
  },

  // 연락처 검색
  async searchContacts(filters) {
    const params = new URLSearchParams()
    if (filters.name) params.append('name', filters.name)
    if (filters.role) params.append('role', filters.role)
    
    const response = await axios.get(`/api/media/contacts?${params.toString()}`)
    return response.data.data || response.data
  },

  // 연락처 생성
  async createContact(contactData) {
    const response = await axios.post('/api/media/contacts', contactData)
    return response.data.data || response.data
  },

  // 연락처 수정
  async updateContact(id, contactData) {
    const response = await axios.put(`/api/media/contacts/${id}`, contactData)
    return response.data.data || response.data
  },

  // 연락처 삭제
  async deleteContact(id) {
    await axios.delete(`/api/media/contacts/${id}`)
  }
}
