// 연락처 관련 DTO 정의

export const createContactSearchFilters = () => ({
  name: '',
  role: '',
  email: ''
})

export const createContactForm = () => ({
  name: '',
  role: '',
  email: '',
  displayOrder: 0
})

export const resetContactForm = (form) => {
  Object.assign(form, createContactForm())
}

export const resetContactSearchFilters = (filters) => {
  Object.assign(filters, createContactSearchFilters())
}
