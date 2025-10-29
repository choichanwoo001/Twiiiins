<template>
  <div class="form-section">
    <h2>{{ editingItem ? `${title} 수정` : `새 ${title} 등록` }}</h2>
    <form @submit.prevent="handleSubmit" class="crud-form">
      <div 
        v-for="field in fields" 
        :key="field.key" 
        :class="field.row ? 'form-row' : 'form-group'"
      >
        <div 
          v-for="subField in field.row ? field.fields : [field]" 
          :key="subField.key"
          class="form-group"
        >
          <label>{{ subField.label }}<span v-if="subField.required"> *</span></label>
          
          <!-- 텍스트 입력 -->
          <input 
            v-if="subField.type === 'text' || subField.type === 'number'"
            v-model="formValues[subField.key]"
            :type="subField.type"
            :placeholder="subField.placeholder"
            :required="subField.required"
            :min="subField.min"
          />
          
          <!-- 파일 업로드 -->
          <div v-else-if="subField.type === 'file'" class="file-upload-container">
            <input 
              type="file" 
              :ref="`fileInput-${subField.key}`"
              @change="handleFileUpload(subField.key, $event)" 
              :accept="subField.accept"
              style="display: none"
            />
            <button type="button" class="btn-upload" @click="$refs[`fileInput-${subField.key}`][0].click()">
              파일 선택
            </button>
            <span v-if="formValues[subField.key]" class="file-name">{{ formValues[subField.key] }}</span>
          </div>
          
          <!-- 텍스트 영역 -->
          <textarea 
            v-else-if="subField.type === 'textarea'"
            v-model="formValues[subField.key]"
            :placeholder="subField.placeholder"
            :required="subField.required"
            :rows="subField.rows || 3"
          ></textarea>
        </div>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-save">
          {{ editingItem ? '수정' : '등록' }}
        </button>
        <button 
          type="button" 
          class="btn-cancel" 
          @click="handleCancel" 
          v-if="editingItem"
        >
          취소
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

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
  editingItem: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'submit', 'cancel'])

const formValues = ref({ ...props.modelValue })

// 부모의 modelValue가 변경되면 로컬 값도 업데이트
watch(() => props.modelValue, (newValue) => {
  formValues.value = { ...newValue }
}, { deep: true })

// 로컬 값이 변경되면 부모에게 알림
watch(formValues, (newValue) => {
  emit('update:modelValue', newValue)
}, { deep: true })

const handleSubmit = () => {
  emit('submit', formValues.value)
}

const handleCancel = () => {
  emit('cancel')
}

const handleFileUpload = (fieldKey, event) => {
  const file = event.target.files[0]
  if (file) {
    formValues.value[fieldKey] = file.name
    // 실제 파일 업로드 로직은 부모 컴포넌트에서 처리
  }
}
</script>

<style scoped>
.form-section {
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.form-section h2 {
  margin-bottom: 1.5rem;
  color: #333;
}

.crud-form {
  max-width: 50rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.form-group label {
  font-weight: 500;
  color: #555;
}

.form-group input,
.form-group textarea {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 0.25rem;
  font-size: 1rem;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.file-upload-container {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.btn-upload {
  padding: 0.5rem 1rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 0.25rem;
  cursor: pointer;
  font-weight: 500;
}

.btn-upload:hover {
  background: #0056b3;
}

.file-name {
  color: #666;
  font-size: 0.9rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.btn-save {
  padding: 0.75rem 2rem;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save:hover {
  background: #229954;
}

.btn-cancel {
  padding: 0.75rem 2rem;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background: #7f8c8d;
}
</style>
