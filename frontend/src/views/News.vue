<template>
  <div class="news-page">
    <h1>{{ t('NEWS') }}</h1>
    <NewsletterSignup />

    <p v-if="loadFailed" class="news-status" role="status">{{ t('News could not be loaded.') }}</p>
    <p v-else-if="!loading && !newsList.length" class="news-status">{{ t('No news yet.') }}</p>
    <div class="news-list" :aria-busy="loading">
      <article v-for="news in newsList" :id="`news-${news.id}`" :key="news.id" class="news-item">
        <h2>
          <button class="news-preview" type="button" :aria-expanded="news.expanded"
            :aria-controls="`news-details-${news.id}`" @click="toggleNews(news.id)">
            <img v-if="news.imageUrls[0]" class="news-thumbnail" :src="news.imageUrls[0]"
              :srcset="getImageSrcset(news.imageUrls[0])" sizes="(max-width: 48rem) 120px, 192px"
              width="192" height="128" alt="" loading="lazy" decoding="async"
              @error="handleResponsiveImageError($event, news.imageUrls[0])">
            <span class="news-content">
              <time v-if="news.date" class="news-date" :datetime="news.date">{{ formatNewsDate(news.date) }}</time>
              <span class="news-title">{{ newsTitle(news) }}</span>
            </span>
            <svg class="news-expand" width="20" height="20" viewBox="0 0 24 24" aria-hidden="true">
              <path d="m6 9 6 6 6-6" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </h2>

        <div :id="`news-details-${news.id}`" :hidden="!news.expanded" class="news-details">
          <template v-if="news.expanded">
            <div v-if="news.bodyEn || news.bodyDe" class="news-description news-rich" v-html="newsBody(news)"></div>
            <p v-else-if="news.description" class="news-description">{{ translateContent('news', 'description', news.description, news) }}</p>
            <p v-for="(url, index) in news.videoUrls || []" :key="url">
              <a :href="url" target="_blank" rel="noopener noreferrer">{{ t('Watch video') }} {{ index + 1 }} ↗</a>
            </p>
            <div v-if="news.imageUrls.length" class="news-images">
              <ImageGrid :images="news.imageUrls" :alt-text-prefix="newsTitle(news)" mobile-aspect-ratio="16 / 9" />
            </div>
          </template>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { computed, onUnmounted, watch } from 'vue'
import { isNavigationFailure, NavigationFailureType, useRoute, useRouter } from 'vue-router'
import { ImageGrid } from '../components/common'
import NewsletterSignup from '../components/common/NewsletterSignup.vue'
import { usePublicNews } from '../composables/usePublicNews'
import { websiteLanguage } from '../composables/useWebsitePreferences'
import { newsBody, newsTitle, t, translateContent } from '../composables/useWebsiteTranslation'
import { getImageSrcset, handleResponsiveImageError } from '../utils/responsiveImages'

const route = useRoute()
const router = useRouter()
const linkedNewsId = computed(() => route.name === 'News' && typeof route.query.newsId === 'string' ? route.query.newsId : '')
const { newsList, loading, loadFailed, refreshNews, toggleNews } = usePublicNews(linkedNewsId)

watch(() => route.query.lang, lang => {
  if (route.name === 'News' && ['en', 'de'].includes(lang)) websiteLanguage.value = lang
}, { immediate: true })

watch(() => newsList.value.find(news => String(news.id) === linkedNewsId.value)?.id, id => {
  if (id != null && !route.hash) document.getElementById(`news-${id}`)?.scrollIntoView()
}, { flush: 'post' })

// Reselecting News loads the current public list even if the URL is already /news.
const removeNavigationHook = router.afterEach((to, _, failure) => {
  if (to.name === 'News' && !linkedNewsId.value && isNavigationFailure(failure, NavigationFailureType.duplicated)) {
    refreshNews()
  }
})
onUnmounted(removeNavigationHook)

function formatNewsDate(date) {
  // The API publishes a calendar date. Keep the same day in every time zone.
  return date.replace(/^(\d{4})-(\d{2})-(\d{2})$/, '$3.$2.$1')
}
</script>

<style scoped>
.news-page {
  max-width: 58rem;
  margin: 0 auto;
  padding: clamp(1.5rem, 4vw, 3.5rem) clamp(1rem, 4vw, 2rem) var(--website-footer-space);
}

h1 {
  margin: 0 0 clamp(1.25rem, 3vw, 2rem);
  font-size: clamp(2rem, 5vw, 4rem);
  font-weight: 300;
  line-height: 1.1;
  letter-spacing: 0.08em;
  overflow-wrap: anywhere;
}

.newsletter-signup { margin: 0 0 clamp(1.5rem, 4vw, 2.75rem); }
.news-status { margin: 1rem 0; color: var(--color-text-secondary); }
.news-list { display: grid; gap: 0.875rem; }

.news-item {
  scroll-margin-top: calc(var(--navbar-height) + 0.75rem);
}

.news-item h2 { margin: 0; font: inherit; }

.news-preview {
  display: flex;
  align-items: center;
  gap: clamp(0.65rem, 2.5vw, 1.5rem);
  width: 100%;
  min-height: 5rem;
  padding: clamp(0.65rem, 2vw, 1rem);
  border: 0;
  background: transparent;
  color: var(--color-text);
  font: inherit;
  text-align: start;
  cursor: pointer;
}

.news-preview:hover { background: var(--color-background-soft); }
.news-preview:focus-visible { outline: 2px solid #815d47; outline-offset: -2px; }

.news-thumbnail {
  display: block;
  width: clamp(4.75rem, 29vw, 12rem);
  height: auto;
  aspect-ratio: 3 / 2;
  flex-shrink: 0;
  object-fit: cover;
  border-radius: 0.125rem;
}

.news-content { display: grid; flex: 1; gap: 0.25rem; min-width: 0; }
.news-date { color: var(--color-text-secondary); font-size: 0.9rem; font-variant-numeric: tabular-nums; }
.news-title {
  color: var(--color-text-secondary);
  font-size: 1.1rem;
  font-weight: 500;
  overflow-wrap: anywhere;
}
.news-expand { flex-shrink: 0; }
.news-preview[aria-expanded='true'] .news-expand { transform: rotate(180deg); }

.news-details {
  padding: clamp(1rem, 3vw, 2rem);
  border-top: 1px solid var(--color-border);
  background: var(--color-background-soft);
  line-height: 1.6;
  overflow-wrap: anywhere;
}

.news-details a, .news-rich :deep(a) { color: #815d47; text-decoration: underline; }
.news-description { margin: 0 0 1rem; white-space: pre-line; }
.news-rich :deep(p) { margin: 0.7rem 0; }
.news-rich :deep(ul), .news-rich :deep(ol) { padding-left: 1.5rem; }
.news-images { margin-top: 1.5rem; }

@media (max-width: 48rem) {
  .news-thumbnail { width: clamp(4.75rem, 27vw, 7.5rem); }
  .news-title { font-size: 1.125rem; line-height: 1.4; }
  .news-expand { width: 1rem; height: 1rem; }
}
</style>
