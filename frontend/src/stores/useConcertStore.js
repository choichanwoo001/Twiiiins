import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { concertService } from '../services'

export const useConcertStore = defineStore('concert', () => {
  // 상태
  const concerts = ref([])
  const upcomingConcerts = ref([])
  const pastConcerts = ref([])
  const isLoading = ref(false)
  const error = ref(null)

  // 게터
  const concertCount = computed(() => concerts.value.length)
  const upcomingCount = computed(() => upcomingConcerts.value.length)
  const pastCount = computed(() => pastConcerts.value.length)

  // 액션
  const setLoading = (loading) => {
    isLoading.value = loading
  }

  const setError = (errorMessage) => {
    error.value = errorMessage
  }

  const clearError = () => {
    error.value = null
  }

  const fetchConcerts = async () => {
    const data = await concertService.getAllConcerts()
    concerts.value = data
    updateFilteredConcerts()
  }

  const loadConcerts = async () => {
    try {
      setLoading(true)
      clearError()
      await fetchConcerts()
    } catch (err) {
      setError('콘서트 목록을 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const updateFilteredConcerts = () => {
    upcomingConcerts.value = concerts.value.filter(concert => !concert.isPast)
    pastConcerts.value = concerts.value.filter(concert => concert.isPast)
  }

  const addConcert = async (concertData) => {
    try {
      setLoading(true)
      clearError()
      const newConcert = await concertService.createConcert(concertData)
      await fetchConcerts()
      return newConcert
    } catch (err) {
      setError('콘서트 생성에 실패했습니다.')
      throw err
    } finally {
      setLoading(false)
    }
  }

  const updateConcert = async (id, concertData) => {
    try {
      setLoading(true)
      clearError()
      const updatedConcert = await concertService.updateConcert(id, concertData)
      await fetchConcerts()
      return updatedConcert
    } catch (err) {
      setError('콘서트 수정에 실패했습니다.')
      throw err
    } finally {
      setLoading(false)
    }
  }

  const deleteConcert = async (id) => {
    try {
      setLoading(true)
      clearError()
      await concertService.deleteConcert(id)
      await fetchConcerts()
    } catch (err) {
      setError('콘서트 삭제에 실패했습니다.')
      throw err
    } finally {
      setLoading(false)
    }
  }

  const moveToPast = async (id) => {
    try {
      setLoading(true)
      clearError()
      await concertService.moveToPastEvent(id)
      await fetchConcerts()
    } catch (err) {
      setError('콘서트 이동에 실패했습니다.')
      throw err
    } finally {
      setLoading(false)
    }
  }

  const moveToUpcoming = async (id) => {
    try {
      setLoading(true)
      clearError()
      await concertService.moveToUpcomingEvent(id)
      await fetchConcerts()
    } catch (err) {
      setError('콘서트 이동에 실패했습니다.')
      throw err
    } finally {
      setLoading(false)
    }
  }

  const moveToPastEvent = async (id) => {
    return await moveToPast(id)
  }

  const moveToUpcomingEvent = async (id) => {
    return await moveToUpcoming(id)
  }

  const triggerAutoMove = async () => {
    try {
      setLoading(true)
      clearError()
      await concertService.triggerAutoMove()
      await fetchConcerts()
    } catch (err) {
      setError('자동 이동 실행에 실패했습니다.')
      throw err
    } finally {
      setLoading(false)
    }
  }

  return {
    // 상태
    concerts,
    upcomingConcerts,
    pastConcerts,
    isLoading,
    error,
    // 게터
    concertCount,
    upcomingCount,
    pastCount,
    // 액션
    loadConcerts,
    addConcert,
    updateConcert,
    deleteConcert,
    moveToPast,
    moveToUpcoming,
    moveToPastEvent,
    moveToUpcomingEvent,
    triggerAutoMove,
    setLoading,
    setError,
    clearError
  }
})
