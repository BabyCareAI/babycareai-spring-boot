package babycareai.backend.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointRequest;
import software.amazon.awssdk.services.sagemakerruntime.model.InvokeEndpointResponse;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageUploadAndPredictAndGenerateService {

    private final AmazonS3Client s3Client;
    private final SageMakerRuntimeClient sageMakerRuntimeClient;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${sagemaker.endpoint.name}")
    private String sagemakerEndpointName;

    @Value("${fastapi.url}")
    private String fastApiUrl;

    public JsonNode uploadAndPredict(MultipartFile image) throws IOException {

        String originalFileName = image.getOriginalFilename();
        String fileName = changeFileName(originalFileName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        // Upload image to S3 (if required)
        s3Client.putObject(bucket, fileName, image.getInputStream(), metadata);

        // Invoke SageMaker endpoint to get prediction
        String predictionJson = invokeSageMakerEndpoint(image);

        // Parse prediction result
        JsonNode predictionNode = objectMapper.readTree(predictionJson);
        JsonNode predictedClasses = predictionNode.get("predicted_classes");
        JsonNode probabilities = predictionNode.get("probabilities");

        // Get the top predicted class
        String topPredictedClass = predictedClasses.get(0).asText();

        // Map English disease name to Korean
        String diseaseName;
        try {
            // Map English disease name to Korean
            diseaseName = translateDiseaseName(topPredictedClass);
        } catch (IllegalArgumentException e) {
            // Handle the case where the disease is not recognized
            ObjectNode errorResponse = objectMapper.createObjectNode();
            errorResponse.put("errorMessage", "가장 높게 예측된 질환명은 지원되지 않는 질환명입니다: 예측된 질환명" + topPredictedClass + " (지원되는 질환명: 수족구, 광선 각화증, 기저세포 암종)");
            return errorResponse;
        }

        // Prepare JSON data to send to FastAPI
        ObjectNode dataToSend = objectMapper.createObjectNode();
        dataToSend.put("disease_name", diseaseName);
        dataToSend.put("fever_status", "아니오");
        dataToSend.put("blooding_status", "아니오");
        dataToSend.put("age", "3");
        dataToSend.put("symptoms", "가려움");

        // Send POST request to FastAPI
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(dataToSend.toString(), headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(fastApiUrl, requestEntity, String.class);

        // Get response from FastAPI
        String fastApiResponse = responseEntity.getBody();

        // Parse FastAPI response
        JsonNode fastApiResponseNode = objectMapper.readTree(fastApiResponse);

        // Return the LLM-generated response
        return fastApiResponseNode;
    }

    private String invokeSageMakerEndpoint(MultipartFile image) throws IOException {

        byte[] imageBytes = image.getBytes();

        // Invoke SageMaker endpoint
        InvokeEndpointRequest request = InvokeEndpointRequest.builder()
                .endpointName(sagemakerEndpointName)
                .contentType(image.getContentType())
                .body(SdkBytes.fromByteArray(imageBytes))
                .build();

        InvokeEndpointResponse response = sageMakerRuntimeClient.invokeEndpoint(request);

        // Return prediction result from SageMaker model
        return response.body().asUtf8String();
    }

    private String changeFileName(String originalFileName) {
        return originalFileName + "_" + System.currentTimeMillis();
    }

    private String translateDiseaseName(String englishName) {
        Map<String, String> translationMap = Map.of(
                "Basal cell carcinoma", "기저세포 암종",
                "HFMD", "수족구",
                "Actinic keratosis", "광선 각화증"
                // Add more mappings as needed
        );
        if (!translationMap.containsKey(englishName)) {
            throw new IllegalArgumentException("Predicted disease is not recognized: " + englishName);
        }
        return translationMap.get(englishName);
    }
}
