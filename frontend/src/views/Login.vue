<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="login-title">Admin Login</h1>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="username">Username</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            placeholder="Enter username"
            required
            autocomplete="username"
          />
        </div>
        
        <div class="form-group">
          <label for="password">Password</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="Enter password"
            required
            autocomplete="current-password"
          />
        </div>
        
        <div v-if="error" class="error-message">
          {{ error }}
        </div>
        
        <button type="submit" class="login-button" :disabled="isLoading">
          {{ isLoading ? 'Logging in...' : 'Login' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../services/authService'
import { useAppStore } from '../stores'

const router = useRouter()
const appStore = useAppStore()

const form = ref({
  username: '',
  password: ''
})

const error = ref(null)
const isLoading = ref(false)

const handleLogin = async () => {
  error.value = null
  isLoading.value = true
  
  try {
    await login(form.value.username, form.value.password)
    router.push('/admin')
  } catch (err) {
    // Extract error message
    let errorMessage = 'Login failed.'
    
    if (err.message) {
      errorMessage = err.message
    } else if (err.response?.data?.error?.message) {
      errorMessage = err.response.data.error.message
    } else if (err.response?.data?.message) {
      errorMessage = err.response.data.message
    } else if (typeof err === 'string') {
      errorMessage = err
    }
    
    error.value = errorMessage
    console.error('Login error:', err)
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f8f9fa;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  background-color: var(--color-background); /* white */
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--color-text); /* #1E1D1D */
  font-size: 2rem;
  font-weight: 600;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-text-secondary); /* #555 */
}

.form-input {
  padding: 0.75rem;
  border: 1px solid var(--color-border); /* #ddd -> #e0e0e0 (var(--color-border)) */
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-text); /* #1E1D1D */
}

.error-message {
  padding: 0.75rem;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 4px;
  color: #c33;
  font-size: 0.9rem;
}

.login-button {
  padding: 0.875rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.login-button:hover:not(:disabled) {
  background: #0056b3;
}

.login-button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>

