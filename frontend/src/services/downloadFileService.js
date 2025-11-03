import axios from '../api/axios'

export const downloadFileService = {
  // 다운로드 파일 목록 조회
  async getAllDownloadFiles() {
    const response = await axios.get('/media/download-files')
    return response.data.data || response.data
  },

  // 다운로드 파일 상세 조회
  async getDownloadFileById(id) {
    const response = await axios.get(`/media/download-files/${id}`)
    return response.data.data || response.data
  },

  // 다운로드 파일 검색
  async searchDownloadFiles(filters) {
    const params = new URLSearchParams()
    if (filters.name) params.append('name', filters.name)
    
    const response = await axios.get(`/media/download-files?${params.toString()}`)
    return response.data.data || response.data
  },

  // 다운로드 파일 생성
  async createDownloadFile(downloadFileData) {
    const response = await axios.post('/media/download-files', downloadFileData)
    return response.data.data || response.data
  },

  // 다운로드 파일 수정
  async updateDownloadFile(id, downloadFileData) {
    const response = await axios.put(`/media/download-files/${id}`, downloadFileData)
    return response.data.data || response.data
  },

  // 다운로드 파일 삭제
  async deleteDownloadFile(id) {
    await axios.delete(`/media/download-files/${id}`)
  }
}
