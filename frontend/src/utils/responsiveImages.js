const DEFAULT_IMAGE_WIDTHS = [640, 1280, 1920]
const IMAGE_EXTENSIONS = /\.(jpe?g|png|webp)$/i

export const canUseImageVariants = (imageUrl) => {
  if (!imageUrl || imageUrl.startsWith('data:')) {
    return false
  }

  const cleanUrl = imageUrl.split('?')[0].split('#')[0]
  return cleanUrl.includes('/image/') && !cleanUrl.includes('/image/variants/') && IMAGE_EXTENSIONS.test(cleanUrl)
}

export const getImageVariantUrl = (imageUrl, width) => {
  if (!canUseImageVariants(imageUrl)) {
    return imageUrl
  }

  const [baseWithQuery, hash = ''] = imageUrl.split('#')
  const [baseUrl, query = ''] = baseWithQuery.split('?')
  const lastSlash = baseUrl.lastIndexOf('/')
  const folder = baseUrl.slice(0, lastSlash)
  const filename = baseUrl.slice(lastSlash + 1)
  const dot = filename.lastIndexOf('.')
  const name = dot > 0 ? filename.slice(0, dot) : filename
  const variantUrl = `${folder}/variants/${name}-${width}.jpg`
  const querySuffix = query ? `?${query}` : ''
  const hashSuffix = hash ? `#${hash}` : ''

  return `${variantUrl}${querySuffix}${hashSuffix}`
}

export const getImageSrcset = (imageUrl, widths = DEFAULT_IMAGE_WIDTHS) => {
  if (!canUseImageVariants(imageUrl)) {
    return ''
  }

  return widths.map(width => `${getImageVariantUrl(imageUrl, width)} ${width}w`).join(', ')
}

export const handleResponsiveImageError = (event, fallbackUrl) => {
  const image = event?.target
  if (!image || !fallbackUrl || image.dataset.fallbackApplied === 'true') {
    return
  }

  image.dataset.fallbackApplied = 'true'
  image.srcset = ''
  image.sizes = ''
  image.src = fallbackUrl
}
