// 사진 관련 DTO 정의

/**
 * @typedef {Object} PhotoGroupSearchFilters
 * @property {string} title - 검색 제목
 */

/**
 * @typedef {Object} PhotoGroupForm
 * @property {string} title - 그룹 제목
 * @property {number} displayOrder - 표시 순서
 */

/**
 * @typedef {Object} PhotoAddForm
 * @property {string} photoGroupId - 사진 그룹 ID
 * @property {string} imageUrl - 이미지 URL
 * @property {string} title - 사진 제목
 * @property {number} displayOrder - 표시 순서
 */

/**
 * @typedef {Object} PhotoEditForm
 * @property {number|null} id - 사진 ID
 * @property {string} imageUrl - 이미지 URL
 * @property {string} title - 사진 제목
 * @property {number} displayOrder - 표시 순서
 */

/**
 * @typedef {Object} PhotoGroup
 * @property {number} id - 그룹 ID
 * @property {string} title - 그룹 제목
 * @property {number} displayOrder - 표시 순서
 * @property {Photo[]} photos - 사진 목록
 * @property {string} createdAt - 생성일
 * @property {string} updatedAt - 수정일
 */

/**
 * @typedef {Object} Photo
 * @property {number} id - 사진 ID
 * @property {string} imageUrl - 이미지 URL
 * @property {string} title - 사진 제목
 * @property {string} altText - 대체 텍스트
 * @property {number} displayOrder - 표시 순서
 * @property {number} photoGroupId - 사진 그룹 ID
 * @property {string} createdAt - 생성일
 * @property {string} updatedAt - 수정일
 */

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
