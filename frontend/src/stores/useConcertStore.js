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

  const loadConcerts = async () => {
    try {
      setLoading(true)
      clearError()
      const data = await concertService.getAllConcerts()
      concerts.value = data
      updateFilteredConcerts()
    } catch (err) {
      setError('콘서트 목록을 불러오는데 실패했습니다.')
      console.error('콘서트 로드 실패:', err)
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
      concerts.value.push(newConcert)
      updateFilteredConcerts()
      return newConcert
    } catch (err) {
      setError('콘서트 생성에 실패했습니다.')
      console.error('콘서트 생성 실패:', err)
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
      const index = concerts.value.findIndex(concert => concert.id === id)
      if (index !== -1) {
        concerts.value[index] = updatedConcert
        updateFilteredConcerts()
      }
      return updatedConcert
    } catch (err) {
      setError('콘서트 수정에 실패했습니다.')
      console.error('콘서트 수정 실패:', err)
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
      concerts.value = concerts.value.filter(concert => concert.id !== id)
      updateFilteredConcerts()
    } catch (err) {
      setError('콘서트 삭제에 실패했습니다.')
      console.error('콘서트 삭제 실패:', err)
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
      await loadConcerts() // 전체 목록 다시 로드
    } catch (err) {
      setError('콘서트 이동에 실패했습니다.')
      console.error('콘서트 이동 실패:', err)
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
      await loadConcerts() // 전체 목록 다시 로드
    } catch (err) {
      setError('콘서트 이동에 실패했습니다.')
      console.error('콘서트 이동 실패:', err)
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
    setLoading,
    setError,
    clearError
  }
})
