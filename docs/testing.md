# 테스트 실행과 검증 범위

## 실행

Java 17(`JAVA_HOME`), Node 22, Python 3.12 이상을 준비한다. Linux/macOS에서는 최초 `chmod +x backend/gradlew`를 실행한다. 프론트엔드에서 최초 `npm ci`, 브라우저 검증 전 `npx playwright install chromium`을 실행한다. Windows에서는 Gradle 명령을 `gradlew.bat`으로 실행한다.

```powershell
# 저장소 루트: Docker와 운영 비밀값 없이 빠른 검증
python scripts/test-fast.py

# 기존·신규 브라우저, 미리보기, 시각 회귀까지 포함
python scripts/test-fast.py --browser
```

```powershell
# Docker 엔진이 실행 중일 때 전체 실제 연동 검증
cd backend
./gradlew test mysqlTest jacocoTestReport bootJar --no-daemon
cd ../frontend
npm ci
npm run build
npx playwright install chromium
cd ..
python scripts/test-migrations.py
python scripts/test-stack.py run

# 고정된 공식 Playwright Linux 이미지에서 단위·빌드·브라우저·미리보기·시각 검증
python scripts/test-linux-browser.py

# 전체 필수 묶음 3회 반복: 새 DB·메일함으로 매회 시작
python scripts/repeat-tests.py --repeat 3

# 대표 결함을 임시로 주입해 테스트 탐지 확인 (원본 바이트 자동 복원)
python scripts/prove-test-guards.py
python scripts/test-fast.py --browser
```

개별 프론트엔드 명령: `npm test`, `npm run test:coverage`, `npm run test:e2e`, `npm run test:preview`, `npm run test:visual`, `npm run test:mutation`. 실제 연동은 스택을 먼저 올린 후 `npm run test:integration`으로도 실행할 수 있다. 기본 브라우저 묶음에는 실제 서버 테스트가 섞이지 않는다.

Linux/macOS 야간 검증은 `TEST_SUITE=nightly python scripts/test-stack.py run`으로 실행한다. PowerShell은 먼저 `$env:TEST_SUITE='nightly'`를 설정한다. Chromium·Firefox·WebKit 설치가 필요하다. 야간 재시작 테스트의 제한 시간은 일반 테스트보다 길다.

Windows 브라우저 실행에 제약이 있으면 `python scripts/test-stack.py run --linux-browsers`로 동일 버전의 공식 Linux 브라우저를 사용한다. 브라우저 서버는 localhost `18081`에만 열리고 실행 종료 시 제거한다. Playwright의 loopback forwarding으로 테스트 서버에 연결하므로 실제 운영 URL을 사용하지 않는다.

관리자 쓰기 요청에는 공통 작업 상태가 표시된다. 등록·수정·삭제·업로드·이동·번역 재시도·발송 등록의 처리 중/성공/실패를 표시하고, 같은 작업의 재시도는 이전 실패 표시를 교체한다. 뉴스레터 발송 등록 성공은 발송 대기열 접수이며 실제 수신 완료를 의미하지 않는다. 자동 갱신하는 메일 미리보기는 자체 상태를 사용한다. 서로 겹치는 요청과 역순 완료도 별도로 검증한다.

## 환경 격리

- 실제 연동은 `tests/compose.yml`의 **twiiiins-test** 프로젝트만 사용한다. 개발·운영 Compose를 호출하지 않는다.
- `--env-file tests/stack.env`로 개발자의 `.env` 자동 로딩을 막는다. 테스트 프로필은 백엔드 dotenv 로딩도 차단한다.
- MySQL은 tmpfs, 업로드는 테스트 컨테이너 내부 `/tmp`, SMTP는 Mailpit을 사용한다. 백엔드·DB·번역 스텁은 외부 연결이 차단된 내부 네트워크만 사용한다. 웹과 Mailpit에는 호스트의 localhost 포트 공개를 위한 별도 ingress 네트워크도 연결한다. Mailpit 버전 조회는 끈다.
- 테스트용 주소·계정·키는 합성 값이다. 실제 구독자·운영 DB·SMTP·Gemini 키가 필요하지 않다.
- 테스트 포트는 웹 `127.0.0.1:18080`, 메일함 `127.0.0.1:18025`다. 개발 환경 포트와 다르다.
- `test-stack.py run`은 실패해도 로그를 수집하고 자신이 만든 스택을 내린다. 수동 실행 후 정리는 `python scripts/test-stack.py down`으로 한다.
- 같은 머신에서 스택 명령을 동시에 실행하지 않는다. 기능 테스트는 직렬 실행하고 동시성은 명시적인 MySQL 테스트에서 검증한다.

