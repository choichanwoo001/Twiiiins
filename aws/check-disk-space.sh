#!/bin/bash

# EC2 디스크 공간 확인 스크립트
echo "=== 디스크 공간 확인 ==="
echo ""
echo "전체 디스크 사용량:"
df -h
echo ""
echo "현재 디렉토리 사용량:"
du -sh /home/ubuntu/twiiiins/* 2>/dev/null | sort -hr
echo ""
echo "Docker 디스크 사용량:"
docker system df
echo ""
echo "가장 큰 파일 Top 20:"
find /home/ubuntu/twiiiins -type f -exec du -h {} + 2>/dev/null | sort -rh | head -20

