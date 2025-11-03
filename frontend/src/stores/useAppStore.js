import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 상태
  const isLoading = ref(false)
  const error = ref(null)
  const user = ref(null)
  const theme = ref('light')
  const isUploading = ref(false)
  const uploadProgress = ref(0)

  // 게터
  const isAuthenticated = computed(() => !!user.value)
  const hasError = computed(() => !!error.value)

  // 액션
  const setLoading = (loading) => {
    isLoading.value = loading
  }

  const setError = (errorMessage) => {
    error.value = errorMessage
  }

  const showError = (errorMessage) => {
    error.value = errorMessage
    // 5초 후 자동으로 에러 메시지 제거
    setTimeout(() => {
      clearError()
    }, 5000)
  }

  const clearError = () => {
    error.value = null
  }

  const setUser = (userData) => {
    user.value = userData
  }

  const logout = () => {
    user.value = null
    localStorage.removeItem('token')
  }

  const setTheme = (newTheme) => {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
  }

  const toggleTheme = () => {
    setTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  const setUploading = (uploading) => {
    isUploading.value = uploading
    if (!uploading) {
      uploadProgress.value = 0
    }
  }

  const setUploadProgress = (progress) => {
    uploadProgress.value = progress
  }

  // 초기화
  const initialize = () => {
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
      theme.value = savedTheme
    }
  }

  return {
    // 상태
    isLoading,
    error,
    user,
    theme,
    isUploading,
    uploadProgress,
    // 게터
    isAuthenticated,
    hasError,
    // 액션
    setLoading,
    setError,
    showError,
    clearError,
    setUser,
    logout,
    setTheme,
    toggleTheme,
    setUploading,
    setUploadProgress,
    initialize
  }
})
