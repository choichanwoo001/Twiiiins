import { defineConfig } from '@playwright/test'
export default defineConfig({
  testDir: './tests', testMatch: '*.spec.js', testIgnore: ['**/newsletter-preview.spec.js', '**/visual.spec.js', '**/integration/**'], fullyParallel: false, workers: 1,
  forbidOnly: !!process.env.CI, retries: 0,
  reporter: [['list'], ['html', { open: 'never', outputFolder: '.cache/playwright-report' }], ['junit', { outputFile: '.cache/playwright.xml' }]],
  use: { baseURL: 'http://127.0.0.1:4178', trace: 'retain-on-failure', screenshot: 'only-on-failure' },
  projects: [{ name: 'desktop', use: { viewport: { width: 1366, height: 900 } } }, { name: 'mobile', use: { viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true } }],
  webServer: { command: 'npm run dev -- --host 127.0.0.1 --port 4178 --strictPort', url: 'http://127.0.0.1:4178', reuseExistingServer: false,
    env: { VITE_API_BASE_URL: '/api', VITE_DUMMY_DATA: 'false' } }
})
