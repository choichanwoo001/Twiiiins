<template>
  <div class="input-wrapper">
    <label
      v-if="label"
      :for="inputId"
      :class="labelClasses"
    >
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    
    <div class="input-container">
      <div v-if="$slots.prefix" class="input-prefix">
        <slot name="prefix" />
      </div>
      
      <input
        :id="inputId"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :required="required"
        :class="inputClasses"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
        v-bind="$attrs"
      />
      
      <div v-if="$slots.suffix" class="input-suffix">
        <slot name="suffix" />
      </div>
    </div>
    
    <div v-if="error || helpText" class="input-message">
      <p v-if="error" class="text-red-600 text-sm">
        {{ error }}
      </p>
      <p v-else-if="helpText" class="text-gray-500 text-sm">
        {{ helpText }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'

// Props
const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  label: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  },
  error: {
    type: String,
    default: ''
  },
  helpText: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  readonly: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'blur', 'focus'])

// Reactive data
const inputId = ref('')

// Computed
const labelClasses = computed(() => [
  'block text-sm font-medium text-gray-700 mb-1',
  {
    'text-red-700': props.error
  }
])

const inputClasses = computed(() => [
  'w-full border rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-offset-0',
  {
    // Size variants
    'px-3 py-1.5 text-sm': props.size === 'small',
    'px-3 py-2 text-base': props.size === 'medium',
    'px-4 py-3 text-lg': props.size === 'large',
    
    // State variants
    'border-gray-300 focus:border-blue-500 focus:ring-blue-500': !props.error && !props.disabled,
    'border-red-300 focus:border-red-500 focus:ring-red-500': props.error,
    'bg-gray-50 border-gray-200 text-gray-500 cursor-not-allowed': props.disabled,
    'bg-gray-50 border-gray-200': props.readonly,
    
    // Prefix/Suffix padding
    'pl-10': props.$slots?.prefix,
    'pr-10': props.$slots?.suffix
  }
])

// Methods
const handleInput = (event) => {
  emit('update:modelValue', event.target.value)
}

const handleBlur = (event) => {
  emit('blur', event)
}

const handleFocus = (event) => {
  emit('focus', event)
}

// Lifecycle
onMounted(() => {
  inputId.value = `input-${Math.random().toString(36).substr(2, 9)}`
})
</script>

<style scoped>
.input-wrapper {
  @apply w-full;
}

.input-container {
  @apply relative;
}

.input-prefix {
  @apply absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400;
}

.input-suffix {
  @apply absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400;
}

.input-message {
  @apply mt-1;
}
</style>
