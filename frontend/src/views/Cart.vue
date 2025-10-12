<template>
  <div class="cart container">
    <h1>장바구니</h1>

    <div v-if="cartStore.items.length > 0">
      <div class="cart-items">
        <div v-for="item in cartStore.items" :key="item.product.id" class="cart-item">
          <img :src="item.product.imageUrl" :alt="item.product.name">
          <div class="item-info">
            <h3>{{ item.product.name }}</h3>
            <p class="price">€{{ item.product.price }}</p>
          </div>
          <div class="quantity-control">
            <button @click="cartStore.updateQuantity(item.product.id, item.quantity - 1)" :disabled="item.quantity <= 1">-</button>
            <span>{{ item.quantity }}</span>
            <button @click="cartStore.updateQuantity(item.product.id, item.quantity + 1)">+</button>
          </div>
          <div class="item-total">
            €{{ (item.product.price * item.quantity).toFixed(2) }}
          </div>
          <button @click="cartStore.removeItem(item.product.id)" class="remove-btn">삭제</button>
        </div>
      </div>

      <div class="cart-summary">
        <h2>주문 요약</h2>
        <div class="summary-row">
          <span>총 상품 금액</span>
          <span>€{{ cartStore.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="summary-row total">
          <span>총 결제 금액</span>
          <span>€{{ cartStore.totalAmount.toFixed(2) }}</span>
        </div>
        <router-link to="/checkout" class="btn btn-primary btn-large">결제하기</router-link>
      </div>
    </div>

    <div v-else class="empty-cart">
      <p>장바구니가 비어있습니다.</p>
      <router-link to="/store" class="btn btn-primary">쇼핑 계속하기</router-link>
    </div>
  </div>
</template>

<script setup>
import { useCartStore } from '../stores/cart'

const cartStore = useCartStore()
</script>

<style scoped>
.cart {
  padding: 2rem 0;
}

h1 {
  font-size: 2.5rem;
  margin-bottom: 2rem;
}

.cart-items {
  margin-bottom: 2rem;
}

.cart-item {
  display: grid;
  grid-template-columns: 100px 1fr auto auto auto;
  gap: 1.5rem;
  align-items: center;
  padding: 1.5rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.cart-item img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
}

.item-info h3 {
  margin-bottom: 0.5rem;
}

.price {
  font-weight: bold;
  color: #666;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.quantity-control button {
  width: 30px;
  height: 30px;
  border: 1px solid #000;
  background: #fff;
  cursor: pointer;
}

.quantity-control button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.item-total {
  font-size: 1.2rem;
  font-weight: bold;
}

.remove-btn {
  padding: 0.5rem 1rem;
  border: 1px solid #c00;
  background: #fff;
  color: #c00;
  cursor: pointer;
}

.remove-btn:hover {
  background: #c00;
  color: #fff;
}

.cart-summary {
  max-width: 400px;
  margin-left: auto;
  padding: 2rem;
  border: 2px solid #000;
  border-radius: 8px;
}

.cart-summary h2 {
  margin-bottom: 1.5rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.summary-row.total {
  font-size: 1.3rem;
  font-weight: bold;
  border-bottom: none;
  margin-top: 1rem;
}

.btn-large {
  width: 100%;
  margin-top: 1.5rem;
  padding: 1rem;
}

.empty-cart {
  text-align: center;
  padding: 4rem 0;
}

.empty-cart p {
  font-size: 1.2rem;
  margin-bottom: 2rem;
}
</style>

