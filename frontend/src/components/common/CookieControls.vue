<template>
  <WebsiteDialog :open="cookieSettingsOpen || (!hasCookieChoice && !dismissed)" :title="copy.title" title-id="cookie-settings-title" @close="closeSettings">
    <p>{{ copy.description }}</p>
    <div class="cookie-category">
      <div class="cookie-option"><strong>{{ copy.essential }}</strong><span class="cookie-status">{{ copy.always }}</span></div>
      <details><summary>{{ websiteLanguage === 'de' ? 'Details anzeigen' : 'Show details' }}</summary><p>{{ copy.essentialDescription }}</p></details>
    </div>
    <div class="cookie-category">
      <label class="cookie-option"><strong>{{ copy.media }}</strong><input type="checkbox" v-model="selectedMedia" role="switch"></label>
      <details><summary>{{ websiteLanguage === 'de' ? 'Details anzeigen' : 'Show details' }}</summary><p>{{ copy.mediaDescription }}</p></details>
    </div>
    <div class="cookie-actions">
      <button class="website-button" @click="saveCookiePreferences(selectedMedia)">{{ copy.save }}</button>
      <button class="website-button" @click="saveCookiePreferences(true)">{{ copy.accept }}</button>
      <button class="website-button" @click="saveCookiePreferences(false)">{{ copy.reject }}</button>
    </div>
    <router-link class="cookie-policy" to="/privacy" @click="closeSettings">{{ copy.policy }}</router-link>
  </WebsiteDialog>
</template>
<script setup>
import { ref, watch } from 'vue'
import WebsiteDialog from './WebsiteDialog.vue'
import { websiteLanguage, useWebsiteCopy, hasCookieChoice, mediaAllowed, cookieSettingsOpen, saveCookiePreferences } from '../../composables/useWebsitePreferences'
const selectedMedia = ref(false)
const dismissed = ref(false)
function closeSettings() { cookieSettingsOpen.value = false; dismissed.value = true }
watch(cookieSettingsOpen, (open) => { if (open) selectedMedia.value = mediaAllowed.value })
const copy = useWebsiteCopy({
 title: 'Cookies & external media', description: 'We use necessary storage to operate this website and remember your choice. YouTube videos load only with your permission. We do not use analytics or marketing trackers.', policy: 'Privacy, Cookie and Website Terms', accept: 'Accept all', reject: 'Reject optional', settings: 'Settings', essential: 'Necessary storage', always: 'Always active', essentialDescription: 'Used for website settings, security and administrator sign-in.', media: 'External media · YouTube', mediaDescription: 'Loading YouTube connects to Google and may share your IP address and device information. You can withdraw permission here at any time.', save: 'Save preferences'
}, {
 title: 'Cookies & externe Medien', description: 'Wir verwenden notwendige Speicherfunktionen für den Betrieb der Website und Ihre Auswahl. YouTube-Videos werden nur mit Ihrer Einwilligung geladen. Wir verwenden keine Analyse- oder Marketing-Tracker.', policy: 'Datenschutz, Cookies und Nutzungsbedingungen', accept: 'Alle akzeptieren', reject: 'Optionale ablehnen', settings: 'Einstellungen', essential: 'Notwendige Speicherung', always: 'Immer aktiv', essentialDescription: 'Für Website-Einstellungen, Sicherheit und die Administrator-Anmeldung.', media: 'Externe Medien · YouTube', mediaDescription: 'Beim Laden von YouTube wird eine Verbindung zu Google hergestellt. Dabei können Ihre IP-Adresse und Geräteinformationen übermittelt werden. Sie können Ihre Einwilligung hier jederzeit widerrufen.', save: 'Auswahl speichern'
})
</script>
<style scoped>
h2 { font-weight: 500; font-size: 1.2rem; margin-bottom: 0.5rem; }
p { font-size: 0.9rem; line-height: 1.6; margin: 0.75rem 0; }
a { font-size: 0.8rem; color: #815d47; }
.cookie-actions { display: flex; gap: 0.6rem; flex-wrap: wrap; margin-top: 1rem; }
.cookie-category { border: 1px solid #e4ded8; padding: 1rem; margin-top: 0.75rem; }
.cookie-option { display: flex; align-items: center; justify-content: space-between; gap: 1rem; font-size: 0.9rem; }
.cookie-status { color: #777; font-size: 0.75rem; }
summary { cursor: pointer; font-size: 0.75rem; color: #777; padding-top: 0.7rem; }
.cookie-policy { display: block; margin-top: 1.25rem; }
input { appearance: none; position: relative; flex-shrink: 0; width: 2.5rem; height: 1.4rem; border-radius: 1rem; background: #c9c6c2; cursor: pointer; }
input::after { content: ''; position: absolute; top: 0.2rem; left: 0.2rem; width: 1rem; height: 1rem; border-radius: 50%; background: white; transition: transform 0.15s; }
input:checked { background: #815d47; }
input:checked::after { transform: translateX(1.1rem); }
input:focus-visible { outline: 2px solid #815d47; outline-offset: 3px; }
@media (max-width: 30rem) { .cookie-actions { flex-direction: column; } .cookie-actions button { width: 100%; } }
</style>
