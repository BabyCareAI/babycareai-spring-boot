package babycareai.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImagePredictionListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final PredictService predictService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String imageUrl = message.getValue().get("imageUrl");

        try {
            JsonNode predictionResult = predictService.predict(imageUrl);
            // 예측 결과를 처리합니다(예: Redis 또는 데이터베이스에 저장).
            storePredictionResult(imageUrl, predictionResult);
            System.out.println("Prediction result: " + predictionResult);
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }



    private void storePredictionResult(String imageUrl, JsonNode predictionResult) {
        // 예측 결과를 저장하는 로직을 구현합니다.
        // 예를 들어 imageUrl을 키로 사용하여 Redis Hash에 저장합니다.
    }
}