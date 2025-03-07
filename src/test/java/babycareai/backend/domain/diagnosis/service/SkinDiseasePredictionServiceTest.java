package babycareai.backend.domain.diagnosis.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkinDiseasePredictionServiceTest {

    @InjectMocks
    private SkinDiseasePredictionService skinDiseasePredictionService;

    @Mock
    private AmazonS3Client s3Client;

    @Mock
    private SageMakerRuntimeClient sageMakerRuntimeClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("예측 성공")
    void predict_성공() throws IOException {
        // given
        String imageUrl = "https://test-bucket.s3.amazonaws.com/test.jpg";
        String diagnosisId = "test-diagnosis-id";
        byte[] imageBytes = "test".getBytes();

        ReflectionTestUtils.setField(skinDiseasePredictionService, "sagemakerEndpointName", "test-endpoint");

        ArrayNode mockArrayNode = JsonNodeFactory.instance.arrayNode();
        mockArrayNode.add("test-prediction");

        InvokeEndpointResponse mockResponse = InvokeEndpointResponse.builder()
                .body(SdkBytes.fromUtf8String("[\"test-prediction\"]"))
                .build();

        when(sageMakerRuntimeClient.invokeEndpoint(any(InvokeEndpointRequest.class)))
                .thenReturn(mockResponse);
        when(objectMapper.readTree(anyString())).thenReturn(mockArrayNode);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        skinDiseasePredictionService.predict(imageUrl, diagnosisId, imageBytes);

        // then
        verify(redisTemplate.opsForValue()).set(anyString(), anyString(), any());
    }
}