# 콘텐츠 자동 번역

콘텐츠 저장과 같은 DB 트랜잭션에서 번역 작업을 등록합니다. Gemini 호출은 별도 스케줄러에서 수행하며 원문 저장 응답을 지연시키지 않습니다. 기존 콘텐츠를 일괄 번역하지 않습니다. 공연의 과거/예정 상태 자동 이동도 번역 작업을 만들지 않습니다.

## 로컬 설정

`backend/.env.translation.example`을 `backend/.env.translation.local`로 복사해 `GEMINI_API_KEY`를 입력합니다. 로컬 파일은 Git에서 제외됩니다. `scripts/start-local.ps1` 또는 backend 디렉터리에서 실행한 Spring Boot가 파일을 읽습니다. 배포 환경 변수가 로컬 파일보다 우선합니다.

```dotenv
GEMINI_API_KEY=
GEMINI_MODEL=gemini-3.5-flash-lite
TRANSLATION_ENABLED=true
TRANSLATION_TIMEOUT_SECONDS=30
```

키가 없거나 기능이 꺼져 있으면 콘텐츠를 저장하고 작업은 `DISABLED`로 남습니다. 설정 후 서버를 재시작하면 저장되어 있던 비활성 작업도 처리합니다. API 키를 `VITE_` 변수에 넣지 않습니다.

## 배포

키를 입력한 로컬 파일의 네 항목을 같은 이름의 GitHub Actions repository secrets로 등록합니다. GitHub CLI가 인증되어 있다면 `scripts/register-translation-secrets.ps1`로 값이 터미널에 표시되지 않게 등록할 수 있습니다. 이 스크립트는 네 가지 번역 설정만 등록하며 배포는 실행하지 않습니다.

기존 배포 워크플로가 Secrets → 서버 `.env` → Docker Compose → Spring 설정으로 전달합니다. 운영 DB 백업 후 `scripts/migrate-translations.sql`을 적용합니다. 마이그레이션은 반복 실행 가능하며 기존 테이블/콘텐츠는 변경하지 않습니다. 기능을 끄려면 `TRANSLATION_ENABLED=false`로 변경한 뒤 배포합니다. 번역 데이터는 유지됩니다.

## 관리자 사용

모든 관리자 섹션 상단의 **자동 번역** 패널에서 최근 작업과 상태를 확인합니다. 펼치면 종류·콘텐츠 ID로 조회하고 페이지를 넘길 수 있습니다. 실패/비활성 작업은 설정이 정상일 때 재시도할 수 있습니다. 영어·독일어 값을 수정하면 바꾼 언어만 수동 작성으로 보존합니다. 새로고침 후 최신 버전을 기준으로 수정해야 하며 충돌 시 저장을 거부합니다.

- `PENDING`: 대기 또는 지연 재시도 중
- `PROCESSING`: Gemini 호출 중. 180초 임대가 만료되면 다른 작업자가 복구할 수 있습니다.
- `COMPLETED`: 현재 원문에 맞는 번역 또는 양쪽 수동 입력
- `FAILED`: 재시도 소진 또는 인증/요청 설정 오류
- `DISABLED`: 키 미설정 또는 기능 꺼짐

일시적 연결 오류·429·5xx·잘못된 응답은 최초 호출 뒤 최대 3회(30초, 120초, 600초 간격) 재시도합니다. 인증/권한 등 다른 4xx는 즉시 실패합니다. 로그와 관리자 응답에는 API 키나 Gemini 원시 오류 응답을 기록하지 않고 오류 코드만 표시합니다.

원문 해시와 작업 버전을 검사하므로 변경/삭제된 콘텐츠나 수동 편집을 오래된 응답이 덮어쓰지 않습니다. 배열은 인덱스를 포함한 필드 경로로 관리하고 내용/순서 변경 시 해당 경로를 갱신합니다. HTML은 정제한 뒤 태그 구조와 링크 보존을 검사합니다. 번역 품질은 관리자가 확인·수정할 수 있습니다.

일반 콘텐츠는 원본 필드를 유지하고 `translations` 메타데이터를 추가합니다. 뉴스·뉴스레터는 기존 언어별 제목/본문/행사/버튼 필드에 결과도 반영하므로 저장된 글의 미리보기·발송에 적용됩니다. 이미 대기열에 등록한 메일의 HTML 스냅샷은 바꾸지 않습니다. 번역 완료 전에 등록한 메일은 당시 사용할 수 있는 원문/번역으로 발송됩니다. 관리자 작성 폼을 열어 둔 사이 번역이 완료되면 글 버전이 변경되어 재조회가 필요할 수 있습니다.

## API

- `GET /api/admin/translations?kind=projects&contentId=1&page=0`: 관리자 전용, 페이지당 100개. 응답은 `available`, `items`입니다.
- `POST /api/admin/translations/{id}/retry`: 실패/비활성 작업을 재등록합니다.
- `PUT /api/admin/translations/{id}`: `{version, en, de}`로 번역을 수정합니다. 오래된 버전은 409입니다.
- 콘텐츠 API 응답의 `translations[fieldPath]`: `state`와 완료된 `en`, `de`를 포함합니다. 원문과 일치하는 결과만 공개합니다.

## 검증

```powershell
cd backend
.\gradlew.bat test --no-daemon
cd ../frontend
npm run build
npx playwright test tests/translation.spec.js tests/website-language.spec.js tests/newsletter.spec.js
```

자동 테스트는 Gemini 모의 응답을 사용해 과금 없이 동시 변경·장애·재시도·HTML·언어 선택·관리자 권한을 검증합니다. 실제 API와 운영 배포 확인에는 사용자의 키 등록이 필요합니다.
