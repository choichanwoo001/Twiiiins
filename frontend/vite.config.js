import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { localDataPlugin } from './dev-data/plugin.js'

function serveNewsletterFonts(server) {
  server.middlewares.use('/newsletter-fonts/', (_request, response, next) => {
    response.setHeader('Access-Control-Allow-Origin', '*')
    next()
  })
}

export default defineConfig(({ command, mode }) => ({
  plugins: [
    vue(),
    {
      name: 'newsletter-font-cors',
      configureServer: serveNewsletterFonts,
      configurePreviewServer: serveNewsletterFonts
    },
    ...(command === 'serve' && loadEnv(mode, process.cwd(), '').VITE_DUMMY_DATA === 'true'
      ? [localDataPlugin({ originalPhotoPreview: loadEnv(mode, process.cwd(), '').VITE_ORIGINAL_PHOTO_PREVIEW === 'true' })] : [])
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: true,
    port: 5173,
    proxy: {
      '/uploads': { target: 'http://localhost:8080', changeOrigin: true },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}))

