package babycareai.backend.domain.diagnosis.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageClassificationServiceTest {

    @Mock
    private AmazonS3Client s3Client;

    @Mock
    private SageMakerRuntimeClient sageMakerRuntimeClient;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ImageClassificationService imageClassificationService;

    private final String bucketName = "test-bucket";
    private final String endpointName = "test-endpoint";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageClassificationService, "bucket", bucketName);
        ReflectionTestUtils.setField(imageClassificationService, "sagemakerEndpointName", endpointName);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("피부질환 예측 성공")
    void predictSkinDisease_Success() throws IOException {
        // given
        String diagnosisId = "test-diagnosis-id";
        byte[] imageBytes = "test image content".getBytes();
        String predictionResult = "test prediction result";

        // S3 mock setup
        S3Object s3Object = mock(S3Object.class);
        S3ObjectInputStream inputStream = new S3ObjectInputStream(
                new ByteArrayInputStream(imageBytes),
                null
        );
        when(s3Object.getObjectContent()).thenReturn(inputStream);
        when(s3Client.getObject(eq(bucketName), eq(diagnosisId))).thenReturn(s3Object);

        // SageMaker mock setup
        InvokeEndpointResponse sageMakerResponse = mock(InvokeEndpointResponse.class);
        when(sageMakerResponse.body()).thenReturn(SdkBytes.fromUtf8String(predictionResult));
        when(sageMakerRuntimeClient.invokeEndpoint(any(InvokeEndpointRequest.class)))
                .thenReturn(sageMakerResponse);

        // when
        imageClassificationService.classifySkinDisease(diagnosisId);

        // then
        verify(s3Client).getObject(eq(bucketName), eq(diagnosisId));

        // 명시적인 InvokeEndpointRequest를 사용하여 검증
        // SageMaker SDK의 InvokeEndpointRequest는 equals() 메서드가 구현되어 있지 않아서 any()로 검증이 불가능
        InvokeEndpointRequest expectedRequest = InvokeEndpointRequest.builder()
                .endpointName(endpointName)
                .contentType("application/x-image")
                .body(SdkBytes.fromByteArray(imageBytes))
                .build();

        verify(sageMakerRuntimeClient).invokeEndpoint(eq(expectedRequest));

        // Redis에 예측 결과 저장 검증
        verify(valueOperations).set(
                eq("prediction:" + diagnosisId),
                eq(String.format("{\"predictionResult\":%s}", predictionResult)),
                eq(Duration.ofMinutes(30))
        );
    }
}
