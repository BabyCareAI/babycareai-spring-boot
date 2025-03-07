package babycareai.backend.domain.diagnosis.service;

import babycareai.backend.domain.diagnosis.entity.SymptomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SymptomInputServiceTest {

    @InjectMocks
    private SymptomInputService symptomInputService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("증상 저장 성공")
    void saveSymptomsToRedis_성공() {
        // given
        String diagnosisId = "test-diagnosis-id";
        List<SymptomType> symptoms = Arrays.asList(SymptomType.FEVER, SymptomType.ITCHING);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        symptomInputService.saveSymptomsToRedis(diagnosisId, symptoms);

        // then
        verify(redisTemplate.opsForValue()).set(
                eq("symptoms:" + diagnosisId),
                anyString(),
                eq(Duration.ofMinutes(30))
        );
    }
}