import { computed, onScopeDispose, ref, watch } from 'vue'
import { newsService } from '../services/newsService'
import { toAbsoluteUrl } from '../utils/commonHelpers'
import { logError } from '../utils/errorHandler'

function displayItem(news, expanded) {
  return { ...news, imageUrls: (news.imageUrls || []).map(url => toAbsoluteUrl(url)), expanded }
}

export function usePublicNews(newsId) {
  const publicItems = ref([])
  const linkedItem = ref(null)
  const loading = ref(false)
  const loadFailed = ref(false)
  const newsList = computed(() => linkedItem.value ? [...publicItems.value, linkedItem.value] : publicItems.value)
  let requestVersion = 0

  async function refreshNews() {
    const version = ++requestVersion
    linkedItem.value = null
    loading.value = true
    loadFailed.value = false
    const linkedId = newsId.value
    const isCurrent = () => version === requestVersion && linkedId === newsId.value
    try {
      const stories = await newsService.getAllNews()
      if (!isCurrent()) return
      publicItems.value = stories.filter(story => !story.archived)
        .map(story => displayItem(story, String(story.id) === linkedId))
    } catch (error) {
      if (!isCurrent()) return
      publicItems.value = []
      loadFailed.value = true
      logError(error, '뉴스 데이터 로드')
      return
    } finally {
      if (isCurrent()) loading.value = false
    }

    if (!/^\d+$/.test(linkedId) || publicItems.value.some(story => String(story.id) === linkedId)) return
    try {
      const story = await newsService.getNewsById(linkedId)
      if (isCurrent()) linkedItem.value = displayItem(story, true)
    } catch (error) {
      if (isCurrent() && error.response?.status !== 404) logError(error, '메일 링크의 뉴스 로드')
    }
  }

  function toggleNews(id) {
    const story = newsList.value.find(item => item.id === id)
    if (story) story.expanded = !story.expanded
  }

  watch(newsId, refreshNews, { immediate: true })
  onScopeDispose(() => { requestVersion++ })
  return { newsList, loading, loadFailed, refreshNews, toggleNews }
}
