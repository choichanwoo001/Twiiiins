<template>
  <Teleport to="body">
    <Transition name="modal" appear>
      <div
        v-if="isOpen"
        class="modal-overlay"
        @click="handleOverlayClick"
      >
        <div
          :class="modalClasses"
          @click.stop
          role="dialog"
          :aria-labelledby="titleId"
          :aria-describedby="descriptionId"
          aria-modal="true"
        >
          <!-- Header -->
          <div v-if="showHeader" class="modal-header">
            <h2 :id="titleId" class="modal-title">
              <slot name="title">{{ title }}</slot>
            </h2>
            <button
              v-if="closable"
              @click="closeModal"
              class="modal-close"
              aria-label="모달 닫기"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>

          <!-- Body -->
          <div class="modal-body">
            <slot />
          </div>

          <!-- Footer -->
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'

// Props
const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large', 'full'].includes(value)
  },
  closable: {
    type: Boolean,
    default: true
  },
  closeOnOverlay: {
    type: Boolean,
    default: true
  },
  showHeader: {
    type: Boolean,
    default: true
  },
  persistent: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['close', 'update:isOpen'])

// Reactive data
const titleId = ref('')
const descriptionId = ref('')

// Computed
const modalClasses = computed(() => [
  'modal-content',
  `modal-content--${props.size}`
])

// Methods
const closeModal = () => {
  if (!props.persistent) {
    emit('close')
    emit('update:isOpen', false)
  }
}

const handleOverlayClick = () => {
  if (props.closeOnOverlay && !props.persistent) {
    closeModal()
  }
}

const handleEscape = (event) => {
  if (event.key === 'Escape' && props.isOpen && !props.persistent) {
    closeModal()
  }
}

// Lifecycle
onMounted(() => {
  titleId.value = `modal-title-${Math.random().toString(36).substr(2, 9)}`
  descriptionId.value = `modal-description-${Math.random().toString(36).substr(2, 9)}`
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
})

// Watch for body scroll lock
watch(() => props.isOpen, (newValue) => {
  nextTick(() => {
    if (newValue) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
  })
})
</script>

<style scoped>
.modal-overlay {
  @apply fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50;
}

.modal-content {
  @apply bg-white rounded-lg shadow-xl max-h-[90vh] overflow-hidden;
}

.modal-content--small {
  @apply w-full max-w-md;
}

.modal-content--medium {
  @apply w-full max-w-lg;
}

.modal-content--large {
  @apply w-full max-w-2xl;
}

.modal-content--full {
  @apply w-full max-w-4xl;
}

.modal-header {
  @apply flex items-center justify-between p-6 border-b border-gray-200;
}

.modal-title {
  @apply text-lg font-semibold text-gray-900;
}

.modal-close {
  @apply p-1 text-gray-400 hover:text-gray-600 transition-colors;
}

.modal-body {
  @apply p-6 overflow-y-auto;
}

.modal-footer {
  @apply flex items-center justify-end gap-3 p-6 border-t border-gray-200 bg-gray-50;
}

/* Transitions */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-content,
.modal-leave-to .modal-content {
  transform: scale(0.9) translateY(-20px);
}

.modal-enter-to .modal-content,
.modal-leave-from .modal-content {
  transform: scale(1) translateY(0);
}
</style>
