const DROPBOX_HOSTS = new Set(['dropbox.com', 'www.dropbox.com', 'dl.dropboxusercontent.com'])
export function isDropboxUrl(value) {
  try {
    const url = new URL(value)
    return url.protocol === 'https:' && DROPBOX_HOSTS.has(url.hostname) && !url.username && !url.password && !url.port && url.pathname !== '/'
  } catch { return false }
}
