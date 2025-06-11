# 모찌케어-SpringBoot

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)
![AWS](https://img.shields.io/badge/AWS-S3%20%7C%20SageMaker-yellow)
![Redis](https://img.shields.io/badge/Redis-Cache-red)

> 본 README.md는 모찌케어 Spring Boot 서버에 대한 주요 정보만을 담고 있습니다.  
> [Blog](https://baxdailygit.github.io/) 👈 AI 기반 영유아 피부 질환 진단 서비스 개발 과정을 블로그에 작성하였습니다.  
> [DeepWiki](https://deepwiki.com/BabyCareAI/babycareai-spring-boot) 👈딥위키를 통해 해당 레포지토리의 궁금한 점을 물어보세요.  

## 📋 프로젝트 개요

**모찌케어 AI**는 아기 피부 병변 사진과 증상 정보를 종합적으로 분석하여 질환명 예측, 중증도 분석, 병원 내원 필요 여부 판단, 가정 내 처치 방법 안내를 제공하는 AI 기반 서비스입니다.

### 주요 기능
- 🖼️ **이미지 업로드**: AWS S3를 통한 의료 이미지 저장
- 🤖 **AI 진단**: AWS SageMaker 기반 피부 질환 분류
- 📝 **증상 입력**: Redis 기반 임시 증상 데이터 저장

## 🏗️ 시스템 아키텍처
<img width="500" alt="image" src="https://github.com/user-attachments/assets/7cff679d-a45b-4b59-8329-f1cb55f7f89d" />

## 🚀 빠른 시작

### 필수 요구사항
- Java 17+
- Gradle 7.0+
- AWS 계정 (S3, SageMaker)
- Redis 서버

### 설치 및 실행

1. **저장소 클론**
```bash
git clone https://github.com/BabyCareAI/babycareai-spring-boot.git
cd babycareai-spring-boot
```

2. **application.properties 환경 설정**

3. **빌드 및 실행**
```bash
./gradlew clean build
./gradlew bootRun
```

4. **API 문서 확인**
```
http://localhost:8080/swagger-ui/index.html
```

## 📡 API 엔드포인트

### 진단 워크플로우

| 순서 | 엔드포인트 | 메서드 | 설명 |
|------|------------|--------|------|
| 1 | `/api/v1/diagnosis/image-upload` | POST | 이미지 업로드 및 진단 ID 생성 |
| 2 | `/api/v1/diagnosis/classify` | POST | AI 기반 피부 질환 분류 |
| 3 | `/api/v1/diagnosis/symptom` | POST | 증상 정보 저장 |

### 예시 요청

**1. 이미지 업로드**
```bash
curl -X POST "http://localhost:8080/api/v1/diagnosis/image-upload" \
  -H "Content-Type: multipart/form-data" \
  -F "image=@skin_image.jpg" \
  -F "bodyPart=FACE"
```

**2.  영상 기반 피부 질환 분류**
```bash
curl -X POST "http://localhost:8080/api/v1/diagnosis/classify" \
  -H "Content-Type: application/json" \
  -d '{"diagnosisId": "uuid-from-upload"}'
```

**3. 증상 제출**
```bash
curl -X POST "http://localhost:8080/api/v1/diagnosis/symptom" \
  -H "Content-Type: application/json" \
  -d '{
    "diagnosisId": "uuid-from-upload",
    "symptoms": ["FEVER", "ACHES_AND_PAINS"]
  }'
```

## 🔧 기술 스택

- **Framework**: Spring Boot 3.3.4
- **Language**: Java 17
- **Build Tool**: Gradle
- **Documentation**: Swagger/OpenAPI 3.0

### AWS 서비스
- **S3**: 이미지 저장 및 메타데이터 관리
- **SageMaker**: 서버리스 추론을 통한 ML 모델 배포
- **ECR**: Docker 이미지 저장소
- **CodeDeploy**: 자동 배포

### 캐싱
- **Redis**: 임시 데이터 캐싱 (30분 TTL))

### 모니터링
- **Prometheus**: 메트릭 수집
- **Grafana**: 대시보드 시각화
- **Spring Actuator**: 애플리케이션 헬스 체크
<img width="400" alt="image" src="https://github.com/user-attachments/assets/9e432bfa-83f6-4d78-b45e-c668bde0b50d" />

## 🧪 테스트 커버리지
<img width="400" alt="image" src="https://github.com/user-attachments/assets/87cc23d1-fdec-4411-b149-7813acc73681" />

## 🚀 배포 자동화 파이프라인
<img width="400" alt="image" src="https://github.com/user-attachments/assets/583daa95-26cf-439a-b0d5-f81d89fd53a9" />

### 배포 명령
```bash
# release 브랜치에 푸시하면 자동 배포
git push origin release/v0.4.1
```

## 📁 프로젝트 구조

```
📦 
├─ .github
│  └─ workflows
│     └─ deploy.yml
├─ .gitignore
├─ Dockerfile
├─ LICENSE
├─ README.md
├─ appspec.yml
├─ build.gradle
├─ gradle
│  └─ wrapper
│     ├─ gradle-wrapper.jar
│     └─ gradle-wrapper.properties
├─ gradlew
├─ gradlew.bat
├─ scripts
│  └─ start-server.sh
├─ settings.gradle
└─ src
   ├─ main
   │  └─ java
   │     └─ babycareai
   │        └─ backend
   │           ├─ BabyCareAiApplication.java
   │           ├─ common
   │           │  └─ dto
   │           │     └─ ErrorResponse.java
   │           ├─ config
   │           │  ├─ RedisConfig.java
   │           │  ├─ RestTemplateConfig.java
   │           │  ├─ S3Config.java
   │           │  ├─ SageMakerConfig.java
   │           │  ├─ SwaggerConfig.java
   │           │  └─ WebConfig.java
   │           ├─ controller
   │           │  ├─ ImageClassificationController.java
   │           │  ├─ ImageUploadController.java
   │           │  └─ SymptomController.java
   │           ├─ domain
   │           │  └─ diagnosis
   │           │     ├─ dto
   │           │     │  ├─ ImageClassificationResponse.java
   │           │     │  ├─ ImageUploadResponse.java
   │           │     │  └─ SymptomRequest.java
   │           │     ├─ enums
   │           │     │  ├─ BodyPart.java
   │           │     │  └─ Symptoms.java
   │           │     └─ service
   │           │        ├─ ImageClassificationService.java
   │           │        ├─ ImageUploadService.java
   │           │        └─ SymptomService.java
   │           └─ exception
   │              ├─ GlobalExceptionHandler.java
   │              ├─ ImageClassificationException.java
   │              ├─ ImageUploadException.java
   │              ├─ S3UploadException.java
   │              └─ SymptomException.java
   └─ test
      └─ java
         └─ babycareai
            └─ backend
               ├─ BabyCareAiApplicationTests.java
               ├─ controller
               │  ├─ ImageClassificationControllerTest.java
               │  ├─ ImageUploadControllerTest.java
               │  └─ SymptomControllerTest.java
               └─ domain
                  └─ diagnosis
                     └─ service
                        ├─ ImageClassificationServiceTest.java
                        ├─ ImageUploadServiceTest.java
                        └─ SymptomServiceTest.java
```

## 📄 라이센스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.


