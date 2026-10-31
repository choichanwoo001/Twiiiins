import { defineConfig } from '@playwright/test'
const nightly = process.env.TEST_SUITE === 'nightly'
export default defineConfig({
  testDir: './tests/integration', fullyParallel: false, workers: 1,
  forbidOnly: !!process.env.CI, retries: nightly ? 1 : 0, failOnFlakyTests: true,
  timeout: 60000, expect: { timeout: 15000 },
  reporter: [['list'], ['html', { open: 'never', outputFolder: '.cache/integration-report' }],
    ['junit', { outputFile: '.cache/integration.xml' }]],
  use: { baseURL: 'http://127.0.0.1:18080', trace: 'retain-on-failure', screenshot: 'only-on-failure',
    ...(process.env.TWIIIINS_BROWSER_WS_ENDPOINT ? { connectOptions: { wsEndpoint: process.env.TWIIIINS_BROWSER_WS_ENDPOINT, exposeNetwork: '<loopback>' } } : {}),
  },
  projects: [
    { name: 'chromium-desktop', use: { browserName: 'chromium', viewport: { width: 1366, height: 900 } } },
    { name: 'chromium-mobile', use: { browserName: 'chromium', viewport: { width: 390, height: 844 }, isMobile: true, hasTouch: true } },
    ...(nightly ? ['firefox', 'webkit'].map(browserName => ({ name: browserName, use: { browserName, viewport: { width: 1366, height: 900 } } })) : []),
  ],
})
