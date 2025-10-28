import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { photoService, videoService, musicService, newsService, equipmentService } from '../services'

export const useMediaStore = defineStore('media', () => {
  // 상태
  const photoGroups = ref([])
  const videos = ref([])
  const musicItems = ref([])
  const newsItems = ref([])
  const equipmentItems = ref([])
  const isLoading = ref(false)
  const error = ref(null)

  // 게터
  const photoGroupCount = computed(() => photoGroups.value.length)
  const videoCount = computed(() => videos.value.length)
  const musicCount = computed(() => musicItems.value.length)
  const newsCount = computed(() => newsItems.value.length)
  const equipmentCount = computed(() => equipmentItems.value.length)

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

  // 사진 그룹 관련
  const loadPhotoGroups = async () => {
    try {
      setLoading(true)
      clearError()
      const data = await photoService.getAllPhotoGroups()
      photoGroups.value = data
    } catch (err) {
      setError('사진 그룹 목록을 불러오는데 실패했습니다.')
      console.error('사진 그룹 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  const addPhotoGroup = async (groupData) => {
    try {
      setLoading(true)
      clearError()
      const newGroup = await photoService.createPhotoGroup(groupData)
      photoGroups.value.push(newGroup)
      return newGroup
    } catch (err) {
      setError('사진 그룹 생성에 실패했습니다.')
      console.error('사진 그룹 생성 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  const updatePhotoGroup = async (id, groupData) => {
    try {
      setLoading(true)
      clearError()
      const updatedGroup = await photoService.updatePhotoGroup(id, groupData)
      const index = photoGroups.value.findIndex(group => group.id === id)
      if (index !== -1) {
        photoGroups.value[index] = updatedGroup
      }
      return updatedGroup
    } catch (err) {
      setError('사진 그룹 수정에 실패했습니다.')
      console.error('사진 그룹 수정 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  const deletePhotoGroup = async (id) => {
    try {
      setLoading(true)
      clearError()
      await photoService.deletePhotoGroup(id)
      photoGroups.value = photoGroups.value.filter(group => group.id !== id)
    } catch (err) {
      setError('사진 그룹 삭제에 실패했습니다.')
      console.error('사진 그룹 삭제 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  // 비디오 관련
  const loadVideos = async () => {
    try {
      setLoading(true)
      clearError()
      const data = await videoService.getAllVideos()
      videos.value = data
    } catch (err) {
      setError('비디오 목록을 불러오는데 실패했습니다.')
      console.error('비디오 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  // 음악 관련
  const loadMusic = async () => {
    try {
      setLoading(true)
      clearError()
      const data = await musicService.getAllMusic()
      musicItems.value = data
    } catch (err) {
      setError('음악 목록을 불러오는데 실패했습니다.')
      console.error('음악 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  // 뉴스 관련
  const loadNews = async () => {
    try {
      setLoading(true)
      clearError()
      const data = await newsService.getAllNews()
      newsItems.value = data
    } catch (err) {
      setError('뉴스 목록을 불러오는데 실패했습니다.')
      console.error('뉴스 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  // 장비 관련
  const loadEquipment = async () => {
    try {
      setLoading(true)
      clearError()
      const data = await equipmentService.getAllEquipment()
      equipmentItems.value = data
    } catch (err) {
      setError('장비 목록을 불러오는데 실패했습니다.')
      console.error('장비 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  // 전체 미디어 로드
  const loadAllMedia = async () => {
    await Promise.all([
      loadPhotoGroups(),
      loadVideos(),
      loadMusic(),
      loadNews(),
      loadEquipment()
    ])
  }

  return {
    // 상태
    photoGroups,
    videos,
    musicItems,
    newsItems,
    equipmentItems,
    isLoading,
    error,
    // 게터
    photoGroupCount,
    videoCount,
    musicCount,
    newsCount,
    equipmentCount,
    // 액션
    loadPhotoGroups,
    addPhotoGroup,
    updatePhotoGroup,
    deletePhotoGroup,
    loadVideos,
    loadMusic,
    loadNews,
    loadEquipment,
    loadAllMedia,
    setLoading,
    setError,
    clearError
  }
})
