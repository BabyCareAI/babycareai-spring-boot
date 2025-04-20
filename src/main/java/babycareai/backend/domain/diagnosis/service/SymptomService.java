package babycareai.backend.domain.diagnosis.service;

import babycareai.backend.domain.diagnosis.enums.Symptoms;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymptomService {

    private static final String REDIS_KEY_PREFIX = "symptoms:";
    private static final Duration REDIS_EXPIRATION = Duration.ofMinutes(30);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveSymptomsToRedis(String diagnosisId, Set<Symptoms> symptoms) {
        String redisKey = REDIS_KEY_PREFIX + diagnosisId;
        Map<String, Set<Symptoms>> symptomsMap = Map.of("symptoms", symptoms);
        
        try {
            String value = objectMapper.writeValueAsString(symptomsMap);
            redisTemplate.opsForValue().set(redisKey, value, REDIS_EXPIRATION);
            log.info("증상 정보가 Redis에 저장되었습니다. Key: {}, Symptoms: {}", redisKey, symptoms);
        } catch (JsonProcessingException e) {
            log.error("증상 정보 직렬화 중 오류 발생", e);
            throw new RuntimeException("증상 정보 저장 중 오류가 발생했습니다", e);
        }
    }
}
