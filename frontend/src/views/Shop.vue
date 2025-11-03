<template>
  <div class="shop">
    <!-- 좌측 고정 타이틀 -->
    <aside class="shop-title">
      <h1>SHOP</h1>
    </aside>

    <!-- 우측 콘텐츠 -->
    <main class="shop-content">
      <div class="no-products">
        <p>No products available yet.</p>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 레이아웃 */
.shop {
  background: #fff;
  color: #222;
  padding-top: 6rem;
  display: grid;
  grid-template-columns: 1fr 2.2fr;
  align-items: start;
  gap: 4rem;
  margin-left: auto;
  margin-right: auto;
  padding-left: 2rem;
  padding-right: 4rem;
  height: calc(100vh - 3.75rem);
  overflow: hidden;
}

/* 좌측 타이틀 */
.shop-title {
  display: flex;
  align-items: flex-start;
  justify-content: flex-start;
  padding-top: 2rem;
}

.shop-title h1 {
  font-size: clamp(2.5rem, 6vw, 4.5rem);
  font-weight: 400;
  letter-spacing: 0.12em;
  color: #C1E1DC;
  text-transform: uppercase;
  line-height: 1;
  margin: 0;
}

/* 우측 콘텐츠 */
.shop-content {
  max-width: 50rem;
  width: 100%;
  margin-right: 2rem;
  justify-self: end;
  padding-top: 2rem;
  min-height: 0; /* 그리드 아이템 내부 스크롤 허용 */
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-gutter: stable;
  box-sizing: border-box;
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

</style>
