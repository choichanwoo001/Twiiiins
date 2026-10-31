import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    include: ['tests/unit/**/*.test.js'],
    clearMocks: true,
    restoreMocks: true,
    coverage: {
      provider: 'v8', reporter: ['text', 'json-summary', 'lcov', 'html'],
      include: ['src/utils/apiCache.js', 'src/utils/downloadLinks.js', 'src/utils/newsLanguage.js',
        'src/services/apiResponse.js', 'src/services/payloadMappers.js',
        'src/composables/useWebsitePreferences.js', 'src/composables/useAdminRequestStatus.js', 'src/components/common/NewsletterSignup.vue'],
      thresholds: { lines: 80, branches: 70 },
    },
  },
})
