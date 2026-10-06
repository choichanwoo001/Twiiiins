<template>
  <Teleport to="body">
    <dialog ref="dialog" class="website-dialog" :lang="websiteLanguage" :aria-labelledby="titleId" @cancel.prevent="emit('close')" @close="emit('close')">
      <h2 :id="titleId">{{ title }}</h2>
      <slot />
      <button type="button" class="website-button dialog-close" @click="emit('close')">{{ websiteLanguage === 'de' ? 'Schließen' : 'Close' }}</button>
    </dialog>
  </Teleport>
</template>
<script setup>
import { ref, watch, nextTick, onBeforeUnmount } from 'vue'
import { websiteLanguage } from '../../composables/useWebsitePreferences'
const props = defineProps({ open: Boolean, title: String, titleId: { type: String, required: true } })
const emit = defineEmits(['close'])
const dialog = ref(null)
let previousOverflow, previousFocus
watch(() => props.open, async (open) => {
  await nextTick()
  if (open && !dialog.value?.open) {
    previousFocus = document.activeElement
    previousOverflow = document.body.style.overflow
    dialog.value?.showModal()
    document.body.style.overflow = 'hidden'
  } else if (!open && dialog.value?.open) {
    dialog.value.close()
    document.body.style.overflow = previousOverflow ?? ''
    if (previousFocus?.isConnected) previousFocus.focus()
  }
}, { immediate: true })
onBeforeUnmount(() => {
  if (dialog.value?.open) { dialog.value.close(); document.body.style.overflow = previousOverflow ?? '' }
})
</script>
<style scoped>
.website-dialog { margin: auto; width: min(34rem, calc(100% - 2rem)); max-height: calc(100dvh - 2rem); overflow: auto; padding: 2rem; border: 1px solid #ded8d1; background: #fff; color: #333; }
.website-dialog::backdrop { background: rgba(30, 29, 29, 0.35); }
h2 { font-size: 1.4rem; font-weight: 500; margin-bottom: 1rem; }
.dialog-close { margin-top: 1.5rem; }
@media (max-width: 30rem) { .website-dialog { padding: 1.25rem; } h2 { font-size: 1.2rem; } }
</style>
