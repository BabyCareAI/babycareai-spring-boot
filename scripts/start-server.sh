#!/bin/bash

echo "--------------- release/57-v0.4.1 배포 시작 (2) -----------------"

# 기존 컨테이너 중지 및 제거
docker-compose down

# 최신 이미지 가져오기
docker pull 293337163237.dkr.ecr.ap-northeast-2.amazonaws.com/babycareai/springboot-server:latest

# 백그라운드로 전체 서비스 시작
docker-compose up -d

# 필요 없는 springboot-server 이미지 정리
docker images | grep "springboot-server" | grep -v "latest" | awk '{print $3}' | xargs -r docker rmi -f

echo "--------------- docker-compose로 서버 배포 완료 -----------------"
