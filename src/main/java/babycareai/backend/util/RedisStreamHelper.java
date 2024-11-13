package babycareai.backend.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamHelper {

    private final RedisTemplate<String, String> redisTemplate;

    public void trimStream(String streamName, long maxLength) {
        redisTemplate.opsForStream().trim(streamName, maxLength);
        }

    public void publishToStream(String streamName, Map<String, String> message) {
        RecordId recordId = redisTemplate.opsForStream().add(streamName, message);
        // 스트림 트림
        trimStream(streamName, 1000);

        if (recordId == null) {
            log.error("Redis Stream에 메시지를 게시하는 데 실패했습니다.");
            // 예외 처리 로직 추가
        } else {
            log.info("Redis Stream에 메시지를 게시했습니다. recordId: {}", recordId);
        }
    }

    // acknowledge 메서드
    public void acknowledge(String groupName, String consumerName, RecordId recordId) {
        redisTemplate.opsForStream().acknowledge(groupName, consumerName, recordId);
    }
}
