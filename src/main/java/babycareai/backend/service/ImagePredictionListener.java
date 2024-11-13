package babycareai.backend.service;

import babycareai.backend.util.RedisStreamHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

/*
* redis stream에서 이미지 URL을 수신하고 SageMaker를 사용하여 이미지를 예측하기 위한 서비스
* redis 키: diagnosis:image:url:stream
* redis 소비자 그룹: prediction_service_group
* producers: ImageUploadService.java
* consumers: ImagePredictionListener.java
* */

@Slf4j
@Service
@RequiredArgsConstructor
public class ImagePredictionListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final PredictionService predictionService;
    private final RedisStreamHelper redisStreamHelper;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        String imageUrl = message.getValue().get("imageUrl");

        try {
            predictionService.predict(imageUrl);
            redisStreamHelper.acknowledge("prediction_service_group", "prediction_service", message.getId());

            } catch (Exception e) {
            log.error("Prediction failed for image: {}", imageUrl, e);
            redisStreamHelper.acknowledge("prediction_service_group", "prediction_service", message.getId());
        }
    }


}