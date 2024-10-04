package babycareai.backend.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageUploadAndPredictService {

    private final AmazonS3Client s3Client;
    private final SageMakerRuntimeClient sageMakerRuntimeClient;
    private final ObjectMapper objectMapper;

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${sagemaker.endpoint.name}")
    private String sagemakerEndpointName;

    public JsonNode uploadAndPredict(MultipartFile image) throws IOException {

        String originalFileName = image.getOriginalFilename();
        String fileName = changeFileName(originalFileName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        s3Client.putObject(bucket, fileName, image.getInputStream(), metadata);

        // 현재는 사용하지 않음. 추후에 이미지 URL을 반환할 때 사용할 수 있음(ex. 기록 남기기)
        // String imageUrl = s3Client.getUrl(bucket, fileName).toString();

        // 업로드된 이미지에 대한 예측 요청
        String predictionJson = invokeSageMakerEndpoint(image);

        // JSON 문자열을 파싱하여 새로운 구조로 반환
        JsonNode predictionNode = objectMapper.readTree(predictionJson);
        JsonNode predictedClasses = predictionNode.get("predicted_classes");
        JsonNode probabilities = predictionNode.get("probabilities");

        // 새로운 JSON 형태로 결과 작성
        ObjectNode responseNode = objectMapper.createObjectNode();
        responseNode.set("predictionResult", predictedClasses);
        responseNode.set("probabilities", probabilities);

        return responseNode;
    }

    private String invokeSageMakerEndpoint(MultipartFile image) throws IOException {

        /*
        * 이미지 바이트를 그대로 전달
        * - 이미지 데이터를 바이트 형태로 전달하는 방식: SageMaker의 권장 패턴이기도 하고, 단순하고 성능적인 이점이 있다.
        * - s3 url을 전달하는 방식: 모델 내에서 S3 URL을 받아 이미지를 다운로드하여 처리하도록 구현해야 한다.
        * - 이미지 데이터를 Base64로 인코딩하여 전달하는 방식: 원본 데이터보다 약 33% 정도 크기가 증가하여 데이터 전송량이 늘어나고, SageMaker에서 디코딩하는 과정이 추가로 필요하다.
        * */
        byte[] imageBytes = image.getBytes();

        // SageMaker 엔드포인트 호출
        InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                .endpointName(sagemakerEndpointName)
                .contentType(image.getContentType())
                .body(SdkBytes.fromByteArray(imageBytes))
                .build();

        InvokeEndpointResponse response = sageMakerRuntimeClient.invokeEndpoint(request);

        // SageMaker 모델의 예측 결과 반환
        return response.body().asUtf8String();
    }

    private String changeFileName(String originalFileName) {
        return originalFileName + "_" + System.currentTimeMillis();
    }
}
