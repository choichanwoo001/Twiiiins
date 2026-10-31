import { defineConfig } from '@playwright/test'
import base from './playwright.config.js'
export default defineConfig({
  ...base, testMatch: '**/visual.spec.js', testIgnore: [],
  snapshotPathTemplate: '{testDir}/visual-baselines/{projectName}/{arg}{ext}',
  expect: { toHaveScreenshot: { animations: 'disabled', maxDiffPixelRatio: 0.005 } },
})
