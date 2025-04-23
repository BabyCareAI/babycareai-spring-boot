package babycareai.backend.domain.diagnosis.service;

import babycareai.backend.domain.diagnosis.dto.ImageClassificationResponse;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ImageClassificationService imageClassificationService;

    private final String bucketName = "test-bucket";
    private final String endpointName = "test-endpoint";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageClassificationService, "bucket", bucketName);
        ReflectionTestUtils.setField(imageClassificationService, "sagemakerEndpointName", endpointName);
    }

    @Test
    @DisplayName("피부질환 예측 성공 - 배열 형태의 결과, 확률이 기준치를 넘는 경우")
    void classifySkinDisease_Success() throws IOException {
        // given
        String diagnosisId = "test-diagnosis-id";
        String bodyPart = "face";
        byte[] imageBytes = "test image content".getBytes();
        String predictionResult = "[{\"class\":\"shingles\",\"probability\":0.8},{\"class\":\"Chickenpox\",\"probability\":0.2}]";

        // S3 mock setup
        S3Object s3Object = mock(S3Object.class);
        ObjectMetadata metadata = mock(ObjectMetadata.class);
        when(metadata.getUserMetaDataOf("bodyPart")).thenReturn(bodyPart);
        when(s3Object.getObjectMetadata()).thenReturn(metadata);
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

        // ObjectMapper mock setup
        JsonNode mockJsonNode = mock(JsonNode.class);
        when(objectMapper.readTree(predictionResult)).thenReturn(mockJsonNode);
        when(mockJsonNode.isArray()).thenReturn(true);
        
        // JsonNode iterator 설정
        JsonNode mockItem1 = mock(JsonNode.class);
        JsonNode mockItem2 = mock(JsonNode.class);
        
        // iterator() 메서드 스터빙
        Iterator<JsonNode> iterator = List.of(mockItem1, mockItem2).iterator();
        when(mockJsonNode.iterator()).thenReturn(iterator);
        
        when(mockItem1.has("probability")).thenReturn(true);
        when(mockItem2.has("probability")).thenReturn(true);
        when(mockItem1.get("probability")).thenReturn(mockItem1);
        when(mockItem2.get("probability")).thenReturn(mockItem2);
        when(mockItem1.asDouble()).thenReturn(0.8);
        when(mockItem2.asDouble()).thenReturn(0.2);
        
        // Redis mock setup
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        ImageClassificationResponse response = imageClassificationService.classifySkinDisease(diagnosisId);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("이미지 분류가 성공적으로 완료되었습니다.");
        assertThat(response.getClassificationResult()).isEqualTo(predictionResult);

        verify(s3Client).getObject(eq(bucketName), eq(diagnosisId));
        String expectedRedisValue = String.format("{\"result\":%s,\"bodyPart\":\"%s\"}", predictionResult, bodyPart);
        verify(valueOperations).set(
                eq("classification:" + diagnosisId),
                eq(expectedRedisValue),
                eq(Duration.ofMinutes(30))
        );
    }

    @Test
    @DisplayName("피부질환 예측 실패 - 확률이 기준치를 넘지 못하는 경우")
    void classifySkinDisease_Failure() throws IOException {
        // given
        String diagnosisId = "test-diagnosis-id";
        String bodyPart = "face";
        byte[] imageBytes = "test image content".getBytes();
        String predictionResult = "[{\"class\":\"shingles\",\"probability\":0.3},{\"class\":\"Chickenpox\",\"probability\":0.2}]";

        // S3 mock setup
        S3Object s3Object = mock(S3Object.class);
        ObjectMetadata metadata = mock(ObjectMetadata.class);
        when(metadata.getUserMetaDataOf("bodyPart")).thenReturn(bodyPart);
        when(s3Object.getObjectMetadata()).thenReturn(metadata);
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

        // ObjectMapper mock setup
        JsonNode mockJsonNode = mock(JsonNode.class);
        when(objectMapper.readTree(predictionResult)).thenReturn(mockJsonNode);
        when(mockJsonNode.isArray()).thenReturn(true);
        
        // JsonNode iterator 설정
        JsonNode mockItem1 = mock(JsonNode.class);
        JsonNode mockItem2 = mock(JsonNode.class);
        
        // iterator() 메서드 스터빙
        Iterator<JsonNode> iterator = List.of(mockItem1, mockItem2).iterator();
        when(mockJsonNode.iterator()).thenReturn(iterator);
        
        when(mockItem1.has("probability")).thenReturn(true);
        when(mockItem2.has("probability")).thenReturn(true);
        when(mockItem1.get("probability")).thenReturn(mockItem1);
        when(mockItem2.get("probability")).thenReturn(mockItem2);
        when(mockItem1.asDouble()).thenReturn(0.3);
        when(mockItem2.asDouble()).thenReturn(0.2);

        // when
        ImageClassificationResponse response = imageClassificationService.classifySkinDisease(diagnosisId);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("최고 확률이 기준치(50%)를 넘지 못했습니다.");
        assertThat(response.getClassificationResult()).isEqualTo(predictionResult);
        assertThat(response.getBodyPart()).isEqualTo(bodyPart);

        verify(s3Client).getObject(eq(bucketName), eq(diagnosisId));
        verify(valueOperations, never()).set(any(), any(), any());
    }

    @Test
    @DisplayName("S3에서 진단 ID를 찾을 수 없을 때 ImageClassificationException 발생")
    void classifySkinDisease_S3Exception() {
        String diagnosisId = "not-found-id";
        when(s3Client.getObject(anyString(), anyString())).thenThrow(new com.amazonaws.services.s3.model.AmazonS3Exception("S3 not found"));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> imageClassificationService.classifySkinDisease(diagnosisId))
                .isInstanceOf(babycareai.backend.exception.ImageClassificationException.class)
                .hasFieldOrPropertyWithValue("code", "S3_ERROR");
    }

    @Test
    @DisplayName("SageMaker 클라이언트 예외 발생 시 ImageClassificationException 발생")
    void classifySkinDisease_SageMakerClientException() {
        String diagnosisId = "test-id";
        S3Object s3Object = mock(S3Object.class);
        ObjectMetadata metadata = mock(ObjectMetadata.class);
        when(metadata.getUserMetaDataOf("bodyPart")).thenReturn("face");
        when(s3Object.getObjectMetadata()).thenReturn(metadata);
        when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(new ByteArrayInputStream(new byte[0]), null));
        when(s3Client.getObject(anyString(), anyString())).thenReturn(s3Object);
        when(sageMakerRuntimeClient.invokeEndpoint(any(InvokeEndpointRequest.class)))
                .thenThrow(software.amazon.awssdk.core.exception.SdkClientException.builder().message("SageMaker error").build());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> imageClassificationService.classifySkinDisease(diagnosisId))
                .isInstanceOf(babycareai.backend.exception.ImageClassificationException.class)
                .hasFieldOrPropertyWithValue("code", "S3_CLIENT_ERROR");
    }

    @Test
    @DisplayName("IO 예외 발생 시 ImageClassificationException 발생")
    void classifySkinDisease_IOException() throws Exception {
        String diagnosisId = "test-id";
        S3Object s3Object = mock(S3Object.class);
        ObjectMetadata metadata = mock(ObjectMetadata.class);
        when(metadata.getUserMetaDataOf("bodyPart")).thenReturn("face");
        when(s3Object.getObjectMetadata()).thenReturn(metadata);
        when(s3Object.getObjectContent()).thenThrow(new RuntimeException("IO error"));
        when(s3Client.getObject(anyString(), anyString())).thenReturn(s3Object);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> imageClassificationService.classifySkinDisease(diagnosisId))
                .isInstanceOf(babycareai.backend.exception.ImageClassificationException.class)
                .hasFieldOrPropertyWithValue("code", "IO_ERROR");
    }

    @Test
    @DisplayName("알 수 없는 예외 발생 시 ImageClassificationException(UNKNOWN_ERROR) 발생")
    void classifySkinDisease_UnknownException() {
        String diagnosisId = "test-id";
        when(s3Client.getObject(anyString(), anyString())).thenThrow(new RuntimeException("unknown error"));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> imageClassificationService.classifySkinDisease(diagnosisId))
                .isInstanceOf(babycareai.backend.exception.ImageClassificationException.class)
                .hasFieldOrPropertyWithValue("code", "UNKNOWN_ERROR");
    }

    @Test
    @DisplayName("분류 결과 저장 시 Redis 예외가 발생해도 ImageClassificationException이 발생하지 않음(현재 구조) - 정상 흐름")
    void classifySkinDisease_RedisException() throws IOException {
        String diagnosisId = "test-diagnosis-id";
        String bodyPart = "face";
        byte[] imageBytes = "test image content".getBytes();
        String predictionResult = "[{\"class\":\"shingles\",\"probability\":0.8},{\"class\":\"Chickenpox\",\"probability\":0.2}]";
        S3Object s3Object = mock(S3Object.class);
        ObjectMetadata metadata = mock(ObjectMetadata.class);
        when(metadata.getUserMetaDataOf("bodyPart")).thenReturn(bodyPart);
        when(s3Object.getObjectMetadata()).thenReturn(metadata);
        S3ObjectInputStream inputStream = new S3ObjectInputStream(
                new ByteArrayInputStream(imageBytes),
                null
        );
        when(s3Object.getObjectContent()).thenReturn(inputStream);
        when(s3Client.getObject(eq(bucketName), eq(diagnosisId))).thenReturn(s3Object);
        InvokeEndpointResponse sageMakerResponse = mock(InvokeEndpointResponse.class);
        when(sageMakerResponse.body()).thenReturn(SdkBytes.fromUtf8String(predictionResult));
        when(sageMakerRuntimeClient.invokeEndpoint(any(InvokeEndpointRequest.class)))
                .thenReturn(sageMakerResponse);
        JsonNode mockJsonNode = mock(JsonNode.class);
        when(objectMapper.readTree(predictionResult)).thenReturn(mockJsonNode);
        when(mockJsonNode.isArray()).thenReturn(true);
        JsonNode mockItem1 = mock(JsonNode.class);
        JsonNode mockItem2 = mock(JsonNode.class);
        Iterator<JsonNode> iterator = List.of(mockItem1, mockItem2).iterator();
        when(mockJsonNode.iterator()).thenReturn(iterator);
        when(mockItem1.has("probability")).thenReturn(true);
        when(mockItem2.has("probability")).thenReturn(true);
        when(mockItem1.get("probability")).thenReturn(mockItem1);
        when(mockItem2.get("probability")).thenReturn(mockItem2);
        when(mockItem1.asDouble()).thenReturn(0.8);
        when(mockItem2.asDouble()).thenReturn(0.2);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("Redis error")).when(valueOperations).set(any(), any(), any());
        ImageClassificationResponse response = imageClassificationService.classifySkinDisease(diagnosisId);
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("이미지 분류가 성공적으로 완료되었습니다.");
        assertThat(response.getClassificationResult()).isEqualTo(predictionResult);
    }
}

