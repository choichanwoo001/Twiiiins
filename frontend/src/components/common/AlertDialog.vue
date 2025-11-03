<template>
  <div v-if="isVisible" class="dialog-overlay" @click="handleOverlayClick">
    <div class="dialog-content" @click.stop>
      <div class="dialog-header">
        <h3>{{ title }}</h3>
      </div>
      
      <div class="dialog-body">
        <p>{{ message }}</p>
      </div>
      
      <div class="dialog-footer">
        <BaseButton :variant="buttonVariant" @click="handleClose">{{ buttonText }}</BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import { BaseButton } from './'

const props = defineProps({
  isVisible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '알림'
  },
  message: {
    type: String,
    required: true
  },
  buttonText: {
    type: String,
    default: '확인'
  },
  buttonVariant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'danger', 'warning', 'success', 'secondary'].includes(value)
  },
  closeOnOverlay: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close'])

const handleClose = () => {
  emit('close')
}

const handleOverlayClick = () => {
  if (props.closeOnOverlay) {
    handleClose()
  }
}
</script>

<style scoped>
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.dialog-content {
  background: white;
  border-radius: 0.5rem;
  padding: 2rem;
  max-width: 90vw;
  width: 400px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.dialog-header {
  margin-bottom: 1.5rem;
}

.dialog-header h3 {
  margin: 0;
  color: #1E1D1D;
  font-size: 1.25rem;
  font-weight: 600;
}

.dialog-body {
  margin-bottom: 2rem;
}

.dialog-body p {
  margin: 0;
  color: #555;
  font-size: 1rem;
  line-height: 1.5;
  white-space: pre-line;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}
</style>

