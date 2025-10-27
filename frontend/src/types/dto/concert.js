// 콘서트 관련 DTO 정의

export const createConcertSearchFilters = () => ({
  name: '',
  location: '',
  startDate: '',
  endDate: ''
})

export const createConcertForm = () => ({
  date: '',
  location: '',
  name: '',
  startTime: '',
  ticketInfo: '',
  fullLocation: '',
  googleMapUrl: '',
  collaborationInfo: '',
  isPast: false
})

export const resetConcertForm = (form) => {
  Object.assign(form, createConcertForm())
}

export const resetConcertSearchFilters = (filters) => {
  Object.assign(filters, createConcertSearchFilters())
}
