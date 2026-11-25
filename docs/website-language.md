# Public website languages

The public website uses the shared `websiteLanguage` preference (`en` or `de`),
stored under `twiiiins-website-language` in local storage. English is the default.
The switch is at the right of the shared public header, including on mobile.
On the home page, the entire header is visible on the first section and hidden
on the concert section. Navigation keeps the current page. Links with an explicit
`lang` parameter retain the selected value when the visitor switches languages.
News is available at `/news`. Links to `/media?section=news` open the same page
with their original article and language parameters. News dates use
`DD.MM.YYYY` in both languages.

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
General content API source fields are preserved. News uses its existing language-specific
fields for generated variants. Text without a current translation is displayed as received.

New and edited content is now queued for server-side Gemini translation. Public API
responses include additive `translations` metadata keyed by field path (for example,
`descriptions[0]`, `reviews[0].text`). The UI prefers current completed DB translations,
then the reviewed static map above, then the original source. API source fields remain
unchanged except News' existing English/German fields, which receive generated variants.

See [automatic translation setup and operations](translations.md). Static interface copy,
PDFs/files, personal names, artist names, URLs and email addresses are not translated.
There is no automatic backfill of untouched existing content.

Validation:

```sh
cd frontend
npm run build
npx playwright test tests/website-language.spec.js tests/newsletter.spec.js
npx playwright test --config playwright.preview.config.js
```
