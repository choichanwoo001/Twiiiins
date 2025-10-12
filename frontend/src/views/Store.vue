<template>
  <div class="store container">
    <h1>Store</h1>
    
    <div class="grid" v-if="products.length > 0">
      <div v-for="product in products" :key="product.id" class="card">
        <img :src="product.imageUrl" :alt="product.name">
        <div class="card-content">
          <h3>{{ product.name }}</h3>
          <p>{{ product.description }}</p>
          <p class="price">€{{ product.price }}</p>
          <p class="stock">재고: {{ product.stock }}개</p>
          <router-link :to="`/store/${product.id}`" class="btn btn-primary">상세보기</router-link>
        </div>
      </div>
    </div>
    <div v-else class="loading">상품을 불러오는 중...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import apiClient from '../api/axios'

const products = ref([])

onMounted(async () => {
  try {
    const res = await apiClient.get('/products')
    products.value = res.data
  } catch (error) {
    console.error('상품 로딩 실패:', error)
  }
})
</script>

<style scoped>
.store {
  padding: 2rem 0;
}

h1 {
  font-size: 2.5rem;
  margin-bottom: 2rem;
  text-align: center;
}

.price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #000;
  margin: 0.5rem 0;
}

.stock {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}
</style>

