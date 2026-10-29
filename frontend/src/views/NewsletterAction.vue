<template>
  <main class="newsletter-action" :lang="language">
    <h1>{{ title }}</h1>
    <p>{{ instruction }}</p>
    <button v-if="!complete" class="website-button" :disabled="busy || !token" @click="submit">{{ busy ? '…' : title }}</button>
    <p role="status" aria-live="polite">{{ message }}</p>
    <router-link to="/contact">{{ language === 'de' ? 'Zurück zum Kontakt' : 'Back to Contact' }}</router-link>
  </main>
</template>
<script setup>
import { computed, ref, watch } from 'vue'
import { websiteLanguage } from '../composables/useWebsitePreferences'
import { useRoute } from 'vue-router'
import { newsletterService as service } from '../services/newsletterService'
const route = useRoute(), busy = ref(false), complete = ref(false), messageKey = ref('')
const token = computed(() => typeof route.query.token === 'string' ? route.query.token : '')
watch(() => route.query.lang, lang => { if (['en', 'de'].includes(lang)) websiteLanguage.value = lang }, { immediate: true })
const language = websiteLanguage
const unsubscribe = computed(() => route.name === 'NewsletterUnsubscribe')
const title = computed(() => unsubscribe.value ? (language.value === 'de' ? 'Newsletter abmelden' : 'Unsubscribe from newsletter') : (language.value === 'de' ? 'Abonnement bestätigen' : 'Confirm subscription'))
const instruction = computed(() => language.value === 'de' ? 'Klicken Sie auf die Schaltfläche, um Ihre Auswahl zu bestätigen.' : 'Click the button to confirm your choice.')
const message = computed(() => {
  if (messageKey.value === 'error') return language.value === 'de' ? 'Der Link ist ungültig, abgelaufen oder wurde bereits verwendet.' : 'This link is invalid, expired or already used.'
  if (messageKey.value !== 'success') return ''
  return language.value === 'de' ? (unsubscribe.value ? 'Sie wurden abgemeldet.' : 'Ihr Abonnement wurde bestätigt.') : (unsubscribe.value ? 'You have been unsubscribed.' : 'Your subscription is confirmed.')
})
watch(() => route.fullPath, () => { complete.value = false; messageKey.value = '' })
async function submit() {
  busy.value = true
  try {
    await (unsubscribe.value ? service.unsubscribe(token.value) : service.confirm(token.value))
    complete.value = true
    messageKey.value = 'success'
  } catch { messageKey.value = 'error' }
  finally { busy.value = false }
}
</script>
<style scoped>.newsletter-action { max-width: 38rem; margin: 5rem auto; padding: 2rem 2rem var(--website-footer-space); line-height: 1.6; } h1 { font-size: 2rem; } a { color: #815d47; }</style>
