export const unwrapApiResponse = (response, fallback = undefined) => {
  const payload = response?.data
  return payload?.data || payload || fallback
}
