<template>
  <div class="order-success container">
    <div class="success-content">
      <div class="success-icon">✓</div>
      <h1>주문이 완료되었습니다!</h1>
      <p>주문해 주셔서 감사합니다.</p>
      <p v-if="orderId">주문번호: {{ orderId }}</p>
      <router-link to="/" class="btn btn-primary">홈으로 돌아가기</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const cartStore = useCartStore()
const orderId = ref(null)

onMounted(() => {
  orderId.value = route.query.orderId
  // 결제 완료 후 장바구니 비우기
  cartStore.clearCart()
})
</script>

<style scoped>
.order-success {
  padding: 4rem 0;
  text-align: center;
}

.success-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 3rem;
  background: #f9f9f9;
  border-radius: 8px;
}

.success-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 2rem;
  background: #0a0;
  color: #fff;
  font-size: 4rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

h1 {
  font-size: 2rem;
  margin-bottom: 1rem;
}

p {
  font-size: 1.1rem;
  margin-bottom: 1rem;
}

.btn {
  margin-top: 2rem;
}
</style>

