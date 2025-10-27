// 뉴스 관련 DTO 정의

export const createNewsSearchFilters = () => ({
  title: '',
  startDate: '',
  endDate: ''
})

export const createNewsForm = () => ({
  date: '',
  title: '',
  description: '',
  displayOrder: 0
})

export const resetNewsForm = (form) => {
  Object.assign(form, createNewsForm())
}

export const resetNewsSearchFilters = (filters) => {
  Object.assign(filters, createNewsSearchFilters())
}
