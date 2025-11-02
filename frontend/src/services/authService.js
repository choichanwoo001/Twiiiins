import apiClient from '../api/axios'
import { useAppStore } from '../stores'

/**
 * 로그인
 * @param {string} username - 사용자 아이디
 * @param {string} password - 비밀번호
 * @returns {Promise} 로그인 응답
 */
export const login = async (username, password) => {
  try {
    const response = await apiClient.post('/auth/login', {
      username,
      password
    })
    
    if (response.data.success) {
      const userData = response.data.data
      const appStore = useAppStore()
      
      // 사용자 정보 저장
      appStore.setUser(userData)
      localStorage.setItem('user', JSON.stringify(userData))
      
      return response.data
    } else {
      throw new Error(response.data.error?.message || '로그인에 실패했습니다.')
    }
  } catch (error) {
    throw error
  }
}

/**
 * 로그아웃
 */
export const logout = () => {
  const appStore = useAppStore()
  appStore.logout()
  localStorage.removeItem('user')
}

