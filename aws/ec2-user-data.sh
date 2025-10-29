#!/bin/bash

# AWS EC2 User Data 스크립트
# Spring Boot + MySQL + Docker 배포용

echo "=== AWS EC2 초기 설정 시작 ==="

# 시스템 업데이트
yum update -y

# Docker 설치
yum install -y docker
systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user

# Docker Compose 설치
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# Git 설치
yum install -y git

# Java 17 설치 (Docker 없이 직접 실행할 경우)
yum install -y java-17-amazon-corretto-devel

# 애플리케이션 디렉토리 생성
mkdir -p /opt/twiiiins
cd /opt/twiiiins

# GitHub 저장소 클론 (토큰 필요시 환경변수 사용)
# git clone https://github.com/[username]/twiiiins.git .

# 환경 변수 파일 생성
cat > .env << EOF
# 데이터베이스 설정
DB_URL=jdbc:mysql://localhost:3306/twiiiins?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=twiiiins_user
DB_PASSWORD=your_secure_password_here

# JWT 설정
JWT_SECRET=your_jwt_secret_key_here_32_chars_minimum

# Stripe 설정 (선택사항)
STRIPE_SECRET_KEY=your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=your_stripe_webhook_secret

# CORS 설정
CORS_ORIGINS=http://localhost:3000,https://your-domain.com

# 서버 설정
PORT=8080
SERVER_CONTEXT_PATH=
EOF

# Docker Compose 파일 생성
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  # MySQL 데이터베이스
  mysql:
    image: mysql:8.0
    container_name: twiiiins-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: twiiiins
      MYSQL_USER: ${DB_USERNAME}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./mysql-init:/docker-entrypoint-initdb.d
    command: --default-authentication-plugin=mysql_native_password

  # Spring Boot 백엔드
  backend:
    build: 
      context: ./backend
      dockerfile: Dockerfile
    container_name: twiiiins-backend
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:mysql://mysql:3306/twiiiins?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - STRIPE_SECRET_KEY=${STRIPE_SECRET_KEY}
      - STRIPE_WEBHOOK_SECRET=${STRIPE_WEBHOOK_SECRET}
      - CORS_ORIGINS=${CORS_ORIGINS}
      - PORT=8080
    depends_on:
      - mysql
    volumes:
      - ./uploads:/app/uploads

volumes:
  mysql_data:
EOF

# MySQL 초기화 스크립트 디렉토리 생성
mkdir -p mysql-init

# 보안 그룹 설정 안내
echo "=== 보안 그룹 설정 필요 ==="
echo "1. EC2 보안 그룹에서 다음 포트 열기:"
echo "   - SSH (22): 0.0.0.0/0"
echo "   - HTTP (80): 0.0.0.0/0"
echo "   - HTTPS (443): 0.0.0.0/0"
echo "   - Custom TCP (8080): 0.0.0.0/0"
echo "   - MySQL (3306): EC2 보안 그룹 내부만"

# 서비스 시작 스크립트 생성
cat > start-services.sh << 'EOF'
#!/bin/bash
cd /opt/twiiiins

# 환경 변수 로드
source .env

# Docker Compose로 서비스 시작
docker-compose up -d

# 서비스 상태 확인
echo "=== 서비스 상태 확인 ==="
docker-compose ps

echo "=== 로그 확인 ==="
echo "백엔드 로그: docker-compose logs -f backend"
echo "MySQL 로그: docker-compose logs -f mysql"
EOF

chmod +x start-services.sh

# 자동 시작 설정 (선택사항)
cat > /etc/systemd/system/twiiiins.service << 'EOF'
[Unit]
Description=Twiiiins Application
After=docker.service
Requires=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/twiiiins
ExecStart=/opt/twiiiins/start-services.sh
ExecStop=/usr/local/bin/docker-compose down

[Install]
WantedBy=multi-user.target
EOF

echo "=== AWS EC2 초기 설정 완료 ==="
echo "다음 단계:"
echo "1. GitHub 저장소 클론: cd /opt/twiiiins && git clone [repository] ."
echo "2. .env 파일에서 환경 변수 수정"
echo "3. 서비스 시작: ./start-services.sh"
echo "4. 자동 시작 활성화: systemctl enable twiiiins"
