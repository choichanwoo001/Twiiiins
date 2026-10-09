<template>
  <div class="newsletter-editor">
    <div v-if="editor" class="toolbar" role="toolbar" aria-label="본문 서식">
      <button type="button" :aria-pressed="editor.isActive('bold')" @click="editor.chain().focus().toggleBold().run()">굵게</button>
      <button type="button" :aria-pressed="editor.isActive('italic')" @click="editor.chain().focus().toggleItalic().run()">기울임</button>
      <button type="button" @click="editor.chain().focus().toggleHeading({ level: 2 }).run()">소제목</button>
      <button type="button" @click="editor.chain().focus().toggleBulletList().run()">목록</button>
      <button type="button" @click="editor.chain().focus().toggleOrderedList().run()">번호 목록</button>
      <button type="button" @click="linkOpen = !linkOpen">링크</button>
      <button type="button" @click="editor.chain().focus().unsetLink().run()">링크 제거</button>
    </div>
    <div v-if="linkOpen" class="link-form"><input v-model="link" type="url" placeholder="https://" aria-label="링크 주소"><button type="button" @click="addLink">적용</button><span>{{ linkError }}</span></div>
    <EditorContent :editor="editor" />
  </div>
</template>
<script setup>
import { ref, watch } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
const props = defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])
const linkOpen = ref(false), link = ref(''), linkError = ref('')
const editor = useEditor({ content: props.modelValue, extensions: [StarterKit.configure({ heading: { levels: [2, 3] }, codeBlock: false, code: false, blockquote: false, horizontalRule: false, strike: false, underline: false, link: { openOnClick: false, protocols: ['https'] } })],
  editorProps: { attributes: { 'aria-label': '뉴스레터 본문', role: 'textbox', 'aria-multiline': 'true' } },
  onUpdate: ({ editor }) => emit('update:modelValue', editor.getHTML()) })
watch(() => props.modelValue, value => { if (editor.value && editor.value.getHTML() !== value) editor.value.commands.setContent(value || '', { emitUpdate: false }) })
function addLink() {
  try { if (new URL(link.value).protocol !== 'https:') throw new Error(); editor.value.chain().focus().setLink({ href: link.value }).run(); linkOpen.value = false; linkError.value = '' }
  catch { linkError.value = 'HTTPS 주소를 입력하세요.' }
}
</script>
<style scoped>
.newsletter-editor { border: 1px solid #ccc; border-radius: .25rem; }.toolbar { display: flex; flex-wrap: wrap; gap: .4rem; padding: .5rem; background: #f6f4f1; } button { padding: .4rem .6rem; cursor: pointer; } button[aria-pressed=true] { background: #ded4cc; }.link-form { padding: .5rem; }.link-form input { padding: .5rem; width: 60%; }:deep(.tiptap) { padding: 1rem; min-height: 14rem; outline-color: #815d47; line-height: 1.7; }:deep(.tiptap p) { margin: .5rem 0; }:deep(a) { color: #815d47; text-decoration: underline; }
</style>
