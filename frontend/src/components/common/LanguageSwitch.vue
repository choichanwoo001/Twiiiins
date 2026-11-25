<template>
  <details ref="menu" class="website-language" @pointerenter="openOnHover" @pointerleave="closeOnPointerLeave" @focusout="onFocusOut">
    <summary ref="trigger" :aria-label="websiteLanguage === 'de' ? 'Sprache: Deutsch' : 'Language: English'"
      @click="keepOpenOnClick">
      <span class="language-label">{{ websiteLanguage === 'de' ? 'SPRACHE' : 'LANGUAGE' }}</span>
      <span class="language-code">{{ websiteLanguage.toUpperCase() }}</span>
    </summary>
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
import { useRoute, useRouter } from 'vue-router'
import { websiteLanguage } from '../../composables/useWebsitePreferences'
const menu = ref(null)
const trigger = ref(null)
const route = useRoute()
const router = useRouter()
let openedByHover = false

function closeMenu(restoreFocus = false) {
  openedByHover = false
  if (menu.value) menu.value.open = false
  if (restoreFocus) trigger.value?.focus()
}

function openOnHover(event) {
  if (event.pointerType !== 'mouse' || menu.value.open) return
  openedByHover = true
  menu.value.open = true
}

function closeOnPointerLeave(event) {
  if (event.pointerType === 'mouse' && openedByHover && !menu.value.contains(document.activeElement)) closeMenu()
}

function keepOpenOnClick(event) {
  if (!openedByHover) return
  // A click keeps the hover-opened menu open after the pointer leaves.
  event.preventDefault()
  openedByHover = false
}
function selectLanguage(language) {
  websiteLanguage.value = language
  closeMenu(true)
  if (['en', 'de'].includes(route.query.lang) && route.query.lang !== language) {
    router.replace({ path: route.path, query: { ...route.query, lang: language }, hash: route.hash })
  }
}
function onOutsideClick(event) {
  if (!menu.value?.contains(event.target)) closeMenu()
}
function onFocusOut(event) {
  if (event.relatedTarget && !menu.value?.contains(event.relatedTarget)) closeMenu()
}
function onEscape(event) {
  if (event.key !== 'Escape' || !menu.value?.open) return
  const focusInside = menu.value.contains(document.activeElement)
  if (focusInside) event.preventDefault()
  closeMenu(focusInside)
}
watch(() => route.fullPath, () => closeMenu())
onMounted(() => {
  document.addEventListener('pointerdown', onOutsideClick)
  document.addEventListener('keydown', onEscape)
})
onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', onOutsideClick)
  document.removeEventListener('keydown', onEscape)
})
</script>
<style scoped>
.website-language { position: relative; z-index: 2000; flex-shrink: 0; }
summary {
  display: flex;
  align-items: center;
  list-style: none;
  cursor: pointer;
  min-height: 2.75rem;
  color: var(--color-text);
  white-space: nowrap;
  line-height: 1.5rem;
  transition: opacity 0.3s;
  -webkit-tap-highlight-color: transparent;
}
summary:is(:hover, :active) { opacity: 0.7; }
summary::-webkit-details-marker { display: none; }
summary:focus-visible { outline: 2px solid #815d47; outline-offset: 3px; }
.language-options { position: absolute; right: 0; top: calc(100% + 0.4rem); min-width: 8rem; padding: 0.3rem; background: #fff; border: 1px solid #ded8d1; box-shadow: 0 4px 16px #00000014; }
/* Keep the pointer inside the disclosure while it crosses the gap. */
.language-options::before { content: ''; position: absolute; inset: -0.5rem 0 100%; }
.language-options button { display: flex; justify-content: space-between; gap: 1rem; width: 100%; padding: 0.65rem 0.75rem; min-height: 2.75rem; color: #555; text-align: left; font: inherit; font-size: 0.85rem; border: 0; background: none; cursor: pointer; }
.language-options button:hover, .language-options button[aria-pressed='true'] { background: #f5f1ec; color: #815d47; }
.language-options button:focus-visible { outline: 2px solid #815d47; outline-offset: -2px; }
.language-code { display: none; }
@media (max-width: 70rem) {
  .language-label { display: none; }
  .language-code { display: inline; }
}
@media (max-width: 48rem) {
  summary { justify-content: center; min-width: 2.75rem; }
}
</style>
