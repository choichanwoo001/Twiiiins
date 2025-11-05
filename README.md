# Twiiiins - Artist Platform

Vue 3 + Spring Boot 3 기반 아티스트 플랫폼

## 프로젝트 개요

Twiiiins는 아티스트의 다양한 콘텐츠를 관리하고 팬들과 소통할 수 있는 통합 플랫폼입니다. 공연 정보, 갤러리, 스토어, 결제 시스템을 하나의 플랫폼에서 제공합니다.

## 프로젝트 구조

```
twiiiins/
├── backend/          # Spring Boot 3
│   ├── src/main/java/com/twiiiins/
│   │   ├── controller/     # REST API 컨트롤러
│   │   ├── service/        # 비즈니스 로직
│   │   ├── dto/           # 데이터 전송 객체
│   │   ├── entity/        # JPA 엔티티
│   │   ├── repository/    # 데이터 접근 계층
│   │   ├── config/        # 설정 클래스
│   │   └── exception/     # 예외 처리
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
├── frontend/         # Vue 3 + Vite
│   ├── src/
│   │   ├── components/    # Vue 컴포넌트
│   │   ├── views/         # 페이지 컴포넌트
│   │   ├── stores/        # Pinia 상태 관리
│   │   ├── services/      # API 서비스
│   │   └── utils/         # 유틸리티 함수
│   └── public/
└── README.md
```

## 기술 스택

### 프론트엔드
- **Vue 3** + **Vite** - 모던 프론트엔드 프레임워크
- **Vue Router** - SPA 라우팅
- **Pinia** - 상태 관리
- **Axios** - HTTP 클라이언트
- **Stripe.js** - 결제 처리

### 백엔드
- **Spring Boot 3** - Java 웹 프레임워크
- **Spring Security** - 보안 프레임워크
- **Spring Data JPA** - 데이터 접근 계층
- **MySQL** - 관계형 데이터베이스
- **Gradle** - 빌드 도구
- **Swagger/OpenAPI** - API 문서화

### 미디어 서비스
- **Cloudflare Stream** - 동영상 스트리밍
- **Cloudflare Images** - 이미지 최적화 및 CDN

### 결제 시스템
- **Stripe** - 카드, EPS, SEPA DD 결제 지원

## 주요 기능

### 🏠 홈페이지
- 최신 영상 및 공연 일정 표시
- 아티스트 소개 및 하이라이트

### 📸 갤러리
- 사진 그룹별 관리
- 동영상 스트리밍
- 미디어 업로드 및 관리

### 🎵 공연 관리
- 공연 일정 등록 및 관리
- 과거/예정 공연 분류
- 자동 상태 변경 기능

### 🛍️ 스토어
- 굿즈 상품 관리
- 재고 관리
- 주문 처리

### 💳 결제 시스템
- Stripe 통합 결제
- 다중 결제 수단 지원
- 주문 내역 관리

### 🔧 어드민 패널
- 콘텐츠 관리 (사진, 영상, 공연)
- 상품 및 재고 관리
- 주문 및 결제 관리

## API 문서

### 기본 정보
- **Base URL**: `http://localhost:8080/api` (개발), `https://twiiiins.com/api` (프로덕션)
- **Content-Type**: `application/json`

### 주요 엔드포인트

#### 🎵 공연 관리 (`/api/concerts`)
- `GET /concerts` - 모든 공연 조회
- `GET /concerts/upcoming` - 예정 공연 조회
- `GET /concerts/past` - 과거 공연 조회
- `GET /concerts/{id}` - 특정 공연 조회
- `POST /concerts` - 공연 생성
- `PUT /concerts/{id}` - 공연 수정
- `DELETE /concerts/{id}` - 공연 삭제

#### 📸 사진 관리 (`/api/photos`)
- `GET /photos/groups` - 사진 그룹 목록
- `GET /photos/groups/{id}` - 특정 사진 그룹 조회
- `POST /photos/groups` - 사진 그룹 생성
- `GET /photos/groups/{groupId}/photos` - 그룹별 사진 목록
- `POST /photos/groups/{groupId}/photos` - 사진 추가

#### 🎬 동영상 관리 (`/api/videos`)
- `GET /videos` - 모든 동영상 조회
- `GET /videos/{id}` - 특정 동영상 조회
- `POST /videos` - 동영상 생성
- `PUT /videos/{id}` - 동영상 수정
- `DELETE /videos/{id}` - 동영상 삭제

#### 📰 뉴스 관리 (`/api/news`)
- `GET /news` - 뉴스 목록 조회
- `POST /news` - 뉴스 생성
- `PUT /news/{id}` - 뉴스 수정
- `DELETE /news/{id}` - 뉴스 삭제

#### 🎸 장비 관리 (`/api/equipment`)
- `GET /equipment` - 장비 목록 조회
- `POST /equipment` - 장비 추가
- `PUT /equipment/{id}` - 장비 수정
- `DELETE /equipment/{id}` - 장비 삭제

### 응답 형식

모든 API는 표준화된 응답 형식을 사용합니다:

```json
{
  "success": true,
  "data": { ... },
  "message": "성공적으로 처리되었습니다.",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

에러 응답:
```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "요청한 리소스를 찾을 수 없습니다."
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## 로컬 개발

### 백엔드
```bash
cd backend
./gradlew bootRun
```

### 프론트엔드
```bash
cd frontend
npm install
npm run dev
```

## 환경변수

