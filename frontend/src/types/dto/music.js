// 음악 관련 DTO 정의

export const createMusicSearchFilters = () => ({
  title: '',
  artist: ''
})

export const createMusicForm = () => ({
  title: '',
  artist: '',
  coverUrl: '',
  linkUrl: '',
  displayOrder: 0
})

export const resetMusicForm = (form) => {
  Object.assign(form, createMusicForm())
}

export const resetMusicSearchFilters = (filters) => {
  Object.assign(filters, createMusicSearchFilters())
}
