# 모찌케어-SpringBoot

README.md 작성중

## AI 기반 GitHub 안내서
deepwiki 문서: https://deepwiki.com/BabyCareAI/babycareai-spring-boot

## 프로젝트 구조
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