## 기능별 검증 목록

`모의 UI`는 Playwright API 모의 응답 또는 Vue 컴포넌트 테스트, `서버`는 MockMvc와 실제 DB, `연동`은 실제 HTTP 서버·빌드된 웹·MySQL·메일함을 의미한다. **테스트 코드가 존재하는 것과 실제 환경에서 통과한 것은 구분한다.**

| 기능 / API | 모의 UI·단위 | 서버 H2 / MySQL | 컨테이너 연동 |
|---|---|---|---|
| `/`, `/about`, `/projects`, `/concerts`, `/media`, `/shop`, `/contact`, `/privacy`, `/login` | 직접 진입·새로고침·탐색·레이아웃 | 해당 공개 API 조회 | 주요 콘텐츠 공개 반영 |
| `/api/auth/login`, `/me`, `/logout` | 로그인 실패·재시도·중복 방지·라우터 가드 | 실제 로그인·만료 경계·로그아웃·위조 토큰 | 관리자 UI 로그인·생성·새로고침·로그아웃 |
| `/api/projects`, `/{id}`, `/slug/{slug}`, `/{id}/images` | payload·캐시·관리 폼·상세 실패 복구·응답 경합 | CRUD·slug·이미지 교체·검색·날짜 경계·잘못된 매개변수 | 공개 상세·수정·새로고침·삭제 |
| `/api/concerts`, `/upcoming`, `/past`, `/{id}`, 이동 API | payload·검색 요청·관리 폼·변경 후 캐시 무효화 | CRUD·기준일 직전/당일/이후·양방향 이동 | UI 생성과 공개 표시·수정·삭제 |
| `/api/media/music`, `/videos`, `/equipment`, `/contacts`, `/download-files` 및 `/{id}` | 각 전송 계약·폼 중복 방지·실패 입력 유지·재시도 | 각각 CRUD·잘못된 입력·공개 재조회 | 각각 실제 HTTP CRUD; 공개된 화면은 브라우저 확인 |
| `/api/media/photo-groups`, `/photos/groups`, 사진·그룹 상세 API | 갤러리 비율·반응형·그룹 폼 | 다중 업로드·메타데이터·개별/그룹 삭제·cascade | 파일 HTTP 조회·공개 갤러리·새로고침·삭제 |
| `/api/upload/image`, `/images`, `/file` | 진행률·실패 복구·불완전 배치 응답 | 빈 파일·잘못된 MIME·손상 이미지·용량·경로 이탈·리사이징 비율 | 실제 사진 업로드와 제공 |
| `/api/media/news`, 상세·이미지 API | 일반 News 편집·실패·재시도·삭제·버전 전달 | 일반 News와 뉴스레터 구분·게시·HTML 정제·수정 | 실제 CRUD와 공개 반영 |
| `/api/newsletter/status`, `/subscribe`, `/confirm`, `/unsubscribe` | 가용성 오류·동의·중복 제출·언어·명시적 확인 | 구독·중복·언어·레거시 링크·탈퇴·재구독·동의 기록·제한 경계 | 선택 언어로 Mailpit 수신·개인 탈퇴 링크·탈퇴자 발송 제외 |
| `/api/admin/news`, 상세·이미지·publish·send·preview·test | 저장·미리보기·늦은 응답·발행·발송·삭제·오류 복구 | 초안 비공개·단일 언어·낙관적 버전·중복 발송·스냅샷 | 발행·공개 반영·발송 중복 요청·실제 SMTP 수신 |
| `/api/admin/newsletter/*` 설정·구독자·mailings·deliveries·exclude·resume·retry | 상태·검색·설정 진단 | 비밀값 비노출·대상 제외·재시도·쿼터·300명 배치·재시작 상태 | 메일링 결과·수신자 상태·중복 메일 확인 |
| `/api/admin/translations`, `/{id}`, `/{id}/retry` | 상태·수동 수정·재시도·언어 fallback | 원문/수동값 보존·롤백·늦은 응답·구조 보존·HTTP 오류·시간 초과 | 로컬 Gemini HTTP 응답·공개 번역·재시작 후 수동값 유지 |
| 언어·미디어 동의 | 손상/만료/미래 저장값·저장소 오류·언어 영속성 | 해당 없음 | 모의 브라우저에서 동의 전 외부 요청 0건·동의/철회·포커스 복귀 |
| Shop | 가격·배송 안내·mailto 주소/제목·영독 전환 | 해당 없음 | 별도 결제 기능 없음 |

