<template>
  <section class="newsletter-signup" aria-labelledby="newsletter-heading">
    <h2 id="newsletter-heading">Newsletter</h2>
    <p>{{ copy.intro }}</p>
    <p v-if="preview">{{ copy.preview }}</p>
    <form v-if="state === 'ready' || state === 'preview'" @submit.prevent="submit">
      <div class="signup-fields">
        <label>{{ copy.email }}<input v-model.trim="email" type="email" autocomplete="email" maxlength="254" required :disabled="busy"></label>
        <label>{{ copy.language }}<select v-model="language" :disabled="busy"><option value="en">English</option><option value="de">Deutsch</option></select></label>
      </div>
      <label class="consent"><input v-model="consent" type="checkbox" required :disabled="busy"><span>{{ copy.consent }} <router-link to="/privacy#newsletter">{{ copy.privacy }}</router-link>.</span></label>
      <button class="website-button" :disabled="busy">{{ busy ? copy.sending : copy.subscribe }}</button>
    </form>
    <p v-else-if="state === 'loading'" role="status">{{ copy.loading }}</p>
    <p v-else-if="state === 'disabled'">{{ copy.unavailable }}</p>
    <div v-else-if="state === 'error'">
      <p role="alert">{{ copy.connectionError }}</p>
      <button class="website-button" type="button" @click="checkAvailability">{{ copy.retry }}</button>
    </div>
    <p role="status" aria-live="polite">{{ messageKey ? copy[messageKey] : '' }}</p>
  </section>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { websiteLanguage, useWebsiteCopy } from '../../composables/useWebsitePreferences'
import { newsletterService as service } from '../../services/newsletterService'
const preview = import.meta.env.DEV && import.meta.env.VITE_DUMMY_DATA === 'true'
const email = ref(''), language = ref(websiteLanguage.value), consent = ref(false), busy = ref(false)
const state = ref(preview ? 'preview' : 'loading'), messageKey = ref('')
const copy = useWebsiteCopy({ preview: 'Preview only. No subscription is saved and no email is sent.', previewSuccess: 'Preview complete. No subscription was saved and no email was sent.', loading: 'Checking subscription availability…', connectionError: 'We could not check subscription availability. Please try again.', retry: 'Try again', intro: 'Music, performances and news from TWIIIINS, straight to your inbox.', email: 'Email address', language: 'Newsletter language', consent: 'I would like to receive the TWIIIINS newsletter. I can unsubscribe at any time. See our', privacy: 'privacy notice', subscribe: 'Subscribe', sending: 'Sending…', unavailable: 'Newsletter subscriptions are currently unavailable.', success: 'Thank you for subscribing! We’ll send you news from TWIIIINS. No further confirmation is needed.', error: 'We could not process your request. Please try again later.' },
{ preview: 'Nur Vorschau. Es wird kein Abonnement gespeichert und keine E-Mail versendet.', previewSuccess: 'Vorschau abgeschlossen. Es wurde kein Abonnement gespeichert und keine E-Mail versendet.', loading: 'Die Verfügbarkeit wird geprüft…', connectionError: 'Die Verfügbarkeit konnte nicht geprüft werden. Bitte versuchen Sie es erneut.', retry: 'Erneut versuchen', intro: 'Musik, Auftritte und Neuigkeiten von TWIIIINS direkt in Ihr Postfach.', email: 'E-Mail-Adresse', language: 'Newsletter-Sprache', consent: 'Ich möchte den TWIIIINS-Newsletter erhalten und kann mich jederzeit abmelden. Weitere Informationen in unserer', privacy: 'Datenschutzerklärung', subscribe: 'Abonnieren', sending: 'Wird gesendet…', unavailable: 'Newsletter-Abonnements sind derzeit nicht verfügbar.', success: 'Vielen Dank für Ihr Abonnement! Sie erhalten ab jetzt Neuigkeiten von TWIIIINS. Eine weitere Bestätigung ist nicht erforderlich.', error: 'Die Anfrage konnte nicht bearbeitet werden. Bitte versuchen Sie es später erneut.' })
watch(websiteLanguage, value => { language.value = value })
async function checkAvailability() {
  if (preview) return
  state.value = 'loading'
  try {
    const result = await service.available()
    if (typeof result?.available !== 'boolean') throw new Error('Invalid newsletter status')
    state.value = result.available ? 'ready' : 'disabled'
  } catch { state.value = 'error' }
}
onMounted(checkAvailability)
async function submit() {
  if (busy.value || !consent.value || !['ready', 'preview'].includes(state.value)) return
  busy.value = true; messageKey.value = ''
  try {
    if (preview) messageKey.value = 'previewSuccess'
    else {
      await service.subscribe({ email: email.value, language: language.value, consent: consent.value })
      messageKey.value = 'success'
    }
    email.value = ''; consent.value = false
  } catch { messageKey.value = 'error' }
  finally { busy.value = false }
}

</script>

<style scoped>
.newsletter-signup { padding-bottom: 1.8rem; border-bottom: 1px solid #e8e3dd; }
h2 { margin: 0 0 .75rem; font-size: 1.3rem; font-weight: 500; }
p { color: #666; line-height: 1.6; }
.signup-fields { display: flex; gap: 1rem; flex-wrap: wrap; }
.signup-fields label { flex: 1; min-width: 10rem; }
label { font-size: .85rem; display: block; }
input[type=email], select { display: block; box-sizing: border-box; width: 100%; margin-top: .5rem; padding: .8rem; border: 1px solid #c4b8af; background: #fff; border-radius: 0; font: inherit; color: #222; }
.consent { display: flex; align-items: flex-start; gap: .65rem; margin: 1.2rem 0; line-height: 1.6; }
.consent input { margin-top: .3rem; flex-shrink: 0; }
a { color: #815d47; } :focus-visible { outline: 2px solid #815d47; outline-offset: 3px; }
button:disabled { opacity: .6; }
</style>
