# 배포 가이드

## 프론트엔드 배포 (Vercel)

### 1. Vercel 계정 연결
1. https://vercel.com 에서 GitHub 계정으로 로그인
2. "New Project" 클릭
3. 이 리포지토리 선택

### 2. 프로젝트 설정
- **Framework Preset**: Vite
- **Root Directory**: `frontend`
- **Build Command**: `npm run build`
- **Output Directory**: `dist`

### 3. 환경변수 설정
Vercel 대시보드에서 다음 환경변수 추가:
```
VITE_API_BASE_URL=https://your-backend-url.railway.app/api
VITE_STRIPE_PUBLIC_KEY=pk_live_your_key
```

### 4. 배포
- `main` 브랜치에 푸시하면 자동 배포

---

## 백엔드 배포 (Railway)

### 1. Railway 계정 연결
1. https://railway.app 에서 GitHub 계정으로 로그인
2. "New Project" 클릭
3. "Deploy from GitHub repo" 선택
4. 이 리포지토리 선택

### 2. PostgreSQL 추가
1. 프로젝트에서 "+ New" 클릭
2. "Database" → "PostgreSQL" 선택
3. 자동으로 환경변수가 설정됨

### 3. 백엔드 서비스 설정
1. 프로젝트에서 백엔드 서비스 클릭
2. Settings → General:
   - **Root Directory**: `backend`
   - **Build Command**: `./gradlew build -x test`
   - **Start Command**: `java -jar build/libs/*.jar`

### 4. 환경변수 설정
Railway 대시보드에서 다음 환경변수 추가:
```
DB_URL=${{Postgres.DATABASE_URL}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
JWT_SECRET=your_jwt_secret_key_min_256_bits
STRIPE_SECRET_KEY=sk_live_your_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret
CLOUDFLARE_ACCOUNT_ID=your_account_id
CLOUDFLARE_API_TOKEN=your_api_token
CORS_ORIGINS=https://your-frontend-url.vercel.app
PORT=8080
```

### 5. Stripe Webhook 설정
1. Stripe 대시보드 → Developers → Webhooks
2. "Add endpoint" 클릭
3. Endpoint URL: `https://your-backend-url.railway.app/api/stripe/webhook`
4. Events to send:
   - `payment_intent.succeeded`
   - `payment_intent.payment_failed`
   - `checkout.session.completed`
5. Webhook Secret을 복사하여 환경변수에 추가

### 6. 배포
- `main` 브랜치에 푸시하면 자동 배포

---

## Cloudflare 설정

### Stream (동영상)
1. Cloudflare 대시보드 → Stream
2. API Token 생성
3. 백엔드 환경변수에 추가

### Images (이미지)
1. Cloudflare 대시보드 → Images
2. Upload API 사용

---

## Stripe 결제 메소드 설정

### 오스트리아 타겟팅
1. Stripe 대시보드 → Settings → Payment methods
2. 다음 활성화:
   - **Card payments** (카드)
   - **EPS** (오스트리아 은행 송금)
   - **SEPA Direct Debit** (유럽 계좌 이체)
   - **Apple Pay**
   - **Google Pay**

---

## 도메인 연결

### Vercel (프론트엔드)
1. Project Settings → Domains
2. 커스텀 도메인 추가
3. DNS 설정 (Cloudflare에서)

### Railway (백엔드)
1. Service → Settings → Networking
2. Generate Domain 또는 커스텀 도메인 설정

### Cloudflare DNS
```
Type: A
Name: api
Value: Railway IP

Type: CNAME
Name: @
Value: your-app.vercel.app
```

---

## 주의사항

1. **JWT Secret**: 최소 256비트 이상의 안전한 키 사용
2. **CORS**: 프론트엔드 도메인을 정확히 설정
3. **PostgreSQL**: Railway의 자동 생성 DB 사용 권장
4. **Stripe**: 테스트 모드로 먼저 확인 후 라이브 모드 전환
5. **환경변수**: `.env` 파일은 절대 커밋하지 않기

