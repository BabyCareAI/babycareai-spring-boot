package babycareai.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/*
* redis stream에서 이미지 URL을 수신하고 SageMaker를 사용하여 이미지를 예측하기 위한 서비스
* redis 키: diagnosis:image:url:stream
* redis 소비자 그룹: prediction_service_group
* producers: ImageUploadService.java
* consumers: ImagePredictionListener.java
* */
@Service
@RequiredArgsConstructor
public class ImagePredictionListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final PredictService predictService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String imageUrl = message.getValue().get("imageUrl");

        try {
            JsonNode predictionResult = predictService.predict(imageUrl);
            // 예측 결과를 처리합니다(예: Redis 또는 데이터베이스에 저장).
            storePredictionResult(imageUrl, predictionResult);
            System.out.println("Prediction result: " + predictionResult);

            // 메시지 ACK를 수행하여 Redis에 처리 완료를 알립니다.
            redisTemplate.opsForStream().acknowledge("prediction_service_group", message);



        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }

    // Redis는 해당 메시지가 소비자(consumer)에게 처리되었음을 알리고, 그 메시지를 더 이상 재전송하지 않도록 하기
    private void acknowledgeMessage(MapRecord<String, String, String> message) {

    }




    private void storePredictionResult(String imageUrl, JsonNode predictionResult) {
        // 저장하는 로직을 구현합니다.
        // 다만, 굳이 저장할 이유가 생각이 듭니다.
        // 차라리 FastAPI에서 진단의 가장 마지막 단계에서 한꺼번에 저장하는 것이 좋을 것 같습니다.
    }
}