import axios from '../api/axios'
import {
  buildDownloadFileCreatePayload,
  buildDownloadFileUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const downloadFileService = {
  async getAllDownloadFiles() {
    const response = await axios.get('/media/download-files')
    return response.data.data
  },

  async searchDownloadFiles(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/download-files', { params })
    return response.data.data
  },

  async createDownloadFile(downloadFileData) {
    const payload = buildDownloadFileCreatePayload(downloadFileData)
    const response = await axios.post('/media/download-files', payload)
    return response.data.data
  },

  async updateDownloadFile(id, downloadFileData) {
    const payload = buildDownloadFileUpdatePayload(downloadFileData)
    const response = await axios.put(`/media/download-files/${id}`, payload)
    return response.data.data
  },

  async deleteDownloadFile(id) {
    await axios.delete(`/media/download-files/${id}`)
  }
}
