// 사진 관련 DTO 정의

export const createPhotoGroupSearchFilters = () => ({
  title: ''
})

export const createPhotoGroupForm = () => ({
  title: '',
  displayOrder: 0
})

export const createPhotoAddForm = () => ({
  photoGroupId: '',
  imageUrl: '',
  title: '',
  displayOrder: 0
})

export const createPhotoEditForm = () => ({
  id: null,
  imageUrl: '',
  title: '',
  displayOrder: 0
})

export const resetPhotoGroupForm = (form) => {
  Object.assign(form, createPhotoGroupForm())
}

export const resetPhotoGroupSearchFilters = (filters) => {
  Object.assign(filters, createPhotoGroupSearchFilters())
}

export const resetPhotoAddFormData = (form) => {
  Object.assign(form, createPhotoAddForm())
}

export const resetPhotoEditForm = (form) => {
  Object.assign(form, createPhotoEditForm())
}
