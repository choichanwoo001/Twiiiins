<template>
  <div class="website-language" role="group" aria-label="Language / Sprache">
    <button v-for="option in ['en', 'de']" :key="option" type="button" :lang="option"
      :aria-pressed="websiteLanguage === option" :aria-label="option === 'en' ? 'English' : 'Deutsch'"
      @click="websiteLanguage = option">{{ option.toUpperCase() }}</button>
  </div>
</template>
<script setup>
import { websiteLanguage } from '../../composables/useWebsitePreferences'
</script>
<style scoped>
.website-language { display: flex; justify-content: flex-end; gap: 0.8rem; }
button { font: inherit; font-size: 0.8rem; color: #777; border: 0; background: none; padding: 0.4rem 0.1rem; min-height: 2.75rem; cursor: pointer; }
button[aria-pressed='true'], button:hover { color: #8b4513; }
button:focus-visible { outline: 1px solid #8b4513; outline-offset: 3px; }
</style>
