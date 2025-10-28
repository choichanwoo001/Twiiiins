<template>
  <div class="lazy-image-container" :style="{ width: width, height: height }">
    <img
      v-if="isLoaded"
      :src="src"
      :alt="alt"
      :class="imageClass"
      @load="onLoad"
      @error="onError"
    />
    <div
      v-else
      class="lazy-placeholder"
      :class="placeholderClass"
      :style="{ width: width, height: height }"
    >
      <div class="loading-spinner" v-if="isLoading">
        <div class="spinner"></div>
      </div>
      <div class="error-placeholder" v-else-if="hasError">
        <span>이미지 로드 실패</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'

// Props
const props = defineProps({
  src: {
    type: String,
    required: true
  },
  alt: {
    type: String,
    default: ''
  },
  width: {
    type: String,
    default: '100%'
  },
  height: {
    type: String,
    default: 'auto'
  },
  imageClass: {
    type: String,
    default: ''
  },
  placeholderClass: {
    type: String,
    default: ''
  },
  rootMargin: {
    type: String,
    default: '50px'
  },
  threshold: {
    type: Number,
    default: 0.1
  }
})

// Reactive data
const isLoaded = ref(false)
const isLoading = ref(false)
const hasError = ref(false)
const observer = ref(null)
const containerRef = ref(null)

// Methods
const onLoad = () => {
  isLoaded.value = true
  isLoading.value = false
  hasError.value = false
}

const onError = () => {
  hasError.value = true
  isLoading.value = false
  isLoaded.value = false
}

const loadImage = () => {
  if (isLoaded.value || isLoading.value) return
  
  isLoading.value = true
  hasError.value = false
  
  // 이미지 로드 시도
  const img = new Image()
  img.onload = () => {
    nextTick(() => {
      onLoad()
    })
  }
  img.onerror = () => {
    nextTick(() => {
      onError()
    })
  }
  img.src = props.src
}

const setupIntersectionObserver = () => {
  if (!containerRef.value) return
  
  const options = {
    root: null,
    rootMargin: props.rootMargin,
    threshold: props.threshold
  }
  
  observer.value = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        loadImage()
        if (observer.value) {
          observer.value.unobserve(entry.target)
        }
      }
    })
  }, options)
  
  observer.value.observe(containerRef.value)
}

// Lifecycle
onMounted(() => {
  containerRef.value = document.querySelector('.lazy-image-container')
  setupIntersectionObserver()
})

onUnmounted(() => {
  if (observer.value) {
    observer.value.disconnect()
  }
})
</script>

<style scoped>
.lazy-image-container {
  position: relative;
  overflow: hidden;
}

.lazy-image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s ease;
}

.lazy-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 0.25rem;
  color: #6c757d;
  font-size: 0.875rem;
}

.loading-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 2rem;
  height: 2rem;
  border: 2px solid #e9ecef;
  border-top: 2px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #943C31;
  font-size: 0.75rem;
  text-align: center;
  padding: 0.5rem;
}
</style>
