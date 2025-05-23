#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
docker stop springboot-server || true
docker rm springboot-server || true
docker pull 293337163237.dkr.ecr.ap-northeast-2.amazonaws.com/babycareai/springboot-server:latest
docker run -d --name springboot-server -p 8080:8080 293337163237.dkr.ecr.ap-northeast-2.amazonaws.com/babycareai/springboot-server:latest
echo "--------------- 서버 배포 완료 -----------------"