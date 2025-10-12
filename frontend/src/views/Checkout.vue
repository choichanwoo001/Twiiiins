<template>
  <div class="checkout container">
    <h1>결제</h1>

    <div class="checkout-layout">
      <!-- 주문 정보 입력 폼 -->
      <div class="checkout-form">
        <h2>배송 정보</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>이메일</label>
            <input type="email" v-model="form.email" required>
          </div>
          
          <div class="form-group">
            <label>이름</label>
            <input type="text" v-model="form.name" required>
          </div>
          
          <div class="form-group">
            <label>전화번호</label>
            <input type="tel" v-model="form.phone">
          </div>
          
          <div class="form-group">
            <label>배송 주소</label>
            <textarea v-model="form.shippingAddress" rows="3" required></textarea>
          </div>

          <button type="submit" class="btn btn-primary btn-large" :disabled="loading">
            {{ loading ? '처리 중...' : '결제 진행' }}
          </button>
        </form>

        <p v-if="error" class="error">{{ error }}</p>
      </div>

      <!-- 주문 요약 -->
      <div class="order-summary">
        <h2>주문 상품</h2>
        <div v-for="item in cartStore.items" :key="item.product.id" class="summary-item">
          <span>{{ item.product.name }} x {{ item.quantity }}</span>
          <span>€{{ (item.product.price * item.quantity).toFixed(2) }}</span>
        </div>
        <div class="summary-total">
          <span>총 결제 금액</span>
          <span>€{{ cartStore.totalAmount.toFixed(2) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import apiClient from '../api/axios'
import { loadStripe } from '@stripe/stripe-js'

const router = useRouter()
const cartStore = useCartStore()

const form = ref({
  email: '',
  name: '',
  phone: '',
  shippingAddress: ''
})

const loading = ref(false)
const error = ref('')

const handleSubmit = async () => {
  loading.value = true
  error.value = ''

  try {
    // 1. 주문 생성
    const orderData = {
      items: cartStore.items.map(item => ({
        productId: item.product.id,
        quantity: item.quantity
      })),
      email: form.value.email,
      name: form.value.name,
      phone: form.value.phone,
      shippingAddress: form.value.shippingAddress
    }

    const orderRes = await apiClient.post('/orders/guest', orderData)
    const order = orderRes.data

    // 2. Stripe Checkout Session 생성
    const checkoutRes = await apiClient.post('/stripe/create-checkout-session', {
      orderId: order.id,
      successUrl: `${window.location.origin}/order-success?orderId=${order.id}`,
      cancelUrl: `${window.location.origin}/cart`
    })

    // 3. Stripe Checkout으로 리다이렉트
    const stripe = await loadStripe(import.meta.env.VITE_STRIPE_PUBLIC_KEY)
    const { error: stripeError } = await stripe.redirectToCheckout({
      sessionId: checkoutRes.data.sessionId
    })

    if (stripeError) {
      error.value = stripeError.message
    }

  } catch (err) {
    console.error('결제 오류:', err)
    error.value = '결제 처리 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.checkout {
  padding: 2rem 0;
}

h1 {
  font-size: 2.5rem;
  margin-bottom: 2rem;
}

.checkout-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 3rem;
}

.checkout-form {
  background: #f9f9f9;
  padding: 2rem;
  border-radius: 8px;
}

.checkout-form h2 {
  margin-bottom: 1.5rem;
}

.btn-large {
  width: 100%;
  padding: 1rem;
  margin-top: 1rem;
}

.order-summary {
  background: #fff;
  padding: 2rem;
  border: 2px solid #000;
  border-radius: 8px;
  height: fit-content;
}

.order-summary h2 {
  margin-bottom: 1.5rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  padding: 1rem 0;
  margin-top: 1rem;
  font-size: 1.3rem;
  font-weight: bold;
}
</style>

