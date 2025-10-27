// 비디오 관련 DTO 정의

export const createVideoSearchFilters = () => ({
  title: ''
})

export const createVideoForm = () => ({
  title: '',
  embedUrl: '',
  displayOrder: 0
})

export const resetVideoForm = (form) => {
  Object.assign(form, createVideoForm())
}

export const resetVideoSearchFilters = (filters) => {
  Object.assign(filters, createVideoSearchFilters())
}
