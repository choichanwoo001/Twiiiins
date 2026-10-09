# 로컬 더미 데이터

2026-10-07 기준 https://twiiiins.com 의 **공개 조회 API**를 참고했습니다.
공연, 프로젝트, 음원, 영상, 뉴스, 사진 그룹, 연락처, 다운로드의 내용·개수·순서를 보존합니다.
장비 목록은 배포 사이트와 동일하게 비어 있습니다.

사진은 저장소의 기존 사진을 크롭해 대체했습니다. 원본과 썸네일 각각의 **실제 픽셀 크기**를
측정해 같은 크기로 저장했으며, `image-manifest.json`에서 비교할 수 있습니다.
공개 PDF 2개는 로컬에 저장했고, 영상은 기존 YouTube 링크를 사용합니다.

## 로컬 개발 실행 · 로그인과 편집 지원

프로젝트 루트에서:

```powershell
.\scripts\start-local.ps1
```

이 스크립트는 전용 로컬 MySQL(`127.0.0.1:3307/twiiiins_local`)과 백엔드를 실행하고,
DB가 비어 있을 때만 이 폴더의 더미 데이터를 넣습니다. 기존 로컬 데이터와 편집 내용은 유지합니다.
프런트엔드는 `VITE_DUMMY_DATA=false`로 실제 로컬 백엔드에 연결합니다.

- 사이트 및 관리자 로그인: http://localhost:5173/login
- 로컬 관리자: `dowon` / `1234`
- 뉴스레터 테스트 메일함: http://localhost:8025

로컬 테스트 메일은 Mailpit에만 저장됩니다. 외부 수신자에게 전달하지 않습니다.
관리자 Newsletter 메뉴에서 실제 저장·웹 게시·실시간 메일 미리보기·테스트 발송을 사용할 수 있습니다.
로그는 `.local/logs`, DB는 `.local/mysql-data`, 메일은 `.local/mailpit.db`에 보관하며 Git에서 제외합니다.
Java 17과 Node.js, Python이 필요합니다. MySQL과 Mailpit은 공식 배포본을 사용자 캐시에 내려받으며
Docker나 Windows 서비스 등록 없이 실행합니다. Java는 `JAVA_HOME` 또는 이 PC의 기존 개발용 캐시를 사용합니다.
이미 백엔드를 빌드했고 코드 변경이 없다면 `-SkipBuild`로 다시 실행할 수 있습니다.

## 백엔드 없는 화면 미리보기 · 선택 사항

```powershell
cd frontend
npm run dev:dummy
```

실제 연결에서 미리보기로 전환하려면 `frontend/.env.local`의 `VITE_DUMMY_DATA`를 `true`로 바꾸세요.
더미 모드는 로그인·저장 없이 공개 화면을 살펴보는 선택 사항입니다.
관리자 작업에는 위의 실제 로컬 연결을 사용하세요.
`start-local.ps1`을 다시 실행하면 실제 연결 설정으로 돌아갑니다.

## 로컬 MySQL에 실제 데이터 넣기

1. 로컬 MySQL의 **빈 개발 스키마**를 대상으로 Spring Boot를 한 번 실행해 테이블을 생성합니다.
2. `assets` 폴더의 파일들을 백엔드의 `FILE_UPLOAD_DIR/dummy` 폴더로 복사합니다.
   기본 저장 경로를 사용하는 경우 프로젝트 루트에서:

   ```powershell
   New-Item -ItemType Directory -Force backend/uploads/dummy
   Copy-Item -LiteralPath (Get-ChildItem frontend/dev-data/assets -File).FullName -Destination backend/uploads/dummy
   ```

3. 해당 개발 스키마에 `scripts/local-seed.sql`을 실행합니다.
   기존 데이터를 지우지 않는 일반 INSERT이며, 이미 같은 ID가 있으면 실패합니다.
   트랜잭션 전체를 오류 시 롤백하는 DB 도구로 실행하세요. 운영 DB에는 실행하지 마세요.
4. 더미 모드를 끄고 실제 로컬 백엔드로 연결합니다. 사용자/비밀번호는 시드에서 변경하지 않습니다.

## 데이터 다시 만들기

Python 3 + Pillow가 필요합니다.

```powershell
python -m pip install Pillow
python scripts/prepare-local-data.py
# 배포 사이트의 최신 공개 데이터와 이미지 크기까지 갱신하려면:
python scripts/prepare-local-data.py --refresh
```

기본 실행은 저장한 `reference.json`을 사용합니다. 공개 PDF가 이미 저장되어 있으면
다시 받지 않습니다. `--refresh`는 공개 조회만 수행하며 운영 서버에 쓰지 않습니다.
생성되는 파일은 `fixtures.json`, `image-manifest.json`, `assets/`, `scripts/local-seed.sql`입니다.
공연의 지난 공연 여부는 스냅샷 시점 그대로 유지합니다.

## 뉴스레터 구독 폼 미리보기

개발 실행에서 `VITE_DUMMY_DATA=true`이면 Contact의 구독 폼을 미리 볼 수 있습니다.
이메일 형식과 동의 여부를 확인하고 제출 완료 안내까지 체험할 수 있지만,
구독 API 요청, 입력 정보 저장, 실제 이메일 발송은 수행하지 않습니다.
운영 빌드는 이 미리보기를 사용하지 않고 실제 뉴스레터 상태 API를 확인합니다.
실제 서비스에서는 구독 비활성과 연결 오류를 구분하며 연결 오류 시 다시 시도할 수 있습니다.

## 뉴스레터 작성·이메일 디자인 체험

더미 모드에서 `/dev/newsletter`에 접속하면 관리자와 같은 입력 항목으로 이메일을 미리 볼 수 있습니다.
예시 내용 채우기, 영어·독일어 전환, 모바일·데스크톱 너비 전환을 지원합니다.
사진은 현재 브라우저에서만 임시 표시하며 저장·업로드·발송하지 않습니다. 새로고침하면 입력 내용이 초기화됩니다.
운영 빌드에는 체험 화면이 포함되지 않습니다.

실제 관리자의 실시간 미리보기는 저장하지 않은 입력을 관리자 인증 API로 렌더링합니다.
공연 일시·장소·안내 버튼 필드는 `scripts/migrate-newsletter.sql`에 포함되어 있습니다.
기존 운영 DB에 배포하기 전 백업 후 해당 마이그레이션으로 새 열을 추가하세요.
