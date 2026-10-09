import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { localDataPlugin } from './dev-data/plugin.js'

export default defineConfig(({ command, mode }) => ({
  plugins: [
    vue(),
    ...(command === 'serve' && loadEnv(mode, process.cwd(), '').VITE_DUMMY_DATA === 'true'
      ? [localDataPlugin()] : [])
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

