<template>
  <form @submit.prevent="handleSubmit" class="base-form">
    <div class="form-header">
      <h3>{{ title }}</h3>
      <button 
        v-if="showClose" 
        type="button" 
        class="btn-close" 
        @click="$emit('close')"
        aria-label="닫기"
      >
        ×
      </button>
    </div>

    <div class="form-body">
      <div 
        v-for="field in fields" 
        :key="field.key" 
        class="form-group"
        :class="{ 'has-error': errors[field.key] }"
      >
        <label :for="field.key" class="form-label">
          {{ field.label }}
          <span v-if="field.required" class="required">*</span>
        </label>
        
        <input
          v-if="field.type === 'text' || field.type === 'email' || field.type === 'password'"
          :id="field.key"
          :type="field.type"
          :placeholder="field.placeholder"
          :required="field.required"
          :min="field.min"
          :max="field.max"
          :minlength="field.minLength"
          :maxlength="field.maxLength"
          v-model="formData[field.key]"
          class="form-input"
          :class="{ 'error': errors[field.key] }"
        />
        
        <textarea
          v-else-if="field.type === 'textarea'"
          :id="field.key"
          :placeholder="field.placeholder"
          :required="field.required"
          :minlength="field.minLength"
          :maxlength="field.maxLength"
          :rows="field.rows || 3"
          v-model="formData[field.key]"
          class="form-textarea"
          :class="{ 'error': errors[field.key] }"
        ></textarea>
        
        <input
          v-else-if="field.type === 'number'"
          :id="field.key"
          type="number"
          :placeholder="field.placeholder"
          :required="field.required"
          :min="field.min"
          :max="field.max"
          :step="field.step"
          v-model.number="formData[field.key]"
          class="form-input"
          :class="{ 'error': errors[field.key] }"
        />
        
        <input
          v-else-if="field.type === 'date'"
          :id="field.key"
          type="date"
          :required="field.required"
          v-model="formData[field.key]"
          class="form-input"
          :class="{ 'error': errors[field.key] }"
        />
        
        <select
          v-else-if="field.type === 'select'"
          :id="field.key"
          :required="field.required"
          v-model="formData[field.key]"
          class="form-select"
          :class="{ 'error': errors[field.key] }"
        >
          <option value="">{{ field.placeholder || '선택하세요' }}</option>
          <option 
            v-for="option in field.options" 
            :key="option.value" 
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
        
        <div v-if="field.type === 'checkbox'" class="checkbox-group">
          <input
            :id="field.key"
            type="checkbox"
            v-model="formData[field.key]"
            class="form-checkbox"
          />
          <label :for="field.key" class="checkbox-label">
            {{ field.label }}
          </label>
        </div>

        <div v-if="errors[field.key]" class="error-message">
          {{ errors[field.key] }}
        </div>
      </div>
    </div>

    <div class="form-footer">
      <button 
        type="button" 
        class="btn btn-secondary" 
        @click="handleCancel"
        :disabled="isLoading"
      >
        취소
      </button>
      <button 
        type="submit" 
        class="btn btn-primary" 
        :disabled="isLoading || !isValid"
      >
        <span v-if="isLoading" class="loading-spinner"></span>
        {{ submitText }}
      </button>
    </div>
  </form>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { validateForm } from '../../utils/crudHelpers'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  fields: {
    type: Array,
    required: true
  },
  modelValue: {
    type: Object,
    required: true
  },
  submitText: {
    type: String,
    default: '저장'
  },
  showClose: {
    type: Boolean,
    default: false
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  validationRules: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'submit', 'cancel', 'close'])

// 폼 데이터
const formData = ref({ ...props.modelValue })
const errors = ref({})

// 유효성 검사 규칙 생성
const validationRules = computed(() => {
  const rules = {}
  props.fields.forEach(field => {
    if (field.required || field.minLength || field.maxLength || field.pattern) {
      rules[field.key] = {
        label: field.label,
        required: field.required,
        minLength: field.minLength,
        maxLength: field.maxLength,
        pattern: field.pattern,
        message: field.errorMessage
      }
    }
  })
  return { ...rules, ...props.validationRules }
})

// 폼 유효성 검사
const isValid = computed(() => {
  const { isValid: formValid } = validateForm(formData.value, validationRules.value)
  return formValid
})

// 모델 값 변경 감지
watch(() => props.modelValue, (newValue) => {
  formData.value = { ...newValue }
}, { deep: true })

// 폼 데이터 변경 감지
watch(formData, (newValue) => {
  emit('update:modelValue', newValue)
  // 에러 클리어
  if (Object.keys(errors.value).length > 0) {
    errors.value = {}
  }
}, { deep: true })

// 폼 제출
const handleSubmit = () => {
  const { errors: validationErrors, isValid: formValid } = validateForm(formData.value, validationRules.value)
  
  if (!formValid) {
    errors.value = validationErrors
    return
  }

  emit('submit', formData.value)
}

// 취소
const handleCancel = () => {
  emit('cancel')
}

// 초기화
onMounted(() => {
  formData.value = { ...props.modelValue }
})
</script>

<style scoped>
.base-form {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.form-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
  padding: 4px;
  line-height: 1;
}

.btn-close:hover {
  color: #374151;
}

.form-group {
  margin-bottom: 20px;
}

.form-group.has-error .form-input,
.form-group.has-error .form-textarea,
.form-group.has-error .form-select {
  border-color: #943C31;
}

.form-label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  color: #374151;
}

.required {
  color: #943C31;
  margin-left: 2px;
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.error,
.form-textarea.error,
.form-select.error {
  border-color: #943C31;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-checkbox {
  width: 16px;
  height: 16px;
}

.checkbox-label {
  margin: 0;
  font-weight: 500;
  color: #374151;
}

.error-message {
  margin-top: 4px;
  font-size: 12px;
  color: #943C31;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: white;
  border-color: #d1d5db;
  color: #374151;
}

.btn-secondary:hover:not(:disabled) {
  background: #f9fafb;
}

.btn-primary {
  background: #3b82f6;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2563eb;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
