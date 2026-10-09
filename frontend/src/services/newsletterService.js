import api from '../api/axios'
import { unwrapApiResponse } from './apiResponse'

const get = async (path, params) => unwrapApiResponse(await api.get(path, { params }))
const post = async (path, data = {}) => unwrapApiResponse(await api.post(path, data))
export const newsletterService = {
  available: async () => unwrapApiResponse(await api.get('/newsletter/status', { handleErrorLocally: true, timeout: 10000 })),
  subscribe: async data => unwrapApiResponse(await api.post('/newsletter/subscribe', data, { handleErrorLocally: true })),
  confirm: token => post('/newsletter/confirm', { token }),
  unsubscribe: token => post('/newsletter/unsubscribe', { token }),
  settings: () => get('/admin/newsletter/settings'),
  news: () => get('/admin/news', { source: 'NEWSLETTER' }),
  save: async data => unwrapApiResponse(await (data.id ? api.put(`/admin/news/${data.id}`, data) : api.post('/admin/news', data))),
  remove: id => api.delete(`/admin/news/${id}`),
  upload: async files => {
    const data = new FormData()
    for (const file of files) data.append('files', file)
    return unwrapApiResponse(await api.post('/admin/newsletter/images', data, { handleErrorLocally: true }))
  },
  publish: id => post(`/admin/news/${id}/publish`),
  send: (id, version) => post(`/admin/news/${id}/send`, { version }),
  previewDraft: async (data, language) => unwrapApiResponse(await api.post('/admin/newsletter/preview', { ...data, language }, { handleErrorLocally: true, timeout: 10000 })),
  preview: (id, language) => get(`/admin/news/${id}/preview`, { language }),
  test: (id, language, email) => post(`/admin/news/${id}/test`, { language, email }),
  subscribers: params => get('/admin/newsletter/subscribers', params),
  exclude: id => post(`/admin/newsletter/subscribers/${id}/exclude`),
  mailings: () => get('/admin/newsletter/mailings'),
  deliveries: id => get(`/admin/newsletter/mailings/${id}/deliveries`),
  retry: id => post(`/admin/newsletter/deliveries/${id}/retry`),
  resume: () => post('/admin/newsletter/resume')
}
