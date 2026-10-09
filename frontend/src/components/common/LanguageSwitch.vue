<template>
  <details ref="menu" class="website-language" @keydown.esc.prevent="closeMenu(true)" @focusout="onFocusOut">
    <summary ref="trigger">{{ websiteLanguage === 'de' ? 'Sprache' : 'Language' }} · {{ websiteLanguage.toUpperCase() }}</summary>
    <div class="language-options" role="group" aria-label="Language / Sprache">
      <button v-for="option in ['en', 'de']" :key="option" type="button" :lang="option"
        :aria-pressed="websiteLanguage === option" @click="selectLanguage(option)">
        {{ option === 'en' ? 'English' : 'Deutsch' }} <span v-if="websiteLanguage === option" aria-hidden="true">✓</span>
      </button>
    </div>
  </details>
</template>
<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute } from 'vue-router'
import { websiteLanguage } from '../../composables/useWebsitePreferences'
const menu = ref(null)
const trigger = ref(null)
const route = useRoute()
function closeMenu(restoreFocus = false) {
  if (menu.value) menu.value.open = false
  if (restoreFocus) trigger.value?.focus()
}
function selectLanguage(language) {
  websiteLanguage.value = language
  closeMenu(true)
}
function onOutsideClick(event) {
  if (!menu.value?.contains(event.target)) closeMenu()
}
function onFocusOut(event) {
  if (!menu.value?.contains(event.relatedTarget)) closeMenu()
}
watch(() => route.fullPath, () => closeMenu())
onMounted(() => document.addEventListener('pointerdown', onOutsideClick))
onBeforeUnmount(() => document.removeEventListener('pointerdown', onOutsideClick))
</script>
<style scoped>
.website-language { position: relative; }
summary { list-style: none; cursor: pointer; padding: 0; color: #555; white-space: nowrap; font-size: 0.75rem; line-height: 1.5rem; }
summary::-webkit-details-marker { display: none; }
summary:focus-visible { outline: 2px solid #815d47; outline-offset: 3px; }
.language-options { position: absolute; left: 0; top: calc(100% + 0.6rem); z-index: 1; min-width: 8rem; padding: 0.3rem; background: #fff; border: 1px solid #ded8d1; box-shadow: 0 4px 16px #00000014; }
.language-options button { display: flex; justify-content: space-between; gap: 1rem; width: 100%; padding: 0.65rem 0.75rem; min-height: 2.75rem; color: #555; text-align: left; font: inherit; font-size: 0.85rem; border: 0; background: none; cursor: pointer; }
.language-options button:hover, .language-options button[aria-pressed='true'] { background: #f5f1ec; color: #815d47; }
.language-options button:focus-visible { outline: 2px solid #815d47; outline-offset: -2px; }
</style>
