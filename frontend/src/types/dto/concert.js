// 콘서트 관련 DTO 정의

/**
 * @typedef {Object} ConcertSearchFilters
 * @property {string} name - 콘서트명
 * @property {string} location - 장소
 * @property {string} startDate - 시작일
 * @property {string} endDate - 종료일
 */

/**
 * @typedef {Object} ConcertForm
 * @property {string} date - 날짜
 * @property {string} location - 장소
 * @property {string} name - 콘서트명
 * @property {string} startTime - 시작 시간
 * @property {string} ticketInfo - 티켓 정보
 * @property {string} fullLocation - 전체 장소명
 * @property {string} googleMapUrl - 구글 맵 URL
 * @property {string} collaborationInfo - 협업 정보
 * @property {boolean} isPast - 과거 이벤트 여부
 */

/**
 * @typedef {Object} Concert
 * @property {number} id - 콘서트 ID
 * @property {string} name - 콘서트명
 * @property {string} date - 날짜
 * @property {string} location - 장소
 * @property {string} startTime - 시작 시간
 * @property {string} ticketInfo - 티켓 정보
 * @property {string} fullLocation - 전체 장소명
 * @property {string} googleMapUrl - 구글 맵 URL
 * @property {string} collaborationInfo - 협업 정보
 * @property {boolean} isPast - 과거 이벤트 여부
 * @property {string} createdAt - 생성일
 * @property {string} updatedAt - 수정일
 */

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
