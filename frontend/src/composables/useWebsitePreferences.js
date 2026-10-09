import { ref, computed, watch } from 'vue'

const LANGUAGE_KEY = 'twiiiins-website-language'
function readLanguage() {
  try { return localStorage.getItem(LANGUAGE_KEY) === 'de' ? 'de' : 'en' } catch { return 'en' }
}
export const websiteLanguage = ref(readLanguage())
watch(websiteLanguage, (language) => {
  try { localStorage.setItem(LANGUAGE_KEY, language) } catch { /* Keep the choice for this visit. */ }
})
const VERSION = 1
const KEY = 'twiiiins-cookie-preferences'
function readConsent() {
  try {
    const value = JSON.parse(localStorage.getItem(KEY))
    if (value?.version === VERSION && typeof value.media === 'boolean' && Number.isFinite(value.savedAt) && value.savedAt <= Date.now() && Date.now() - value.savedAt < 180 * 86400000) return value
  } catch { /* Storage can be unavailable; start with optional media off. */ }
  return null
}
export const cookiePreferences = ref(readConsent())
export const mediaAllowed = computed(() => cookiePreferences.value?.media === true)
export const cookieSettingsOpen = ref(false)
export const hasCookieChoice = ref(cookiePreferences.value !== null)
export function saveCookiePreferences(media) {
  const value = { version: VERSION, media: Boolean(media), savedAt: Date.now() }
  cookiePreferences.value = value
  hasCookieChoice.value = true
  try { localStorage.setItem(KEY, JSON.stringify(value)) } catch { /* Keep the choice for this visit. */ }
  cookieSettingsOpen.value = false
}
export function useWebsiteCopy(en, de) {
  return computed(() => websiteLanguage.value === 'de' ? de : en)
}
