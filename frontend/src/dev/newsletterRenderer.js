import template from '../../../backend/src/main/resources/newsletter/email.html?raw'
import footerTemplate from '../../../backend/src/main/resources/newsletter/footer.html?raw'
const escape = text => String(text || '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]))
const expand = (source, values) => source.replace(/\{\{(\w+)\}\}/g, (_, key) => values[key] || '')
function url(value, image = false) {
  if (image && value.startsWith('blob:') && new URL(value.slice(5)).origin === location.origin) return value
  const parsed = new URL(value, location.origin)
  if (parsed.username || parsed.password || !(parsed.protocol === 'https:' || (image && parsed.origin === location.origin && ['http:', 'https:'].includes(parsed.protocol)))) throw new Error('Use an HTTPS URL')
  return parsed.href
}
function clean(html) {
  const doc = new DOMParser().parseFromString(html || '', 'text/html')
  const allowed = new Set(['P', 'BR', 'STRONG', 'EM', 'H2', 'H3', 'UL', 'OL', 'LI', 'A'])
  function node(value) {
    if (value.nodeType === Node.TEXT_NODE) return escape(value.textContent)
    if (value.nodeType !== Node.ELEMENT_NODE || ['SCRIPT', 'STYLE'].includes(value.tagName)) return ''
    const children = [...value.childNodes].map(node).join('')
    if (!allowed.has(value.tagName)) return children
    const tag = value.tagName.toLowerCase()
    if (tag === 'br') return '<br>'
    if (tag === 'a') {
      let href
      try { href = url(value.getAttribute('href') || '') } catch { return children }
      return `<a href="${escape(href)}" rel="noopener noreferrer" style="color:#80ffcf;text-decoration:underline;">${children}</a>`
    }
    return `<${tag}>${children}</${tag}>`
  }
  return [...doc.body.childNodes].map(node).join('')
}
export function renderPreview(data, language) {
  const de = language === 'de', title = (de ? data.titleDe : data.title) || ''
  const image = (src, alt = '') => `<p style="margin:24px 0;"><img width="640" style="display:block;width:100%;max-width:640px;height:auto;border-radius:16px;" alt="${escape(alt)}" src="${escape(url(src, true))}"></p>`
  const images = data.imageUrls || [], when = de ? data.eventWhenDe : data.eventWhenEn, place = de ? data.eventLocationDe : data.eventLocationEn
  const label = de ? data.ctaLabelDe : data.ctaLabelEn
  let actions = ''
  if (data.ctaUrl) {
    const href = url(data.ctaUrl)
    if (label) actions += `<p style="margin:28px 0;"><a style="display:inline-block;background:#80ffcf;color:#050505;padding:14px 24px;font-size:16px;font-weight:bold;text-decoration:none;" href="${escape(href)}">${escape(label)}</a></p>`
  }
  for (const video of data.videoUrls || []) if (video) actions += `<p><a style="color:#80ffcf;text-decoration:underline;" href="${escape(url(video))}">${de ? 'Video ansehen' : 'Watch video'}</a></p>`
  const html = expand(template, { language, title: escape(title), hero: images.length ? image(images[0], title) : '',
    details: when || place ? `<p style="margin:20px 0 28px;text-align:center;font-size:18px;line-height:1.5;color:#fff;">${escape(when)}${when && place ? '<br>' : ''}${escape(place)}</p>` : '',
    body: clean(de ? data.bodyDe : data.bodyEn), gallery: images.slice(1).map(src => image(src)).join(''), actions })
  const footer = expand(footerTemplate, { operator: de ? 'Beispiel: Veranstalter' : 'Example: sender information', replyTo: 'Example: contact@example.com',
    view: `<span>${de ? 'Im Browser ansehen (Vorschau)' : 'View in browser (preview)'}</span>`, unsubscribe: `<span>${de ? 'Abmelden (Vorschau)' : 'Unsubscribe (preview)'}</span>` })
  return { title, html: html.replace('<!--NEWSLETTER_FOOTER-->', footer) }
}
