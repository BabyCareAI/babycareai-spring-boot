package babycareai.backend.domain.diagnosis.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageClassificationService {

    private final AmazonS3Client s3Client;
    private final SageMakerRuntimeClient sageMakerRuntimeClient;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${sagemaker.endpoint.name}")
    private String sagemakerEndpointName;

    public void classifySkinDisease(String diagnosisId) throws IOException {
        // 이미지 다운로드
        S3Object imageObject = s3Client.getObject(bucket, diagnosisId);

        // 업로드된 이미지에 대한 예측 요청
        byte[] imageBytes = imageObject.getObjectContent().readAllBytes(); // 이미지 바이트로 변환
        String classificationResult = invokeSageMakerEndpoint(imageBytes); // SageMaker 엔드포인트 호출

        // diagnosisId, imageUrl, classificationResult Redis에 저장
        saveClassificationToRedis(diagnosisId, classificationResult);
        log.info("예측 결과 저장 완료. diagnosisId: {}, classificationResult: {}", diagnosisId, classificationResult);
    }

    private String invokeSageMakerEndpoint(byte[] imageBytes) throws IOException {
        // SageMaker 엔드포인트 호출
        InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                .endpointName(sagemakerEndpointName)
                .contentType("application/x-image")
                .body(SdkBytes.fromByteArray(imageBytes))
                .build();

        InvokeEndpointResponse response = sageMakerRuntimeClient.invokeEndpoint(request);

        // SageMaker 모델의 예측 결과 반환
        return response.body().asUtf8String();
    }

    private void saveClassificationToRedis(String diagnosisId, String classificationResult) {
        String redisKey = "classification:" + diagnosisId;

        String value = String.format("{\"classificationResult\":%s}", classificationResult);

        // Redis에 예측 데이터 저장
        redisTemplate.opsForValue().set(redisKey, value, Duration.ofMinutes(30));
    }
}
