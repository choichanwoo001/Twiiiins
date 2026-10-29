# Public website languages

The public website uses the shared `websiteLanguage` preference (`en` or `de`),
stored under `twiiiins-website-language` in local storage. English is the default.
The switch lives beside the social links, or in the lower left on pages without
social links. Navigation URLs do not change with the language.

Fixed interface copy is in `frontend/src/composables/useWebsiteTranslation.js`.
About, privacy, cookie, shop and newsletter copy also retain their existing
authored English/German translations.

`frontend/src/translations/content.json` contains reviewed translations of the
public content retrieved on 20 October 2026 from the projects, concerts, news,
contacts, download files, videos and photo groups APIs at twiiiins.com, with
the local seed content cross-checked. Equipment had no public entries.

Translations are keyed by content kind, field and exact source text. Names,
work titles, brands, venues, addresses, URLs and email addresses are preserved.
Existing authored newsletter titles/bodies take precedence over saved copy.
Original API data is never rewritten. New or edited text without a matching
translation is displayed as received, avoiding stale translations.

To translate later content, add its exact source and reviewed `en`/`de` values
to the relevant kind and field in the JSON file. There is no translation service,
API key, automatic translation job, database migration or additional admin input.

Validation:

```sh
cd frontend
npm run build
npx playwright test tests/website-language.spec.js tests/newsletter.spec.js
npx playwright test --config playwright.preview.config.js
```