### 백엔드 (.env)
```env
# 데이터베이스 설정
DB_URL=jdbc:mysql://localhost:3306/twiiiins
DB_USERNAME=root
DB_PASSWORD=your_password

# Stripe 결제 설정
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Cloudflare 미디어 설정
CLOUDFLARE_ACCOUNT_ID=...
CLOUDFLARE_API_TOKEN=...

# 파일 업로드 설정
UPLOAD_DIR=./uploads
MAX_FILE_SIZE=10485760
```

### 프론트엔드 (.env)
```env
# API 설정
VITE_API_BASE_URL=http://localhost:8080/api

# Stripe 설정
VITE_STRIPE_PUBLIC_KEY=pk_test_...

# Cloudflare 설정
VITE_CLOUDFLARE_STREAM_URL=https://customer-...
```

## 개발 가이드

### 코드 스타일
- **백엔드**: Java 17, Spring Boot 3 컨벤션
- **프론트엔드**: Vue 3 Composition API, TypeScript 권장
- **커밋 메시지**: 한국어로 작성

### API 개발 규칙
- 모든 컨트롤러에 `@ApiResponse` 어노테이션 적용
- 표준화된 응답 형식 사용
- 적절한 HTTP 상태 코드 반환
- 전역 예외 처리 활용

### 프론트엔드 개발 규칙
- Axios 인터셉터를 통한 에러 처리
- Pinia를 통한 상태 관리
- 컴포넌트 재사용성 고려

## 프로덕션 배포

### 도메인 및 HTTPS 설정

프로젝트는 `twiiiins.com` 도메인과 HTTPS를 지원하도록 구성되어 있습니다.

#### 1. SSL 인증서 준비

Let's Encrypt 인증서를 사용하는 경우:
```bash
# Certbot으로 인증서 발급
certbot certonly --standalone -d twiiiins.com -d www.twiiiins.com

# 인증서를 nginx 컨테이너에서 접근 가능한 위치에 복사
cp /etc/letsencrypt/live/twiiiins.com/fullchain.pem aws/nginx/ssl/cert.pem
cp /etc/letsencrypt/live/twiiiins.com/privkey.pem aws/nginx/ssl/key.pem
```

또는 자체 서명 인증서로 테스트:
```bash
mkdir -p aws/nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout aws/nginx/ssl/key.pem \
  -out aws/nginx/ssl/cert.pem
```

#### 2. 환경 변수 설정

프로젝트 루트에 `.env` 파일 생성:

```env
# ============================================
# 필수 환경 변수
# ============================================

# 데이터베이스 (필수)
DB_URL=jdbc:mysql://your-db-host:3306/twiiiins
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# CORS 허용 도메인 (선택사항 - 기본값 있음)
# 설정하지 않으면: http://localhost:5173,https://twiiiins.com,https://www.twiiiins.com 사용
CORS_ORIGINS=https://twiiiins.com,https://www.twiiiins.com

# ============================================
# AWS S3 설정 (파일 업로드 필수)
# ============================================
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_REGION=eu-central-1
S3_BUCKET_NAME=twiiiins-uploads

# ============================================
# 선택적 환경 변수 (기본값 있음)
# ============================================

# 파일 업로드 (기본값: 104857600 = 100MB)
UPLOAD_MAX_SIZE=104857600
FILE_UPLOAD_DIR=/app/uploads

# 서버 포트 (기본값: 8080)
PORT=8080

# JPA 설정 (기본값: update)
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false

# 로깅 레벨 (기본값: INFO)
LOG_LEVEL=INFO

# ============================================
# 선택적 서비스 설정 (향후 구현 시)
# ============================================

# Stripe 결제 (결제 기능 사용 시)
# STRIPE_SECRET_KEY=sk_live_...
# STRIPE_WEBHOOK_SECRET=whsec_...

# Cloudflare 미디어 (동영상 스트리밍 사용 시)
# CLOUDFLARE_ACCOUNT_ID=...
# CLOUDFLARE_API_TOKEN=...
```

#### 3. 프론트엔드 빌드

```bash
cd frontend
npm install
npm run build
# 빌드된 파일은 frontend/dist/ 디렉토리에 생성됩니다
```

#### 4. Docker Compose로 배포

```bash
# 컨테이너 빌드 및 실행
docker-compose up -d --build

# 로그 확인
docker-compose logs -f
```

#### 5. Nginx 설정 확인

- HTTP(80) → HTTPS(443) 리다이렉트: 자동 처리
- SSL 인증서 경로: `/etc/nginx/ssl/cert.pem`, `/etc/nginx/ssl/key.pem`
- 도메인: `twiiiins.com`, `www.twiiiins.com`

#### 6. 백엔드 CORS 설정

`SecurityConfig.java`에서 다음 도메인들이 기본 허용됩니다:
- `https://twiiiins.com`
- `https://www.twiiiins.com`
- `http://localhost:5173` (개발 환경)

환경 변수 `CORS_ORIGINS`로 추가 도메인 설정 가능합니다.

### 배포 체크리스트

- [ ] SSL 인증서 설정 완료
- [ ] 도메인 DNS A 레코드 설정 (서버 IP로)
- [ ] 환경 변수 설정 완료
- [ ] 프론트엔드 빌드 완료
- [ ] Docker 컨테이너 실행 확인
- [ ] HTTPS 접근 테스트
- [ ] API 요청 테스트
- [ ] CORS 오류 확인

## 라이선스

이 프로젝트는 MIT 라이선스 하에 있습니다.

## 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
햣 
ssl 인증서도 등록함 (ubuntu에)