모든 Spring MVC **보호된 쓰기 매핑**을 런타임에서 수집해 익명·위조 토큰의 401 응답을 검증한다. 새 쓰기 엔드포인트도 자동으로 이 검사에 포함된다. 공개 구독·확인·탈퇴와 로그인은 이 검사에서 제외하고 각각 별도로 검증한다.

MySQL 묶음은 공통 CMS 계약과 기존 뉴스레터·번역 계약 전체를 같은 테스트로 다시 실행한다. 동시 구독은 4개 스레드로 동시에 요청하고 구독자·웰컴 작업이 하나인지 확인한다. Docker 미실행 시 자동 skip하지 않고 실패한다.

## CI·배포와 품질 기준

- PR: `Verify`의 backend / frontend / integration 성공을 `required`에서 합산한다. 실패·취소·skip 모두 성공으로 인정하지 않는다.
- main/master 배포: 동일 커밋에서 같은 재사용 워크플로를 통과한 뒤 빌드·배포한다. 백엔드 이미지와 배포 설정은 커밋 SHA를 사용한다.
- 매일 04:00 KST: 다중 브라우저, 재시작, 모의 UI, 접근성·레이아웃·시각 회귀.
- 매주 월요일 05:00 KST: 인증·중복 제출·캐시 결함 탐지 증명, 필수 묶음 3회, mutation testing. 300명 발송·다양한 크기의 이미지 검증도 필수 서버 묶음에 포함된다. 장기 진단이 필요할 때만 `--repeat 20`을 수동 사용한다.
- PR 재시도는 0회다. 야간 실제 연동만 진단용 1회 재시도하며, 재시도로 통과해도 `failOnFlakyTests`로 실패 처리한다.
- 리포트·스크린샷·trace·로그 보관은 14일이다. 로컬 위치는 `backend/build/reports`, `frontend/.cache`, `frontend/coverage`, `frontend/reports/mutation`, `.local/test-artifacts`다.
- 프론트엔드 coverage의 명시적 대상은 payload/응답 변환, 캐시, 다운로드 링크, 뉴스 언어, 사이트 설정, 관리자 요청 상태, 구독 폼이다. 이 수치를 전체 Vue 화면 coverage로 해석하지 않는다. 줄 80%·분기 70% 하한과 별도의 체크인 기준선 하락 방지를 적용한다.
- 백엔드는 전체 JaCoCo 기준선을 유지하며, 검증기·토큰·인증 필터·요청 제한의 핵심 집합에는 줄 80%·분기 70% 하한을 추가한다. DTO·생성 코드가 포함된 전체 분기 수치를 함께 보고한다.
- 기준선은 `tests/coverage-baseline.json`에 있다. CI가 자동으로 낮추거나 갱신하지 않는다. 범위 변경 시 리뷰에서 이유와 실제 측정치를 확인한다.
- 시각 기준 이미지는 사진 갤러리와 상품 이미지의 안정적인 영역만 포함한다. 갱신은 로컬 `npx playwright test -c playwright.visual.config.js --update-snapshots` 후 diff 검토로 수행한다. Linux CI에서 자동 갱신하지 않는다.

### 저장소 보호 규칙

연결된 도구에는 branch protection/ruleset 변경 기능이 없어 원격 규칙은 변경하지 않았다. GitHub Settings → Rules → Rulesets에서 main/master에 다음을 설정한다.

1. PR을 통해서만 병합하고 필수 상태 검사에 Verify의 `required` 검사(첫 실행 후 실제 표시 이름 확인)를 지정한다.
2. 최신 커밋에서 검사가 통과하도록 요구하고 강제 푸시·브랜치 삭제를 제한한다.
3. 관리자 포함 우회 권한은 저장소 운영 정책에 맞게 제한한다.

워크플로 자체의 배포 차단은 이 원격 규칙과 별개로 적용된다. 원격 설정의 실제 적용 여부는 완료로 표시하지 않는다.

