export function hasNewsText(html) {
  const doc = new DOMParser().parseFromString(html || '', 'text/html')
  doc.querySelectorAll('script, style').forEach(node => node.remove())
  return !!doc.body.textContent.trim()
}

export function completeNewsLanguages(news) {
  return [
    news.title?.trim() && hasNewsText(news.bodyEn) ? 'en' : null,
    news.titleDe?.trim() && hasNewsText(news.bodyDe) ? 'de' : null
  ].filter(Boolean)
}

export function newsContentLanguage(news, requested) {
  const complete = completeNewsLanguages(news)
  return complete.includes(requested) ? requested : complete[0] || requested
}
