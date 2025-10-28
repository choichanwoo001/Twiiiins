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
  @apply inline-flex items-center justify-center gap-2 px-4 py-2 rounded-md font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2;
}

/* Variants */
.base-button--primary {
  @apply bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500;
}

.base-button--secondary {
  @apply bg-gray-200 text-gray-900 hover:bg-gray-300 focus:ring-gray-500;
}

.base-button--danger {
  @apply bg-red-600 text-white hover:bg-red-700 focus:ring-red-500;
}

.base-button--success {
  @apply bg-green-600 text-white hover:bg-green-700 focus:ring-green-500;
}

.base-button--warning {
  @apply bg-yellow-600 text-white hover:bg-yellow-700 focus:ring-yellow-500;
}

.base-button--ghost {
  @apply bg-transparent text-gray-700 hover:bg-gray-100 focus:ring-gray-500;
}

/* Sizes */
.base-button--small {
  @apply px-3 py-1.5 text-sm;
}

.base-button--medium {
  @apply px-4 py-2 text-base;
}

.base-button--large {
  @apply px-6 py-3 text-lg;
}

/* States */
.base-button--disabled {
  @apply opacity-50 cursor-not-allowed;
}

.base-button--loading {
  @apply cursor-wait;
}

.base-button--full-width {
  @apply w-full;
}

.button-loading {
  @apply flex items-center;
}

.button-icon {
  @apply flex items-center;
}

.button-text {
  @apply flex items-center;
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
