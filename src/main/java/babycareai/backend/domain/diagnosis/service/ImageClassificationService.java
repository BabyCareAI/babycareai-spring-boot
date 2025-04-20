package babycareai.backend.domain.diagnosis.service;

import babycareai.backend.domain.diagnosis.dto.ImageClassificationResponse;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${sagemaker.endpoint.name}")
    private String sagemakerEndpointName;

    private static final double CONFIDENCE_THRESHOLD = 0.5; // 50%

    public ImageClassificationResponse classifySkinDisease(String diagnosisId) throws IOException {
        // 이미지 다운로드 및 메타데이터 조회
        S3Object imageObject = s3Client.getObject(bucket, diagnosisId);
        String bodyPart = imageObject.getObjectMetadata().getUserMetaDataOf("bodyPart");

        // 업로드된 이미지에 대한 예측 요청
        byte[] imageBytes = imageObject.getObjectContent().readAllBytes();
        String classificationResult = invokeSageMakerEndpoint(imageBytes);

        // JSON 파싱
        JsonNode resultNode = parseClassificationResult(classificationResult);
        
        // 최고 확률 찾기
        double maxProbability = findMaxProbability(resultNode);
        
        log.info("최고 확률: {}", maxProbability);

        // 확률이 기준치를 넘지 못하면 실패 반환
        if (maxProbability < CONFIDENCE_THRESHOLD) {
            return ImageClassificationResponse.builder()
                    .success(false)
                    .message("최고 확률이 기준치(50%)를 넘지 못했습니다.")
                    .bodyPart(bodyPart)
                    .classificationResult(classificationResult)
                    .build();
        }

        // Redis에 결과 저장 (부위 정보 포함)
        saveClassificationToRedis(diagnosisId, bodyPart, classificationResult);
        log.info("예측 결과 저장 완료. diagnosisId: {}, bodyPart: {}, classificationResult: {}", 
                diagnosisId, bodyPart, classificationResult);

        return ImageClassificationResponse.builder()
                .success(true)
                .message("이미지 분류가 성공적으로 완료되었습니다.")
                .bodyPart(bodyPart)
                .classificationResult(classificationResult)
                .build();
    }

    /**
     * 분류 결과 JSON 문자열을 파싱하여 JsonNode 객체로 변환합니다.
     * 
     * @param classificationResult 분류 결과 JSON 문자열
     * @return 파싱된 JsonNode 객체
     * @throws IOException JSON 파싱 중 오류 발생 시
     */
    private JsonNode parseClassificationResult(String classificationResult) throws IOException {
        JsonNode resultNode = objectMapper.readTree(classificationResult);
        log.info("파싱 결과: {}", resultNode);
        return resultNode;
    }

    /**
     * JsonNode 객체에서 최고 확률을 찾습니다.
     * 
     * @param resultNode 분류 결과 JsonNode 객체
     * @return 최고 확률 값
     */
    private double findMaxProbability(JsonNode resultNode) {
        double maxProbability = 0.0;
        
        // 배열 형태의 결과 처리
        if (resultNode.isArray()) {
            for (JsonNode item : resultNode) {
                if (item.has("probability")) {
                    double probability = item.get("probability").asDouble();
                    maxProbability = Math.max(maxProbability, probability);
                }
            }
        } else {
            log.warn("예상치 못한 결과 형식: {}", resultNode);
        }
        
        return maxProbability;
    }

    private String invokeSageMakerEndpoint(byte[] imageBytes) throws IOException {
        InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                .endpointName(sagemakerEndpointName)
                .contentType("application/x-image")
                .body(SdkBytes.fromByteArray(imageBytes))
                .build();

        InvokeEndpointResponse response = sageMakerRuntimeClient.invokeEndpoint(request);
        return response.body().asUtf8String();
    }

    private void saveClassificationToRedis(String diagnosisId, String bodyPart, String classificationResult) {
        String redisKey = "classification:" + diagnosisId;
        String value = String.format("{\"result\":%s,\"bodyPart\":\"%s\"}", classificationResult, bodyPart);
        redisTemplate.opsForValue().set(redisKey, value, Duration.ofMinutes(30));
    }
}
