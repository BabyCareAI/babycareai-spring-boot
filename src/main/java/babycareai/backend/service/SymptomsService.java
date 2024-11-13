package babycareai.backend.service;

import babycareai.backend.enums.SymptomType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymptomsService {

    private final RedisTemplate<String, String> redisTemplate;
    public void saveSymptomsToRedis(String diagnosisId, List<SymptomType> symptoms) {
        // save symptom data to redis
        String redisKey = "symptoms:" + diagnosisId;
        String value = symptoms.toString();
        redisTemplate.opsForValue().set(redisKey, value);

        log.info("추가 증상 Redis에 저장 완료. Key: {}, Value: {}", redisKey, value);
    }
}
