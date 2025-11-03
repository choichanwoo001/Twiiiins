# SSL 인증서 설정 가이드

## 📋 SSL 인증서가 필요한가요?

### ✅ **프로덕션 환경 (도메인 사용 시)**
- **필수**: 보안, SEO, 브라우저 호환성
- 무료 인증서 사용 가능 (Let's Encrypt)

### ❌ **개발/테스트 환경**
- **선택사항**: HTTP만 사용해도 됩니다
- 로컬 개발 시 필요 없음

---

## 🔒 방법 1: Let's Encrypt 무료 인증서 (프로덕션 권장)

### 전제 조건
- 도메인(`twiiiins.com`)이 서버 IP로 DNS A 레코드 설정 완료
- 80, 443 포트 열려있어야 함

### 설치 및 설정

#### 1단계: Certbot 설치
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install certbot

# CentOS/RHEL
sudo yum install certbot
```

#### 2단계: 인증서 발급
```bash
# nginx가 실행 중이면 중지
docker-compose stop nginx

# Certbot으로 인증서 발급 (standalone 모드)
sudo certbot certonly --standalone \
  -d twiiiins.com \
  -d www.twiiiins.com \
  --email your-email@example.com \
  --agree-tos \
  --non-interactive

# 또는 대화형 모드
sudo certbot certonly --standalone -d twiiiins.com -d www.twiiiins.com
```

#### 3단계: 인증서를 프로젝트로 복사
```bash
# ssl 디렉토리 생성
mkdir -p aws/nginx/ssl

# 인증서 복사
sudo cp /etc/letsencrypt/live/twiiiins.com/fullchain.pem aws/nginx/ssl/cert.pem
sudo cp /etc/letsencrypt/live/twiiiins.com/privkey.pem aws/nginx/ssl/key.pem

# 권한 설정 (nginx 컨테이너에서 읽을 수 있도록)
sudo chmod 644 aws/nginx/ssl/cert.pem
sudo chmod 600 aws/nginx/ssl/key.pem
sudo chown $USER:$USER aws/nginx/ssl/*.pem
```

#### 4단계: 자동 갱신 설정 (90일마다 갱신 필요)
```bash
# crontab 편집
sudo crontab -e

# 다음 줄 추가 (매일 새벽 3시에 갱신 시도)
0 3 * * * certbot renew --quiet && docker-compose restart nginx
```

#### 5단계: nginx 시작
```bash
docker-compose up -d nginx
```

---

## 🧪 방법 2: 자체 서명 인증서 (테스트용)

**주의**: 브라우저에서 경고가 뜨지만 테스트는 가능합니다.

```bash
# ssl 디렉토리 생성
mkdir -p aws/nginx/ssl

# 자체 서명 인증서 생성
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout aws/nginx/ssl/key.pem \
  -out aws/nginx/ssl/cert.pem \
  -subj "/C=KR/ST=Seoul/L=Seoul/O=Twiiiins/CN=twiiiins.com"

# 권한 설정
chmod 644 aws/nginx/ssl/cert.pem
chmod 600 aws/nginx/ssl/key.pem
```

---

## 🚫 방법 3: HTTP만 사용 (개발/테스트)

프로덕션이 아니거나 인증서 없이 테스트하려면:

### nginx.conf 수정
HTTPS 서버 블록을 주석 처리하고 HTTP만 사용:

```nginx
# HTTPS 서버 블록 전체를 주석 처리
# server {
#     listen 443 ssl http2;
#     ...
# }

# HTTP 서버 수정 (리다이렉트 제거)
server {
    listen 80;
    server_name twiiiins.com www.twiiiins.com;
    
    # HTTPS 리다이렉트 제거
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # API 프록시
    location /api/ {
        proxy_pass http://backend;
        # ... 기타 설정
    }
}
```

---

## ✅ 인증서 설정 확인

### 1. nginx 로그 확인
```bash
docker-compose logs nginx
```

### 2. SSL 연결 테스트
```bash
# OpenSSL로 테스트
openssl s_client -connect twiiiins.com:443 -servername twiiiins.com

# 또는 브라우저에서
https://twiiiins.com
```

### 3. SSL Labs 테스트 (프로덕션)
https://www.ssllabs.com/ssltest/analyze.html?d=twiiiins.com

---

## 🔄 인증서 갱신 (Let's Encrypt)

Let's Encrypt 인증서는 90일마다 갱신 필요:

```bash
# 수동 갱신
sudo certbot renew

# 갱신 후 nginx 재시작
docker-compose restart nginx

# 또는 자동 갱신 스크립트
#!/bin/bash
certbot renew --quiet
if [ $? -eq 0 ]; then
    docker-compose restart nginx
fi
```

---

## ❓ 문제 해결

### "SSL certificate not found" 오류
- `aws/nginx/ssl/` 디렉토리에 파일이 있는지 확인
- 파일 권한 확인 (`chmod 644 cert.pem`, `chmod 600 key.pem`)

### "Port 80 already in use"
- 다른 웹 서버가 실행 중인지 확인
- `sudo lsof -i :80` 또는 `sudo netstat -tulpn | grep :80`

### "Connection refused" (Let's Encrypt)
- 방화벽에서 80, 443 포트 열려있는지 확인
- DNS 레코드가 올바른지 확인 (`dig twiiiins.com`)

---

## 📝 요약

| 상황 | 인증서 필요? | 방법 |
|------|------------|------|
| 프로덕션 (도메인 사용) | ✅ 필수 | Let's Encrypt 권장 |
| 개발/테스트 (로컬) | ❌ 불필요 | HTTP만 사용 |
| 내부 테스트 | ❌ 선택 | 자체 서명 또는 HTTP |

