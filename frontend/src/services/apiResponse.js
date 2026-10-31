export const unwrapApiResponse = (response, fallback = undefined) => {
  const payload = response?.data
  if (payload && typeof payload === 'object' && Object.hasOwn(payload, 'data')) return payload.data
  return payload ?? fallback
}
