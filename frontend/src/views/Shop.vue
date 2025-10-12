<template>
  <div class="shop">
    <!-- 텍스트 섹션 -->
    <section class="shop-text">
      <h1>SHOP</h1>
    </section>

    <!-- 상품 섹션 -->
    <section class="shop-content">
      <div class="no-products">
        <p>No products available yet.</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.shop {
  background-color: white;
  color: #333;
  font-family: 'Arial', sans-serif;
  min-height: calc(100vh - 60px); /* 헤더 높이 제외 */
  margin-top: -60px; /* 헤더 패딩 상쇄 */
  padding-top: 60px; /* 헤더 높이만큼 상단 패딩 */
}

/* 텍스트 섹션 */
.shop-text {
  padding: 2rem 2rem 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  font-size: 4rem;
  font-weight: bold;
  color: #2E86AB;
  margin: 0;
  text-align: left;
  letter-spacing: 3px;
}

/* 상품 섹션 */
.shop-content {
  padding: 0 2rem 4rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.no-products {
  text-align: center;
  padding: 4rem 2rem;
}

.no-products p {
  font-size: 1.5rem;
  color: #666;
  margin: 0;
  font-weight: 300;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .shop-text {
    padding: 2rem 1rem 3rem 1rem;
  }
  
  h1 {
    font-size: 2.5rem;
    text-align: center;
  }
  
  .shop-content {
    padding: 0 1rem 3rem 1rem;
  }
  
  .no-products {
    padding: 3rem 1rem;
  }
  
  .no-products p {
    font-size: 1.2rem;
  }
}

@media (max-width: 480px) {
  h1 {
    font-size: 2rem;
  }
  
  .no-products p {
    font-size: 1rem;
  }
}
</style>
