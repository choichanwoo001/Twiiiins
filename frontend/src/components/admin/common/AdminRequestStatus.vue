<template>
  <section v-if="status.items.length" class="admin-request-status" aria-label="관리자 작업 상태">
    <div v-for="item in status.items" :key="item.id" :class="['operation', item.state]" :aria-busy="item.state === 'pending'">
      <p :role="item.state === 'failure' ? 'alert' : 'status'" aria-atomic="true">
        <strong>{{ item.label }} · {{ labels[item.state] }}</strong>
        <span v-if="item.detail">{{ item.detail }}</span>
      </p>
      <button v-if="item.state !== 'pending'" type="button" :aria-label="`${item.label} 알림 닫기`" @click="dismiss(item.id)">닫기</button>
    </div>
  </section>
</template>
<script setup>
import { adminRequestStatus as status, dismissAdminRequest as dismiss } from '../../../composables/useAdminRequestStatus'
const labels = { pending: '처리 중…', success: '성공', failure: '실패', cancelled: '취소됨' }
</script>
<style scoped>
.admin-request-status { position:sticky;top:0;z-index:20;margin:.75rem;max-height:35vh;overflow:auto; }
.operation { display:flex;align-items:start;justify-content:space-between;gap:1rem;padding:.75rem 1rem;border:1px solid #536e87;background:#eef5fb;color:#173550; }
.operation + .operation { margin-top:.25rem; }
.operation.success { background:#eef8f1;border-color:#427653;color:#184629; }
.operation.failure { background:#fff0ef;border-color:#a44138;color:#79251e; }
.operation.cancelled { background:#f4f4f4;border-color:#666;color:#333; }
p { margin:0;min-width:0;overflow-wrap:anywhere; }span { display:block;margin-top:.25rem; }
button { flex-shrink:0;background:white;color:inherit;border:1px solid currentColor;padding:.25rem .5rem;cursor:pointer; }
</style>
