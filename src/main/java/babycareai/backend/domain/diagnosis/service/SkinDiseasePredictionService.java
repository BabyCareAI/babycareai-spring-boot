package babycareai.backend.domain.diagnosis.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
public class SkinDiseasePredictionService {

    private final AmazonS3Client s3Client;
    private final SageMakerRuntimeClient sageMakerRuntimeClient;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${sagemaker.endpoint.name}")
    private String sagemakerEndpointName;

    public void predict(String imageUrl, String diagnosisId, byte[] imageBytes) throws IOException {
        // 업로드된 이미지에 대한 예측 요청
        String predictionJson = invokeSageMakerEndpoint(imageBytes);

        // 예측 JSON을 배열로 파싱
        ArrayNode predictionArray = (ArrayNode) objectMapper.readTree(predictionJson);

        // diagnosisId, imageUrl, predictionResult Redis에 저장
        savePredictionToRedis(diagnosisId, imageUrl, predictionArray.toString());
        log.info("예측 결과 저장 완료. diagnosisId: {}, imageUrl: {}, predictionResult: {}", diagnosisId, imageUrl, predictionArray);
    }

    private S3Object downloadImage(String imageUrl) throws IOException {

            // S3 URL에서 파일 이름 추출
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            // S3에서 이미지 다운로드
            return s3Client.getObject(bucket, fileName);
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

    private void savePredictionToRedis(String diagnosisId, String imageUrl, String predictionResult) {
        String redisKey = "prediction:" + diagnosisId;

        String value = String.format("{\"imageUrl\":\"%s\",\"predictionResult\":%s}", imageUrl, predictionResult);

        // Redis에 예측 데이터 저장
        redisTemplate.opsForValue().set(redisKey, value, Duration.ofMinutes(30));
    }
}
