#!/bin/bash

# EC2 디스크 공간 정리 스크립트
echo "=== 디스크 공간 정리 시작 ==="

# 현재 디스크 사용량 확인
echo "=== 현재 디스크 사용량 ==="
df -h

# 1. Docker 빌드 캐시 정리 (가장 많은 공간을 차지)
echo ""
echo "=== Docker 빌드 캐시 정리 중... ==="
docker builder prune -af

# 2. Docker 정리 (사용하지 않는 이미지, 컨테이너, 볼륨, 네트워크)
echo ""
echo "=== Docker 정리 중... ==="
docker system prune -af --volumes

# 3. 오래된 Docker 이미지 정리 (30일 이상 사용하지 않은 것)
echo ""
echo "=== 오래된 Docker 이미지 정리 중... ==="
docker image prune -af --filter "until=720h"

# 4. 중단된 빌드 프로세스 정리
echo ""
echo "=== 중단된 빌드 프로세스 정리 중... ==="
docker buildx prune -af 2>/dev/null || true

# 3. 로그 파일 정리 (30일 이상된 로그)
echo ""
echo "=== 오래된 로그 파일 정리 중... ==="
find /home/ubuntu/twiiiins -name "*.log" -type f -mtime +30 -delete
find /var/log -name "*.log" -type f -mtime +30 -exec truncate -s 0 {} \;

# 4. 백엔드 빌드 캐시 정리
echo ""
echo "=== 백엔드 빌드 캐시 정리 중... ==="
if [ -d "/home/ubuntu/twiiiins/backend/build" ]; then
    rm -rf /home/ubuntu/twiiiins/backend/build/*
fi
if [ -d "/home/ubuntu/twiiiins/backend/.gradle" ]; then
    rm -rf /home/ubuntu/twiiiins/backend/.gradle/caches/*
fi

# 5. 프론트엔드 빌드 캐시 정리
echo ""
echo "=== 프론트엔드 빌드 캐시 정리 중... ==="
if [ -d "/home/ubuntu/twiiiins/frontend/node_modules" ]; then
    # node_modules는 유지하되, .cache만 정리
    rm -rf /home/ubuntu/twiiiins/frontend/.vite
    rm -rf /home/ubuntu/twiiiins/frontend/.cache
    rm -rf /home/ubuntu/twiiiins/frontend/dist
fi

# 6. 임시 파일 정리
echo ""
echo "=== 임시 파일 정리 중... ==="
find /tmp -type f -mtime +7 -delete
find /var/tmp -type f -mtime +7 -delete

# 7. APT 캐시 정리 (Ubuntu/Debian의 경우)
if command -v apt-get &> /dev/null; then
    echo ""
    echo "=== APT 캐시 정리 중... ==="
    apt-get clean
    apt-get autoclean
    apt-get autoremove -y
fi

# 8. YUM 캐시 정리 (Amazon Linux/CentOS의 경우)
if command -v yum &> /dev/null; then
    echo ""
    echo "=== YUM 캐시 정리 중... ==="
    yum clean all
fi

# 9. 최종 디스크 사용량 확인
echo ""
echo "=== 정리 후 디스크 사용량 ==="
df -h

# 10. 가장 큰 디렉토리 확인
echo ""
echo "=== 가장 큰 디렉토리 Top 10 ==="
du -h --max-depth=1 /home/ubuntu 2>/dev/null | sort -hr | head -10

echo ""
echo "=== 디스크 공간 정리 완료 ==="

