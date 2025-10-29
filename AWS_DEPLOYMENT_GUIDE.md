# 🚀 AWS 배포 가이드

## 📋 개요

이 가이드는 Twiiiins 프로젝트를 AWS에 배포하는 방법을 설명합니다.

### 🏗️ 아키텍처
```
Internet → Route 53 → CloudFront → EC2 (Nginx + Spring Boot + MySQL)
                                    ↓
                                 S3 (파일 저장)
```

---

## 🛠️ 1단계: AWS 리소스 생성

### 1.1 EC2 인스턴스 생성
```
1. AWS Console → EC2 → Launch Instance
2. AMI: Amazon Linux 2023
3. Instance Type: t3.micro (Free tier)
4. Key Pair: 새로 생성 또는 기존 사용
5. Security Group: 
   - SSH (22): 0.0.0.0/0
   - HTTP (80): 0.0.0.0/0
   - HTTPS (443): 0.0.0.0/0
   - Custom TCP (8080): 0.0.0.0/0
6. User Data: aws/ec2-user-data.sh 내용 복사
```

### 1.2 RDS MySQL 생성 (선택사항)
```
1. AWS Console → RDS → Create Database
2. Engine: MySQL 8.0
3. Template: Free tier
4. DB Instance: db.t3.micro
5. Master Username: twiiiins_admin
6. Master Password: [안전한 비밀번호]
7. VPC: Default
8. Security Group: EC2와 동일한 그룹 사용
```

### 1.3 S3 버킷 생성
```
1. AWS Console → S3 → Create Bucket
2. Bucket Name: twiiiins-uploads-[고유번호]
3. Region: ap-northeast-2 (서울)
4. Public Access: Block all (권장)
5. Versioning: Enable (선택사항)
```

---

## 🔧 2단계: EC2 설정

### 2.1 EC2 접속
```bash
# SSH로 EC2 접속
ssh -i your-key.pem ec2-user@your-ec2-ip
```

### 2.2 애플리케이션 배포
```bash
# 애플리케이션 디렉토리로 이동
cd /opt/twiiiins

# GitHub 저장소 클론
git clone https://github.com/[username]/twiiiins.git .

# 환경 변수 설정
cp env.example .env
nano .env  # 환경 변수 수정

# 서비스 시작
./start-services.sh
```

### 2.3 환경 변수 설정 (.env)
```bash
# 데이터베이스 설정
DB_URL=jdbc:mysql://localhost:3306/twiiiins?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=twiiiins_user
DB_PASSWORD=your_secure_password_here

# JWT 설정
JWT_SECRET=your_jwt_secret_key_here_32_chars_minimum

# AWS S3 설정
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_REGION=ap-northeast-2
S3_BUCKET_NAME=twiiiins-uploads-123456

# CORS 설정
CORS_ORIGINS=https://your-domain.com,https://www.your-domain.com
```

---

## 🌐 3단계: 도메인 및 SSL 설정

### 3.1 Route 53 도메인 설정
```
1. AWS Console → Route 53 → Hosted Zones
2. Create Hosted Zone
3. Domain Name: your-domain.com
4. Type: Public hosted zone
5. A Record 생성:
   - Name: @
   - Type: A
   - Value: EC2 Public IP
6. CNAME Record 생성:
   - Name: www
   - Type: CNAME
   - Value: your-domain.com
```

### 3.2 SSL 인증서 설정 (Let's Encrypt)
```bash
# EC2에서 실행
sudo yum install -y certbot

# 인증서 발급
sudo certbot certonly --standalone -d your-domain.com -d www.your-domain.com

# 인증서를 Nginx 디렉토리로 복사
sudo mkdir -p /opt/twiiiins/nginx/ssl
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem /opt/twiiiins/nginx/ssl/
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem /opt/twiiiins/nginx/ssl/

# 자동 갱신 설정
echo "0 12 * * * /usr/bin/certbot renew --quiet" | sudo crontab -
```

---

## 🔄 4단계: CI/CD 설정

### 4.1 GitHub Actions 업데이트
```yaml
# .github/workflows/deploy.yml에 추가
- name: Deploy to AWS
  if: github.ref == 'refs/heads/main'
  run: |
    # EC2에 배포 스크립트 실행
    ssh -i ${{ secrets.EC2_KEY }} ec2-user@${{ secrets.EC2_HOST }} '
      cd /opt/twiiiins &&
      git pull origin main &&
      docker-compose down &&
      docker-compose up -d --build
    '
```

### 4.2 GitHub Secrets 설정
```
EC2_KEY: EC2 키 파일 내용
EC2_HOST: EC2 퍼블릭 IP
AWS_ACCESS_KEY_ID: AWS 액세스 키
AWS_SECRET_ACCESS_KEY: AWS 시크릿 키
```

---

## 📊 5단계: 모니터링 설정

### 5.1 CloudWatch 설정
```
1. AWS Console → CloudWatch
2. Log Groups 생성:
   - /aws/ec2/twiiiins/application
   - /aws/ec2/twiiiins/nginx
3. EC2에서 CloudWatch Agent 설치
4. 로그 전송 설정
```

### 5.2 알람 설정
```
1. CloudWatch → Alarms
2. EC2 CPU 사용률 > 80%
3. 메모리 사용률 > 80%
4. 디스크 사용률 > 80%
5. SNS로 이메일 알림 설정
```

---

## 🚀 6단계: 배포 확인

### 6.1 서비스 상태 확인
```bash
# Docker 컨테이너 상태
docker-compose ps

# 로그 확인
docker-compose logs -f backend
docker-compose logs -f mysql

# 헬스체크
curl http://localhost:8080/actuator/health
```

### 6.2 API 테스트
```bash
# API 엔드포인트 테스트
curl https://your-domain.com/api/health
curl https://your-domain.com/api/concerts
```

---

## 🔧 7단계: 유지보수

### 7.1 백업 설정
```bash
# MySQL 백업 스크립트
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker exec twiiiins-mysql mysqldump -u root -p$DB_PASSWORD twiiiins > backup_$DATE.sql
aws s3 cp backup_$DATE.sql s3://your-backup-bucket/
```

### 7.2 업데이트 배포
```bash
# 수동 배포
cd /opt/twiiiins
git pull origin main
docker-compose down
docker-compose up -d --build

# 자동 배포 (GitHub Actions 사용)
git push origin main
```

---

## 💰 비용 최적화

### Free Tier 활용
- EC2 t3.micro: 750시간/월 무료
- RDS db.t3.micro: 750시간/월 무료
- S3: 5GB 무료
- Route 53: 호스팅 존 1개 무료

### 예상 월 비용 (Free Tier 초과 시)
- EC2 t3.micro: ~$8.50
- RDS db.t3.micro: ~$12.50
- S3 (10GB): ~$0.25
- Route 53: ~$0.50
- **총합: ~$22/월**

---

## 🆘 문제 해결

### 일반적인 문제들
1. **포트 접근 불가**: 보안 그룹 확인
2. **SSL 인증서 오류**: Let's Encrypt 갱신
3. **데이터베이스 연결 실패**: RDS 보안 그룹 확인
4. **S3 업로드 실패**: IAM 권한 확인

### 로그 확인 명령어
```bash
# 애플리케이션 로그
docker-compose logs -f backend

# Nginx 로그
docker-compose logs -f nginx

# 시스템 로그
sudo journalctl -u docker
```

---

## 📞 지원

문제가 발생하면 다음을 확인하세요:
1. AWS CloudWatch 로그
2. EC2 시스템 로그
3. Docker 컨테이너 로그
4. 보안 그룹 설정
5. 환경 변수 설정
