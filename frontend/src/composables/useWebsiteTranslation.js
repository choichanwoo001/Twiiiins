import { computed } from 'vue'
import { websiteLanguage } from './useWebsitePreferences'
import contentTranslations from '../translations/content.json'
import { newsContentLanguage } from '../utils/newsLanguage'

const german = {
  ABOUT: 'ÜBER UNS', PROJECTS: 'PROJEKTE', CONCERTS: 'KONZERTE', CONCERT: 'KONZERTE',
  MEDIA: 'MEDIEN', SHOP: 'SHOP', CONTACT: 'KONTAKT', MUSIC: 'MUSIK', VIDEOS: 'VIDEOS',
  PHOTOS: 'FOTOS', NEWS: 'NEUIGKEITEN', EQUIPMENT: 'AUSRÜSTUNG', REVIEWS: 'REZENSIONEN',
  'Open menu': 'Menü öffnen', 'Close menu': 'Menü schließen', 'Scroll to top': 'Nach oben scrollen',
  'Upcoming Events': 'Kommende Veranstaltungen', 'Past Events': 'Vergangene Veranstaltungen',
  'UPCOMING EVENTS': 'KOMMENDE VERANSTALTUNGEN', 'No upcoming events.': 'Keine kommenden Veranstaltungen.',
  'No projects found.': 'Keine Projekte gefunden.', 'Image not found': 'Bild nicht gefunden',
  'Project image': 'Projektbild', Image: 'Bild',
  'Premiere:': 'Premiere:', 'Director:': 'Regie:', 'More Information': 'Weitere Informationen',
  'Concert started at:': 'Konzertbeginn war um:', 'Concert starts at:': 'Konzertbeginn:',
  'Ticket Info:': 'Ticketinformationen:', 'Location:': 'Ort:', 'Google Map:': 'Google Maps:',
  'View on Google Maps': 'Auf Google Maps ansehen',
  'Concert information (only for collaboration):': 'Informationen zur Zusammenarbeit:',
  'Invalid video URL.': 'Ungültige Video-URL.', 'No videos found.': 'Keine Videos gefunden.',
  'YouTube video player': 'YouTube-Videoplayer', 'Photo from': 'Foto aus', Download: 'Downloads',
  'FOR COLLABORATIONS, PERFORMANCE INQUIRIES, OR PORTFOLIO REQUESTS,': 'FÜR KOOPERATIONEN, KONZERTANFRAGEN ODER PORTFOLIO-ANFRAGEN',
  'PLEASE CONTACT US HERE. WE LOOK FORWARD TO CONNECTING WITH YOU!': 'KONTAKTIEREN SIE UNS BITTE HIER. WIR FREUEN UNS AUF IHRE NACHRICHT!',
  'Preview · sample contacts and downloads. Download links are inactive.': 'Vorschau · Beispielkontakte und Downloads. Download-Links sind deaktiviert.',
  'TWIIIINS are a modern, dynamic violin duo with influences spanning folk, pop, classical, funk, jazz, rock, and cinematic elements.': 'TWIIIINS sind ein modernes, dynamisches Violinduo mit Einflüssen aus Folk, Pop, Klassik, Funk, Jazz, Rock und Filmmusik.',
  'Dowon Kim and Linus Shastri holding their violins in a lively pose': 'Dowon Kim und Linus Shastri halten ihre Geigen in einer lebhaften Pose',
  'Dowon Kim and Linus Shastri playing their violins in the studio': 'Dowon Kim und Linus Shastri spielen ihre Geigen im Studio',
  'TWIIIINS posing with their red loop stations in the studio': 'TWIIIINS posieren mit ihren roten Loopstations im Studio',
  'TWIIIINS performing under blue stage lights at Seongsu Art Hall': 'TWIIIINS treten im blauen Bühnenlicht in der Seongsu Art Hall auf',
  'Dowon Kim and Linus Shastri performing live at Seongsu Art Hall': 'Dowon Kim und Linus Shastri treten live in der Seongsu Art Hall auf',
  'An audience gathered around TWIIIINS during their Korea tour': 'Publikum versammelt sich während ihrer Korea-Tour um TWIIIINS',
  'TWIIIINS – Only The Beginning album cover, with flowing orange, black and cream patterns': 'Albumcover von TWIIIINS – Only The Beginning mit fließenden Mustern in Orange, Schwarz und Creme',
}

export const websiteLocale = computed(() => websiteLanguage.value === 'de' ? 'de-DE' : 'en-US')
export function t(source) { return websiteLanguage.value === 'de' ? german[source] ?? source : source }

// Only exact, reviewed source strings are localized. New or edited content stays intact.
export function translateContent(kind, field, source) {
  if (typeof source !== 'string') return source
  return contentTranslations[kind]?.[field]?.[source]?.[websiteLanguage.value] ?? source
}

// Existing authored language variants take precedence over the saved translations.
export function newsTitle(news) {
  return newsContentLanguage(news, websiteLanguage.value) === 'de' && news.titleDe?.trim()
    ? news.titleDe : translateContent('news', 'title', news.title)
}
export function newsBody(news) {
  if (newsContentLanguage(news, websiteLanguage.value) === 'de' && news.bodyDe?.trim()) return news.bodyDe
  return translateContent('news', 'body', news.bodyEn || news.bodyDe || '')
}
