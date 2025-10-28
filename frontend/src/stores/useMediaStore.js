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

  const addVideo = async (videoData) => {
    try {
      setLoading(true)
      clearError()
      const newVideo = await videoService.createVideo(videoData)
      videos.value.push(newVideo)
      return newVideo
    } catch (err) {
      setError('비디오 생성에 실패했습니다.')
      console.error('비디오 생성 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  const updateVideo = async (id, videoData) => {
    try {
      setLoading(true)
      clearError()
      const updatedVideo = await videoService.updateVideo(id, videoData)
      const index = videos.value.findIndex(video => video.id === id)
      if (index !== -1) {
        videos.value[index] = updatedVideo
      }
      return updatedVideo
    } catch (err) {
      setError('비디오 수정에 실패했습니다.')
      console.error('비디오 수정 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  const deleteVideo = async (id) => {
    try {
      setLoading(true)
      clearError()
      await videoService.deleteVideo(id)
      videos.value = videos.value.filter(video => video.id !== id)
    } catch (err) {
      setError('비디오 삭제에 실패했습니다.')
      console.error('비디오 삭제 실패:', err)
      throw err
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

  const addMusic = async (musicData) => {
    try {
      setLoading(true)
      clearError()
      const newMusic = await musicService.createMusic(musicData)
      musicItems.value.push(newMusic)
      return newMusic
    } catch (err) {
      setError('음악 생성에 실패했습니다.')
      console.error('음악 생성 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  const updateMusic = async (id, musicData) => {
    try {
      setLoading(true)
      clearError()
      const updatedMusic = await musicService.updateMusic(id, musicData)
      const index = musicItems.value.findIndex(music => music.id === id)
      if (index !== -1) {
        musicItems.value[index] = updatedMusic
      }
      return updatedMusic
    } catch (err) {
      setError('음악 수정에 실패했습니다.')
      console.error('음악 수정 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  const deleteMusic = async (id) => {
    try {
      setLoading(true)
      clearError()
      await musicService.deleteMusic(id)
      musicItems.value = musicItems.value.filter(music => music.id !== id)
    } catch (err) {
      setError('음악 삭제에 실패했습니다.')
      console.error('음악 삭제 실패:', err)
      throw err
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

  const addNews = async (newsData) => {
    try {
      setLoading(true)
      clearError()
      const newNews = await newsService.createNews(newsData)
      newsItems.value.push(newNews)
      return newNews
    } catch (err) {
      setError('뉴스 생성에 실패했습니다.')
      console.error('뉴스 생성 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  const updateNews = async (id, newsData) => {
    try {
      setLoading(true)
      clearError()
      const updatedNews = await newsService.updateNews(id, newsData)
      const index = newsItems.value.findIndex(news => news.id === id)
      if (index !== -1) {
        newsItems.value[index] = updatedNews
      }
      return updatedNews
    } catch (err) {
      setError('뉴스 수정에 실패했습니다.')
      console.error('뉴스 수정 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  const deleteNews = async (id) => {
    try {
      setLoading(true)
      clearError()
      await newsService.deleteNews(id)
      newsItems.value = newsItems.value.filter(news => news.id !== id)
    } catch (err) {
      setError('뉴스 삭제에 실패했습니다.')
      console.error('뉴스 삭제 실패:', err)
      throw err
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

  const addEquipment = async (equipmentData) => {
    try {
      setLoading(true)
      clearError()
      const newEquipment = await equipmentService.createEquipment(equipmentData)
      equipmentItems.value.push(newEquipment)
      return newEquipment
    } catch (err) {
      setError('장비 생성에 실패했습니다.')
      console.error('장비 생성 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  const updateEquipment = async (id, equipmentData) => {
    try {
      setLoading(true)
      clearError()
      const updatedEquipment = await equipmentService.updateEquipment(id, equipmentData)
      const index = equipmentItems.value.findIndex(equipment => equipment.id === id)
      if (index !== -1) {
        equipmentItems.value[index] = updatedEquipment
      }
      return updatedEquipment
    } catch (err) {
      setError('장비 수정에 실패했습니다.')
      console.error('장비 수정 실패:', err)
      throw err
    } finally {
      setLoading(false)
    }
  }

  const deleteEquipment = async (id) => {
    try {
      setLoading(true)
      clearError()
      await equipmentService.deleteEquipment(id)
      equipmentItems.value = equipmentItems.value.filter(equipment => equipment.id !== id)
    } catch (err) {
      setError('장비 삭제에 실패했습니다.')
      console.error('장비 삭제 실패:', err)
      throw err
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
    addVideo,
    updateVideo,
    deleteVideo,
    loadMusic,
    addMusic,
    updateMusic,
    deleteMusic,
    loadNews,
    addNews,
    updateNews,
    deleteNews,
    loadEquipment,
    addEquipment,
    updateEquipment,
    deleteEquipment,
    loadAllMedia,
    setLoading,
    setError,
    clearError
  }
})
