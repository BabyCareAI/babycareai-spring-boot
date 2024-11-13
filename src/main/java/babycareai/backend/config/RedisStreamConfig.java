package babycareai.backend.config;

import babycareai.backend.service.ImagePredictionListener;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;

import java.time.Duration;

@Configuration
public class RedisStreamConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    private final RedisTemplate<String, String> redisTemplate;

    private final RedisConnectionFactory redisConnectionFactory;
    private final ImagePredictionListener imagePredictionListener;

    public RedisStreamConfig(RedisTemplate<String, String> redisTemplate,
                             RedisConnectionFactory redisConnectionFactory, ImagePredictionListener imagePredictionListener) {
        this.redisTemplate = redisTemplate;
        this.redisConnectionFactory = redisConnectionFactory;
        this.imagePredictionListener = imagePredictionListener;
    }

    // 설명: Redis Stream에 대한 리스너를 시작합니다.
    @PostConstruct
    public void startListener() {
        // Stream and group names
        String streamKey = "diagnosis:image:url:stream";
        String groupName = "prediction_service_group";

        // Create consumer group if it doesn't exist
        createConsumerGroup(streamKey, groupName);

        // Set up the listener container options
        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(1))
                        .build();

        // Create the listener container
        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(redisConnectionFactory, options);

        // Start listening to the stream
        container.receive(Consumer.from(groupName, applicationName),
                StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
                imagePredictionListener);

        container.start();
    }

    private void createConsumerGroup(String streamKey, String groupName) {
        try {
            redisTemplate.opsForStream().groups(streamKey);
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.latest(), groupName);
        } catch (RedisSystemException e) {
            // 스트림이 존재하지 않으면
            if (e.getRootCause() instanceof io.lettuce.core.RedisBusyException) {
                System.out.println("소비자 그룹이 이미 존재합니다.");
                System.out.println("존재하는 소비자 그룹: " + redisTemplate.opsForStream().groups(streamKey));
                System.out.println("모든 키 출력 : " + redisTemplate.keys("*"));
            } else if (e.getRootCause() instanceof io.lettuce.core.RedisCommandExecutionException) {
                System.out.println("소비자 그룹이 없으므로 생성합니다.");
                redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.latest(), groupName);
                System.out.println("redisTemplate.opsForStream().groups(streamKey): " + redisTemplate.opsForStream().groups(streamKey));
                System.out.println("redisTemplate.keys(\"*\"): " + redisTemplate.keys("*"));
            } else {
                throw e;
            }
        } catch (Exception e) {
            // Consumer group might already exist
            // Handle other exceptions if necessary
        }
    }
}