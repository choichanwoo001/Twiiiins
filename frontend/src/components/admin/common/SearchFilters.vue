<template>
  <div class="search-section">
    <div class="search-filters">
      <div 
        v-for="filter in filters" 
        :key="filter.key" 
        class="filter-group"
      >
        <label>{{ filter.label }}</label>
        <input 
          v-model="filterValues[filter.key]" 
          :placeholder="filter.placeholder" 
          :type="filter.type || 'text'"
        />
      </div>
      <div class="filter-actions">
        <button class="btn-reset" @click="handleReset">초기화</button>
        <button class="btn-search" @click="handleSearch">검색</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  filters: {
    type: Array,
    required: true
  },
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])

const filterValues = ref({ ...props.modelValue })

// 부모의 modelValue가 변경되면 로컬 값도 업데이트 (실제 변경사항이 있을 때만)
watch(() => props.modelValue, (newValue) => {
  try {
    const currentStr = JSON.stringify(filterValues.value) || '{}'
    const newStr = JSON.stringify(newValue) || '{}'
    if (currentStr !== newStr) {
      filterValues.value = { ...newValue }
    }
  } catch (e) {
    // JSON 변환 실패 시 안전하게 처리
    filterValues.value = { ...newValue }
  }
}, { deep: true })

// 로컬 값이 변경되면 부모에게 알림 (부모 값과 다를 때만)
watch(filterValues, (newValue) => {
  try {
    const currentStr = JSON.stringify(props.modelValue) || '{}'
    const newStr = JSON.stringify(newValue) || '{}'
    if (currentStr !== newStr) {
      emit('update:modelValue', { ...newValue })
    }
  } catch (e) {
    // JSON 변환 실패 시 안전하게 처리
    emit('update:modelValue', { ...newValue })
  }
}, { deep: true })

const handleSearch = () => {
  emit('search', filterValues.value)
}

const handleReset = () => {
  const resetValues = {}
  props.filters.forEach(filter => {
    resetValues[filter.key] = filter.defaultValue || ''
  })
  filterValues.value = resetValues
  emit('reset', resetValues)
}
</script>

<style scoped>
.search-section {
  background: white;
  padding: 1.5rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  margin-bottom: 2rem;
}

.search-filters {
  display: flex;
  gap: 1rem;
  align-items: end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group label {
  font-weight: 500;
  color: #555;
}

.filter-group input {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 0.25rem;
  font-size: 0.9rem;
}

.filter-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-reset, .btn-search {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-reset {
  background: #95a5a6;
  color: white;
}

.btn-reset:hover {
  background: #7f8c8d;
}

.btn-search {
  background: #3498db;
  color: white;
}

.btn-search:hover {
  background: #2980b9;
}
</style>
