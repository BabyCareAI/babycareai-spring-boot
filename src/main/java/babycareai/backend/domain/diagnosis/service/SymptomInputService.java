package babycareai.backend.domain.diagnosis.service;

import babycareai.backend.domain.diagnosis.entity.SymptomType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymptomInputService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    public void saveSymptomsToRedis(String diagnosisId, List<SymptomType> symptoms) {
        String redisKey = "symptoms:" + diagnosisId;
        Map<String, List<SymptomType>> symptomsMap = new HashMap<>();
        symptomsMap.put("symptoms", symptoms);
        String value;
        try {
            value = objectMapper.writeValueAsString(symptomsMap);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing symptoms", e);
        }
        redisTemplate.opsForValue().set(redisKey, value, Duration.ofMinutes(30));
        log.info("추가 증상 Redis에 저장 완료. Key: {}, Value: {}", redisKey, value);
    }
}
