package babycareai.backend.domain.diagnosis.service;

import babycareai.backend.domain.diagnosis.entity.SymptomType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymptomInputService {

    private final RedisTemplate<String, String> redisTemplate;
    public void saveSymptomsToRedis(String diagnosisId, List<SymptomType> symptoms) {
        // save symptom data to redis
        String redisKey = "symptoms:" + diagnosisId;
        String value = symptoms.toString();
        redisTemplate.opsForValue().set(redisKey, value, Duration.ofMinutes(30));
        log.info("추가 증상 Redis에 저장 완료. Key: {}, Value: {}", redisKey, value);
    }
}
