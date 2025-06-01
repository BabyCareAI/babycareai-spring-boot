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
        // 불필요한 stubbing 제거. 필요한 테스트에서만 stubbing.
    }

    @Test
    @DisplayName("증상 저장 성공")
    void saveSymptomsToRedis_Success() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // given
        String diagnosisId = "test-diagnosis-id";
        Set<Symptoms> symptoms = Set.of(Symptoms.FEVER, Symptoms.ACHES_AND_PAINS);
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
                eq(Duration.ofHours(48))
        );
    }

    @Test
    @DisplayName("진단 ID가 null이면 SymptomException 발생")
    void saveSymptomsToRedis_NullDiagnosisId_ThrowsException() {
        Set<Symptoms> symptoms = Set.of(Symptoms.FEVER);
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                symptomService.saveSymptomsToRedis(null, symptoms)
        ).isInstanceOf(babycareai.backend.exception.SymptomException.class)
         .hasMessageContaining("진단 ID는 필수 값입니다.");
    }

    @Test
    @DisplayName("진단 ID가 빈 문자열이면 SymptomException 발생")
    void saveSymptomsToRedis_BlankDiagnosisId_ThrowsException() {
        Set<Symptoms> symptoms = Set.of(Symptoms.FEVER);
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                symptomService.saveSymptomsToRedis("   ", symptoms)
        ).isInstanceOf(babycareai.backend.exception.SymptomException.class)
         .hasMessageContaining("진단 ID는 필수 값입니다.");
    }

    @Test
    @DisplayName("증상 값이 null이면 SymptomException 발생")
    void saveSymptomsToRedis_NullSymptoms_ThrowsException() {
        String diagnosisId = "test-diagnosis-id";
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                symptomService.saveSymptomsToRedis(diagnosisId, null)
        ).isInstanceOf(babycareai.backend.exception.SymptomException.class)
         .hasMessageContaining("최소 1개 이상의 증상을 선택해야 합니다.");
    }

    @Test
    @DisplayName("증상 값이 비어 있으면 SymptomException 발생")
    void saveSymptomsToRedis_EmptySymptoms_ThrowsException() {
        String diagnosisId = "test-diagnosis-id";
        Set<Symptoms> symptoms = Set.of();
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                symptomService.saveSymptomsToRedis(diagnosisId, symptoms)
        ).isInstanceOf(babycareai.backend.exception.SymptomException.class)
         .hasMessageContaining("최소 1개 이상의 증상을 선택해야 합니다.");
    }

    @Test
    @DisplayName("직렬화 오류 발생 시 SymptomException 발생")
    void saveSymptomsToRedis_SerializationError_ThrowsException() throws Exception {
        String diagnosisId = "test-diagnosis-id";
        Set<Symptoms> symptoms = Set.of(Symptoms.FEVER);
        when(objectMapper.writeValueAsString(Map.of("symptoms", symptoms)))
                .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("직렬화 오류") {});
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                symptomService.saveSymptomsToRedis(diagnosisId, symptoms)
        ).isInstanceOf(babycareai.backend.exception.SymptomException.class)
         .hasMessageContaining("증상 정보 저장 중 오류가 발생했습니다");
    }
}
