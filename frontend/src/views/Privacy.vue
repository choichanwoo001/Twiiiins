<template>
  <article class="privacy-page" :lang="websiteLanguage">
    <header class="privacy-header"><h1>{{ websiteLanguage === 'de' ? 'Datenschutz' : 'Privacy' }}</h1></header>
    <h2 class="privacy-subtitle">{{ copy.title }}</h2>
    <p class="privacy-draft">{{ copy.draft }}</p><p class="privacy-date">{{ copy.date }}</p><p>{{ copy.intro }}</p>
    <div class="privacy-settings"><span>{{ websiteLanguage === 'de' ? 'Einstellungen für diese Website' : 'Settings for this website' }}</span><button class="website-button" @click="cookieSettingsOpen = true">{{ websiteLanguage === 'de' ? 'Cookie-Einstellungen' : 'Cookie settings' }}</button></div>
    <details class="privacy-contents"><summary>{{ websiteLanguage === 'de' ? 'Inhalt' : 'Contents' }}</summary><nav class="privacy-index" :aria-label="websiteLanguage === 'de' ? 'Inhalt' : 'Contents'">
      <router-link v-for="section in copy.sections" :key="section[0]" :to="{ path: '/privacy', hash: '#' + section[0] }">{{ section[1] }}</router-link>
    </nav></details>
    <section v-for="section in copy.sections" :key="section[0]" :id="section[0]">
      <h2>{{ section[1] }}</h2><p v-for="paragraph in section[2]" :key="paragraph">{{ paragraph }}</p>
      <button v-if="section[0] === 'cookies'" class="privacy-text-button" @click="cookieSettingsOpen = true">{{ websiteLanguage === 'de' ? 'Cookie-Einstellungen ändern' : 'Change cookie settings' }}</button>
    </section>
  </article>
</template>
<script setup>
import { computed } from 'vue'
import { websiteLanguage, cookieSettingsOpen } from '../composables/useWebsitePreferences'
import { privacyCopy } from '../constants/privacyCopy'
const copy = computed(() => privacyCopy[websiteLanguage.value])
</script>
<style scoped>
.privacy-page { max-width: 64rem; margin: 0 auto; padding: 3rem 2rem 8rem; overflow-wrap: anywhere; }
.privacy-header { display: flex; align-items: center; justify-content: space-between; gap: 1rem; border-bottom: 1px solid #e8e3dd; margin-bottom: 2.5rem; }
.privacy-header h1 { text-transform: uppercase; letter-spacing: 0.08em; font-size: clamp(2.2rem, 6vw, 4.5rem); }
.privacy-subtitle { margin-top: 0; font-size: clamp(1.3rem, 3vw, 1.8rem); }
.privacy-settings { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem; padding: 1.5rem 0; margin: 1.5rem 0; border-block: 1px solid #e8e3dd; }
.privacy-settings span { color: #555; font-size: 0.9rem; }
.privacy-contents summary { cursor: pointer; color: #815d47; font-size: 0.9rem; padding: 0.75rem 0; }
.privacy-text-button { border: none; background: none; padding: 0.5rem 0; color: #815d47; text-decoration: underline; text-underline-offset: 0.2em; cursor: pointer; font: inherit; }
h1 { font-size: clamp(1.8rem, 4vw, 2.8rem); font-weight: 400; color: #815d47; line-height: 1.2; margin: 1rem 0 2rem; }
h2 { font-size: 1.15rem; font-weight: 500; margin: 2.5rem 0 1rem; }
p { color: #555; line-height: 1.8; margin-bottom: 1rem; }
.privacy-draft { border-left: 2px solid #815d47; padding-left: 1rem; font-size: 0.9rem; }
.privacy-date { font-size: 0.8rem; }
.privacy-index { display: flex; flex-direction: column; gap: 0.3rem; margin: 2rem 0; }
a { color: #815d47; font-size: 0.9rem; }
section { scroll-margin-top: calc(var(--navbar-height) + 1rem); }
@media (max-width: 48rem) { .privacy-page { padding: 2rem 1.5rem 9rem; } }
</style>
