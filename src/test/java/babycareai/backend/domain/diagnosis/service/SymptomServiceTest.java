package babycareai.backend.domain.diagnosis.service;

import babycareai.backend.domain.diagnosis.enums.Symptoms;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SymptomServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SymptomService symptomService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("증상 저장 성공")
    void saveSymptomsToRedis_Success() throws Exception {
        // given
        String diagnosisId = "test-diagnosis-id";
        Set<Symptoms> symptoms = Set.of(Symptoms.ITCHINESS, Symptoms.RED_SPOTS_RASH);
        String expectedValue = "{\"symptoms\":[\"ITCHINESS\",\"RED_SPOTS_RASH\"]}";

        // objectMapper가 정상적으로 동작하도록 설정
        when(objectMapper.writeValueAsString(Map.of("symptoms", symptoms)))
                .thenReturn(expectedValue);

        // when
        symptomService.saveSymptomsToRedis(diagnosisId, symptoms);

        // then
        verify(valueOperations).set(
                eq("symptoms:" + diagnosisId),
                eq(expectedValue),
                eq(Duration.ofMinutes(30))
        );
    }

    @Test
    @DisplayName("증상 없이 저장 성공")
    void saveSymptomsToRedis_EmptySymptoms() throws Exception {
        // given
        String diagnosisId = "test-diagnosis-id";
        Set<Symptoms> symptoms = Set.of();
        String expectedValue = "{\"symptoms\":[]}";

        when(objectMapper.writeValueAsString(Map.of("symptoms", symptoms)))
                .thenReturn(expectedValue);

        // when
        symptomService.saveSymptomsToRedis(diagnosisId, symptoms);

        // then
        verify(valueOperations).set(
                eq("symptoms:" + diagnosisId),
                eq(expectedValue),
                eq(Duration.ofMinutes(30))
        );
    }
}
