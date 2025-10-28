<template>
  <div class="data-list">
    <h2>{{ title }}</h2>
    <div class="data-table">
      <table role="table" :aria-label="`${title} 데이터 테이블`">
        <thead>
          <tr>
            <th v-for="column in columns" :key="column.key" scope="col">
              {{ column.label }}
            </th>
            <th v-if="actions.length > 0" scope="col">작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in data" :key="getItemKey(item)">
            <td v-for="column in columns" :key="column.key">
              <slot 
                :name="`cell-${column.key}`" 
                :item="item" 
                :value="getColumnValue(item, column.key)"
                :index="index"
              >
                {{ getColumnValue(item, column.key) }}
              </slot>
            </td>
            <td v-if="actions.length > 0">
              <button
                v-for="action in actions"
                :key="action.key"
                :class="action.class"
                @click="handleAction(action.key, item)"
                :aria-label="`${getItemName(item)} ${action.label}`"
              >
                {{ action.label }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  title: {
    type: String,
    default: '전체 목록'
  },
  data: {
    type: Array,
    required: true
  },
  columns: {
    type: Array,
    required: true
  },
  actions: {
    type: Array,
    default: () => []
  },
  itemKey: {
    type: String,
    default: 'id'
  }
})

const emit = defineEmits(['action'])

const getItemKey = (item) => {
  return item[props.itemKey] || item.id
}

const getColumnValue = (item, key) => {
  return item[key]
}

const getItemName = (item) => {
  // 아이템의 이름을 가져오는 함수 (title, name, 또는 첫 번째 컬럼 값)
  return item.title || item.name || item[Object.keys(item)[0]] || '항목'
}

const handleAction = (actionKey, item) => {
  emit('action', actionKey, item)
}
</script>

<style scoped>
.data-list {
  margin-bottom: 2rem;
  background: white;
  padding: 2rem;
  border-radius: 0.5rem;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.data-list h2 {
  margin-bottom: 1rem;
  color: #333;
}

.data-table {
  overflow-x: auto;
}

.data-table table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 0.5rem;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.data-table th,
.data-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
}

.data-table tr:hover {
  background: #f8f9fa;
}

/* 액션 버튼 스타일 */
button {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.25rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  margin-right: 0.5rem;
}

.btn-edit {
  background: #f39c12;
  color: white;
}

.btn-edit:hover {
  background: #e67e22;
}

.btn-delete {
  background: #943C31;
  color: white;
}

.btn-delete:hover {
  background: #7a2f26;
}

.btn-manage {
  background: #17a2b8;
  color: white;
}

.btn-manage:hover {
  background: #138496;
}
</style>
