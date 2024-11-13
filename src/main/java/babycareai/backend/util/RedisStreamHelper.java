package babycareai.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisStreamHelper {

    private final RedisTemplate<String, String> redisTemplate;

    public void trimStream(String streamName, long maxLength) {
        redisTemplate.opsForStream().trim(streamName, maxLength);
        System.out.println("Stream " + streamName + "을(를) 최대 " + maxLength + "개의 메시지로 트림했습니다.");
    }

    public void publishToStream(String streamName, Map<String, String> message) {
        System.out.println("Redis Stream에 메시지 게시: " + message);

        RecordId recordId = redisTemplate.opsForStream().add(streamName, message);

        // 스트림 트림
        trimStream(streamName, 1000);

        if (recordId == null) {
            System.out.println("Redis Stream에 메시지를 게시하지 못했습니다.");
            // 예외 처리 로직 추가
        } else {
            System.out.println("Redis Stream에 게시된 메시지 ID: " + recordId);
        }

    }
}
