import { defineConfig } from '@playwright/test'
import base from './playwright.config.js'
export default defineConfig({
  ...base, outputDir: './.cache/newsletter-preview-tests',
  testIgnore: [], testMatch: '**/newsletter-preview.spec.js',
  use: { ...base.use, baseURL: 'http://127.0.0.1:4179' },
  webServer: { ...base.webServer,
    command: 'npm run dev -- --host 127.0.0.1 --port 4179 --strictPort',
    url: 'http://127.0.0.1:4179',
    env: { VITE_API_BASE_URL: '/api', VITE_DUMMY_DATA: 'true' }
  }
})
