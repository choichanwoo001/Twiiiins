# Twiiiins - Artist Platform

Vue 3 + Spring Boot 3 기반 아티스트 플랫폼

## 프로젝트 구조

```
twiiiins/
├── backend/          # Spring Boot 3 + PostgreSQL
├── frontend/         # Vue 3 + Vite
└── README.md
```

## 기술 스택

### 프론트엔드
- Vue 3 + Vite
- Vue Router
- Pinia (상태관리)
- Stripe.js (결제)
- 배포: Vercel

### 백엔드
- Spring Boot 3
- Spring Security + JWT
- PostgreSQL
- Gradle
- 배포: Railway

### 미디어
- Cloudflare Stream (동영상)
- Cloudflare Images (이미지)

### 결제
- Stripe (카드, EPS, SEPA DD)

## 주요 기능
- 홈 (최신 영상, 일정)
- 갤러리 (사진/영상)
- 스토어 (굿즈)
- 결제/주문
- 아티스트 소개
- 어드민 (상품/재고/주문 관리)

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
```
DB_URL=jdbc:postgresql://localhost:5432/twiiiins
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
CLOUDFLARE_ACCOUNT_ID=...
CLOUDFLARE_API_TOKEN=...
```

### 프론트엔드 (.env)
```
VITE_API_BASE_URL=http://localhost:8080/api
VITE_STRIPE_PUBLIC_KEY=pk_test_...
```

## 배포

### Vercel (프론트엔드)
- GitHub 연동 후 자동 배포
- Root Directory: `frontend`
- Build Command: `npm run build`
- Output Directory: `dist`

### Railway (백엔드)
- GitHub 연동 후 자동 배포
- Root Directory: `backend`
- PostgreSQL 프로비저닝 자동 연결

