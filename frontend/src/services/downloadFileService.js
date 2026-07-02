import axios from '../api/axios'
import { unwrapApiResponse } from './apiResponse'
import {
  buildDownloadFileCreatePayload,
  buildDownloadFileUpdatePayload,
  sanitizeQueryParams
} from './payloadMappers'

export const downloadFileService = {
  // 다운로드 파일 목록 조회
  async getAllDownloadFiles() {
    const response = await axios.get('/media/download-files')
    return unwrapApiResponse(response)
  },

  // 다운로드 파일 상세 조회
  async getDownloadFileById(id) {
    const response = await axios.get(`/media/download-files/${id}`)
    return unwrapApiResponse(response)
  },

  // 다운로드 파일 검색
  async searchDownloadFiles(filters) {
    const params = sanitizeQueryParams(filters)
    const response = await axios.get('/media/download-files', { params })
    return unwrapApiResponse(response)
  },

  // 다운로드 파일 생성
  async createDownloadFile(downloadFileData) {
    const payload = buildDownloadFileCreatePayload(downloadFileData)
    const response = await axios.post('/media/download-files', payload)
    return unwrapApiResponse(response)
  },

  // 다운로드 파일 수정
  async updateDownloadFile(id, downloadFileData) {
    const payload = buildDownloadFileUpdatePayload(downloadFileData)
    const response = await axios.put(`/media/download-files/${id}`, payload)
    return unwrapApiResponse(response)
  },

  // 다운로드 파일 삭제
  async deleteDownloadFile(id) {
    await axios.delete(`/media/download-files/${id}`)
  }
}
