<template>
  <div class="image-grid">
    <div class="image-row">
      <div class="image-item" v-for="(imageUrl, index) in images" :key="index" :style="{ '--mobile-aspect-ratio': mobileAspectRatio }">
        <img :src="imageUrl" :alt="`${altTextPrefix} ${index + 1}`" />
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  images: {
    type: Array,
    required: true,
    default: () => []
  },
  altTextPrefix: {
    type: String,
    default: 'Image'
  },
  mobileAspectRatio: {
    type: String,
    default: 'auto'
  }
})
</script>

<style scoped>
.image-grid {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 90%; 
  width: 100%;
}

.image-row {
  display: flex;
  gap: 0.1rem;
  flex-wrap: wrap;
  min-height: 18.75rem;
  align-items: center; 
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  justify-content: center; 
}

.image-item {
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  height: 25rem; 
  width: auto;
  box-sizing: border-box;
}

.image-item img {
  width: auto; 
  height: 100%;
  object-fit: cover; 
  object-position: center;
  transition: transform 0.3s ease;
  display: block;
  max-width: 100%;
  max-height: 100%;
  box-sizing: border-box;
}

.image-item:hover img {
  transform: scale(1.05);
}

/* 모바일 최적화 (768px 이하) */
@media (max-width: 768px) {
  .image-grid {
    max-width: 100%;
    gap: 0.1rem;
  }

  .image-row {
    height: auto;
    min-height: 0;
    flex-direction: column;
    gap: 0.1rem;
  }

  .image-item {
    height: auto;
    width: 100%;
    max-width: 100%;
    aspect-ratio: var(--mobile-aspect-ratio);
  }

  .image-item img {
    width: 100%;
    height: auto;
    max-height: none;
  }
}
</style>
