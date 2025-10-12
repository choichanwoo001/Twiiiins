<template>
  <div class="product-detail container" v-if="product">
    <div class="product-layout">
      <div class="product-image">
        <img :src="product.imageUrl" :alt="product.name">
      </div>
      
      <div class="product-info">
        <h1>{{ product.name }}</h1>
        <p class="price">€{{ product.price }}</p>
        <p class="description">{{ product.description }}</p>
        
        <div class="stock-info">
          <p v-if="product.stock > 0" class="in-stock">재고: {{ product.stock }}개</p>
          <p v-else class="out-of-stock">품절</p>
        </div>

        <div class="quantity-selector">
          <label>수량:</label>
          <button @click="decreaseQuantity">-</button>
          <input type="number" v-model.number="quantity" min="1" :max="product.stock">
          <button @click="increaseQuantity">+</button>
        </div>

        <button 
          @click="addToCart" 
          class="btn btn-primary btn-large"
          :disabled="product.stock === 0">
          장바구니에 담기
        </button>

        <p v-if="message" class="success">{{ message }}</p>
      </div>
    </div>
  </div>
  <div v-else class="loading container">상품 정보를 불러오는 중...</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '../stores/cart'
import apiClient from '../api/axios'

const route = useRoute()
const cartStore = useCartStore()

const product = ref(null)
const quantity = ref(1)
const message = ref('')

const increaseQuantity = () => {
  if (quantity.value < product.value.stock) {
    quantity.value++
  }
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const addToCart = () => {
  cartStore.addItem(product.value, quantity.value)
  message.value = '장바구니에 추가되었습니다!'
  setTimeout(() => {
    message.value = ''
  }, 3000)
}

onMounted(async () => {
  try {
    const res = await apiClient.get(`/products/${route.params.id}`)
    product.value = res.data
  } catch (error) {
    console.error('상품 로딩 실패:', error)
  }
})
</script>

<style scoped>
.product-detail {
  padding: 2rem 0;
}

.product-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
}

.product-image img {
  width: 100%;
  border-radius: 8px;
}

.product-info h1 {
  font-size: 2rem;
  margin-bottom: 1rem;
}

.price {
  font-size: 2rem;
  font-weight: bold;
  margin: 1rem 0;
}

.description {
  line-height: 1.8;
  margin: 2rem 0;
}

.stock-info {
  margin: 1rem 0;
}

.in-stock {
  color: #0a0;
}

.out-of-stock {
  color: #c00;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 2rem 0;
}

.quantity-selector button {
  width: 40px;
  height: 40px;
  border: 1px solid #000;
  background: #fff;
  cursor: pointer;
  font-size: 1.2rem;
}

.quantity-selector input {
  width: 60px;
  text-align: center;
  font-size: 1.2rem;
}

.btn-large {
  width: 100%;
  padding: 1rem;
  font-size: 1.1rem;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>

