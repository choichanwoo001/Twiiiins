import axios from '../api/axios'

export const downloadFileService = {
  // 다운로드 파일 목록 조회
  async getAllDownloadFiles() {
    const response = await axios.get('/api/media/download-files')
    return response.data
  },

  // 다운로드 파일 상세 조회
  async getDownloadFileById(id) {
    const response = await axios.get(`/api/media/download-files/${id}`)
    return response.data
  },

  // 다운로드 파일 검색
  async searchDownloadFiles(filters) {
    const params = new URLSearchParams()
    if (filters.name) params.append('name', filters.name)
    
    const response = await axios.get(`/api/media/download-files?${params.toString()}`)
    return response.data
  },

  // 다운로드 파일 생성
  async createDownloadFile(downloadFileData) {
    const response = await axios.post('/api/media/download-files', downloadFileData)
    return response.data
  },

  // 다운로드 파일 수정
  async updateDownloadFile(id, downloadFileData) {
    const response = await axios.put(`/api/media/download-files/${id}`, downloadFileData)
    return response.data
  },

  // 다운로드 파일 삭제
  async deleteDownloadFile(id) {
    await axios.delete(`/api/media/download-files/${id}`)
  }
}
