import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])

  // 로컬 스토리지에서 장바구니 불러오기
  const loadCart = () => {
    const savedCart = localStorage.getItem('cart')
    if (savedCart) {
      items.value = JSON.parse(savedCart)
    }
  }

  // 장바구니 저장
  const saveCart = () => {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  // 상품 추가
  const addItem = (product, quantity = 1) => {
    const existingItem = items.value.find(item => item.product.id === product.id)
    
    if (existingItem) {
      existingItem.quantity += quantity
    } else {
      items.value.push({ product, quantity })
    }
    
    saveCart()
  }

  // 상품 제거
  const removeItem = (productId) => {
    items.value = items.value.filter(item => item.product.id !== productId)
    saveCart()
  }

  // 수량 업데이트
  const updateQuantity = (productId, quantity) => {
    const item = items.value.find(item => item.product.id === productId)
    if (item) {
      item.quantity = quantity
      saveCart()
    }
  }

  // 장바구니 비우기
  const clearCart = () => {
    items.value = []
    saveCart()
  }

  // 총 상품 개수
  const itemCount = computed(() => {
    return items.value.reduce((total, item) => total + item.quantity, 0)
  })

  // 총 금액
  const totalAmount = computed(() => {
    return items.value.reduce((total, item) => {
      return total + (item.product.price * item.quantity)
    }, 0)
  })

  // 초기화 시 로컬 스토리지에서 로드
  loadCart()

  return {
    items,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    itemCount,
    totalAmount
  }
})