## 확인 상태와 후속 검토

2026-10-31 Docker Desktop 복구 후 실제 검증: 백엔드 H2·단위 170건, MySQL Testcontainers 122건, 프론트엔드 단위·컴포넌트 244건, 브라우저 112건, 미리보기 6건, 시각 회귀 4건, Python 9건 통과. 실제 MySQL CLI 마이그레이션은 합성 레거시 데이터 보존과 SQL 재실행을 통과했다. Chromium 데스크톱/모바일·Firefox·WebKit 실제 연동 48건은 재시도 없이 통과했고, 백엔드 재시작 후 데이터·수동 번역 보존도 확인했다. Linux 공식 브라우저 이미지에서 프론트엔드 전체 묶음과 시각 기준의 일치도 확인했다. 원격 GitHub Actions 자체는 아직 실행하지 않았다.

반복 검증 완료 기준은 사용자 합의에 따라 전체 필수 묶음 **3회 연속 통과**로 변경했다. 주간 CI와 로컬 기본값도 3회다. 수정 후 전체 687건 × 3회 = **2,061건**을 실패·오류·skip·테스트 재시도 없이 통과했다. 매회 빌드·커버리지 기준·실제 SQL 마이그레이션·새 컨테이너 E2E도 통과했다. 이전 실행 결과는 합산하지 않았다. 회차별 XML·로그·건수는 `.local/test-artifacts/repeat-20261031T055619Z/summary.json`, 최종 집계는 `.local/test-artifacts/completion.json`에 있다. 검증 대상 322개 파일의 해시가 실행 중 바뀌지 않은 것도 확인했다. 공개 페이지·API·정적 파일의 읽기 전용 스모크도 통과했다.

이전 반복은 3회 통과 후 4회차 모바일 언어 테스트가 30초 제한으로 실패했다. 한 테스트에 페이지 이동 11회와 여러 기능 검증이 몰려 있었고, trace의 앞선 이동·검증은 정상 완료됐다. 언어 테스트를 프로젝트/공연/연락처, 미디어/Shop, 홈/수신 거부의 3개 흐름으로 나눴다. 4개 화면 너비를 한 테스트에서 확인하던 레이아웃 검증도 너비별로 분리했다. 기존 검증과 기본 30초 제한은 유지했다. 관련 모바일 검증은 3회 반복 69건을 통과했으며 원래 실패 trace와 로그는 `.local/test-artifacts/browser-timeout-investigation`에 보관한다.

실제 번역 워커와 콘텐츠 삭제가 경합할 때 발생하던 낙관적 잠금 500 오류를 수정하고 MySQL 회귀 테스트를 추가했다. 수정 전 삭제 방식을 임시로 복원하면 새 테스트가 같은 낙관적 잠금 오류로 실패하는 것도 확인하고 수정 코드를 복원했다. 핵심 순수 로직 mutation score는 97%이며, 인증 검사·중복 제출 방지·캐시 무효화 제거 결함 3종도 관련 테스트가 탐지했다. 관리자 작업 상태 재점검과 추가 보강은 [관리자 작업 상태 재점검](admin-operation-audit.md)에 기록했다.

다음은 기존 제품 계약을 임의로 바꾸지 않은 영역이다.

- 일반 CMS의 동시 수정은 현재 버전 계약을 따른다. 모든 엔티티에 새 충돌 감지 정책을 추가하지 않았다.
- 장비는 현재 공개 화면 기능 플래그가 꺼져 있어 연동 테스트는 실제 CRUD를 확인하고 공개 장비 탭은 요구하지 않는다.
- 사진 다중 업로드 중 일부 실패 시 DB 트랜잭션과 이미 저장된 파일의 원자성, 콘텐츠 삭제 후 원본 파일 보관 정책은 별도 설계가 필요하다. 현재 파일 삭제 정책을 테스트 추가만으로 변경하지 않았다.
- 실제 외부 결제·유튜브 서비스·운영 SMTP·Gemini 품질 평가는 이 회귀 테스트의 범위 밖이다.

참고: SMTP 수신함 조회는 [Mailpit 공식 API](https://mailpit.axllent.org/docs/api-v1/)를 사용한다. Testcontainers는 [Docker 엔진 호환성을 보완한 1.21.4](https://github.com/testcontainers/testcontainers-java/releases/tag/1.21.4)로 고정했다.
