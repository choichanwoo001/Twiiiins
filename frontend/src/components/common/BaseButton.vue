<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="buttonClasses"
    @click="handleClick"
    v-bind="$attrs"
  >
    <span v-if="loading" class="button-loading">
      <svg class="animate-spin h-4 w-4" viewBox="0 0 24 24">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
      </svg>
    </span>
    <span v-else-if="$slots.icon" class="button-icon">
      <slot name="icon" />
    </span>
    <span v-if="$slots.default" class="button-text">
      <slot />
    </span>
  </button>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'danger', 'success', 'warning', 'ghost'].includes(value)
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'button'
  },
  fullWidth: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['click'])

// Computed
const buttonClasses = computed(() => {
  const baseClasses = [
    'base-button',
    `base-button--${props.variant}`,
    `base-button--${props.size}`,
    {
      'base-button--disabled': props.disabled || props.loading,
      'base-button--loading': props.loading,
      'base-button--full-width': props.fullWidth
    }
  ]
  return baseClasses
})

// Methods
const handleClick = (event) => {
  if (!props.disabled && !props.loading) {
    emit('click', event)
  }
}
</script>

<style scoped>
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  font-weight: 500;
  transition: all 0.2s;
  border: none;
  cursor: pointer;
  text-decoration: none;
  font-size: 0.875rem;
  line-height: 1.25rem;
}

.base-button:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.5);
}

/* Variants */
.base-button--primary {
  background-color: #2563eb;
  color: white;
}

.base-button--primary:hover {
  background-color: #1d4ed8;
}

.base-button--secondary {
  background-color: #e5e7eb;
  color: #111827;
}

.base-button--secondary:hover {
  background-color: #d1d5db;
}

.base-button--danger {
  background-color: #dc2626;
  color: white;
}

.base-button--danger:hover {
  background-color: #b91c1c;
}

.base-button--success {
  background-color: #16a34a;
  color: white;
}

.base-button--success:hover {
  background-color: #15803d;
}

.base-button--warning {
  background-color: #ca8a04;
  color: white;
}

.base-button--warning:hover {
  background-color: #a16207;
}

.base-button--ghost {
  background-color: transparent;
  color: #374151;
}

.base-button--ghost:hover {
  background-color: #f3f4f6;
}

/* Sizes */
.base-button--small {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
  line-height: 1rem;
}

.base-button--medium {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
  line-height: 1.25rem;
}

.base-button--large {
  padding: 0.75rem 1.5rem;
  font-size: 1.125rem;
  line-height: 1.75rem;
}

/* States */
.base-button--disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.base-button--loading {
  cursor: wait;
}

.base-button--full-width {
  width: 100%;
}

.button-loading {
  display: flex;
  align-items: center;
}

.button-icon {
  display: flex;
  align-items: center;
}

.button-text {
  display: flex;
  align-items: center;
}

/* Animation */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
