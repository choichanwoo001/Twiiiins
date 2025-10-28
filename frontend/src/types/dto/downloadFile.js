// 다운로드 파일 관련 DTO 정의

export const createDownloadFileSearchFilters = () => ({
  name: ''
})

export const createDownloadFileForm = () => ({
  name: '',
  fileUrl: '',
  displayOrder: 0
})

export const resetDownloadFileForm = (form) => {
  Object.assign(form, createDownloadFileForm())
}

export const resetDownloadFileSearchFilters = (filters) => {
  Object.assign(filters, createDownloadFileSearchFilters())
}
