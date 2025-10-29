// 장비 관련 DTO 정의

export const createEquipmentSearchFilters = () => ({
  name: ''
})

export const createEquipmentForm = () => ({
  name: '',
  imageUrl: '',
  displayOrder: 0
})

export const resetEquipmentForm = (form) => {
  Object.assign(form, createEquipmentForm())
}

export const resetEquipmentSearchFilters = (filters) => {
  Object.assign(filters, createEquipmentSearchFilters())
}
