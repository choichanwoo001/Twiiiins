import { beforeEach, describe, expect, it, vi } from 'vitest'
import { apiCache } from '../../src/utils/apiCache'
import { concertService } from '../../src/services/concertService'
import { musicService } from '../../src/services/musicService'
import { videoService } from '../../src/services/videoService'
import { equipmentService } from '../../src/services/equipmentService'
import { contactService } from '../../src/services/contactService'
import { downloadFileService } from '../../src/services/downloadFileService'
import { newsService } from '../../src/services/newsService'
import { photoService } from '../../src/services/photoService'
import { projectService } from '../../src/services/projectService'
import { uploadService } from '../../src/services/uploadService'
vi.mock('../../src/utils/errorHandler', () => ({ logError: vi.fn(), getErrorMessage: error => error.message }))
const api = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }))
vi.mock('../../src/api/axios', () => ({ default: api }))
beforeEach(() => {
  apiCache.deletePattern('.*')
  for (const fn of Object.values(api)) fn.mockReset().mockResolvedValue({ data: { success: true, data: { id: 1 } } })
})
const cases = [
  [concertService, 'Concert', 'Concerts', '/concerts', { name: 'Concert', location: 'Berlin', date: '2026-10-31' }],
  [musicService, 'Music', 'Music', '/media/music', { title: 'Track', artist: 'TWIIIINS', coverUrl: '/uploads/a.png' }],
  [videoService, 'Video', 'Videos', '/media/videos', { title: 'Film', embedUrl: 'https://example.com/video' }],
  [equipmentService, 'Equipment', 'Equipment', '/media/equipment', { name: 'Violin' }],
  [contactService, 'Contact', 'Contacts', '/media/contacts', { name: 'Booking', email: 'booking@example.com' }],
  [downloadFileService, 'DownloadFile', 'DownloadFiles', '/media/download-files', { name: 'Rider', fileUrl: '/uploads/rider.pdf' }],
  [newsService, 'News', 'News', '/media/news', { title: 'News', date: '2026-10-31' }],
  [projectService, 'Project', 'Projects', '/projects', { title: 'Project', urlSlug: 'project', premiereDate: '2026-10-31' }],
  [photoService, 'PhotoGroup', 'PhotoGroups', '/media/photos/groups', { title: 'Photos' }],
]
describe.each(cases)('%s transport contract', (service, singular, plural, path, input) => {
  it('sends create, update, detail and delete requests to real endpoint shapes', async () => {
    await service[`create${singular}`](input); expect(api.post).toHaveBeenCalledWith(path, expect.objectContaining(input))
    await service[`update${singular}`](1, input); expect(api.put).toHaveBeenCalledWith(`${path}/1`, expect.objectContaining(input))
    if (singular === 'Project') {
      await service.getProjectBySlug('project'); expect(api.get).toHaveBeenCalledWith('/projects/slug/project')
    } else if (singular === 'Video') {
      await service.getVideo(1); expect(api.get).toHaveBeenCalledWith(`${path}/1`)
    } else {
      await service[`get${singular}ById`](1); expect(api.get).toHaveBeenCalledWith(`${path}/1`)
    }
    await service[`delete${singular}`](1); expect(api.delete).toHaveBeenCalledWith(`${path}/1`)
  })
  it('does not hide transport failures as successful mutations', async () => {
    api.post.mockRejectedValueOnce(new Error('offline'))
    await expect(service[`create${singular}`](input)).rejects.toThrow()
    api.delete.mockRejectedValueOnce(new Error('offline'))
    await expect(service[`delete${singular}`](1)).rejects.toThrow()
    api.put.mockRejectedValueOnce(new Error('offline'))
    await expect(service[`update${singular}`](1, input)).rejects.toThrow()
  })
  it('normalizes search filters', async () => {
    if (singular === 'Project') {
      await service.getAllProjects(); expect(api.get).toHaveBeenLastCalledWith('/projects'); return
    }
    const search = singular === 'Project' ? service.getAllProjects : service[`search${plural}`]
    await search({ title: ' Hello ', empty: '' })
    expect(api.get).toHaveBeenLastCalledWith(singular === 'PhotoGroup' ? '/media/photo-groups' : path, { params: { title: 'Hello' } })
  })
})

it.each(['createConcert', 'updateConcert', 'deleteConcert', 'moveToPastEvent', 'moveToUpcomingEvent', 'triggerAutoMove'])('%s invalidates the cached list', async action => {
  api.get.mockResolvedValue({ data: { success: true, data: [] } })
  await concertService.getAllConcerts(); await concertService.getAllConcerts(); expect(api.get).toHaveBeenCalledTimes(1)
  const input = cases[0][4]
  if (action === 'createConcert') await concertService[action](input)
  else if (action === 'updateConcert') await concertService[action](1, input)
  else await concertService[action](1)
  await concertService.getAllConcerts(); expect(api.get).toHaveBeenCalledTimes(2)
})
it('does not invalidate committed cache when a write fails', async () => {
  await concertService.getAllConcerts(); api.delete.mockRejectedValueOnce(new Error('offline'))
  await expect(concertService.deleteConcert(1)).rejects.toThrow()
  await concertService.getAllConcerts(); expect(api.get).toHaveBeenCalledTimes(1)
})
it('rejects incomplete batch upload results and supports a successful retry', async () => {
  const files = [new File(['a'], 'a.png', { type: 'image/png' }), new File(['b'], 'b.png', { type: 'image/png' })]
  api.post.mockResolvedValueOnce({ data: { data: { urls: ['/a.png'], errors: ['b failed'] } } })
  await expect(uploadService.uploadImages(files)).rejects.toThrow('Some images failed')
  api.post.mockResolvedValueOnce({ data: { data: { urls: ['/a.png', '/b.png'], errors: [] } } })
  expect(await uploadService.uploadImages(files)).toEqual(['/a.png', '/b.png'])
  expect(api.post.mock.calls.at(-1)[1].getAll('files')).toHaveLength(2)
})
it('requires a URL from single uploads', async () => {
  await expect(uploadService.uploadImage(new File(['a'], 'a.png'))).rejects.toThrow('URL')
  api.post.mockResolvedValueOnce({ data: { data: { url: '/uploads/a.pdf' } } })
  expect(await uploadService.uploadFile(new File(['pdf'], 'a.pdf'))).toBe('/uploads/a.pdf')
})
