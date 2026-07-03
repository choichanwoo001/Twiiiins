import axios from '../api/axios'
import { unwrapApiResponse } from './apiResponse'

const uploadSingle = async (endpoint, file) => {
  const formData = new FormData()
  formData.append('file', file)

  const response = await axios.post(endpoint, formData)
  const data = unwrapApiResponse(response)
  const url = data?.url

  if (!url) {
    throw new Error('업로드 응답에 URL이 없습니다.')
  }

  return url
}

export const uploadService = {
  uploadImage(file) {
    return uploadSingle('/upload/image', file)
  },

  uploadFile(file) {
    return uploadSingle('/upload/file', file)
  },

  async uploadImages(files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })

    const response = await axios.post('/upload/images', formData)
    const data = unwrapApiResponse(response)
    const urls = data?.urls || []
    const errors = data?.errors || []

    if (errors.length > 0 || urls.length !== files.length) {
      const details = errors.length > 0 ? ` ${errors.join(', ')}` : ''
      throw new Error(`Some images failed to upload.${details}`)
    }

    if (urls.length === 0) {
      throw new Error('No uploaded image URLs were returned.')
    }

    return urls
  }
}
