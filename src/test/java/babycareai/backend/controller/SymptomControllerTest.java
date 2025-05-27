package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.dto.SymptomRequest;
import babycareai.backend.domain.diagnosis.enums.Symptoms;
import babycareai.backend.domain.diagnosis.service.SymptomService;
import babycareai.backend.exception.SymptomException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SymptomController.class)
class SymptomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SymptomService symptomService;

    @Test
    @DisplayName("증상 제출 성공")
    void submitSymptom_Success() throws Exception {
        // given
        String diagnosisId = "test-diagnosis-id";
        Set<Symptoms> symptoms = Set.of(Symptoms.FEVER, Symptoms.ACHES_AND_PAINS);
        SymptomRequest request = new SymptomRequest(diagnosisId, symptoms);

        // when & then
        mockMvc.perform(post("/api/v1/diagnosis/symptom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(symptomService).saveSymptomsToRedis(diagnosisId, request.getSymptoms());
    }

    @Test
    @DisplayName("증상 제출 실패 - 진단 ID 누락")
    void submitSymptom_MissingDiagnosisId_BadRequest() throws Exception {
        SymptomRequest request = new SymptomRequest(null, Set.of(Symptoms.FEVER));
        mockMvc.perform(post("/api/v1/diagnosis/symptom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail").value("진단 ID는 필수 값입니다."));
    }

    @Test
    @DisplayName("증상 제출 실패 - 증상 누락")
    void submitSymptom_MissingSymptoms_BadRequest() throws Exception {
        SymptomRequest request = new SymptomRequest("test-diagnosis-id", null);
        mockMvc.perform(post("/api/v1/diagnosis/symptom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.detail").value("증상은 필수 값입니다."));
    }

    @Test
    @DisplayName("증상 제출 실패 - 증상 빈 값")
    void submitSymptom_EmptySymptoms_BadRequest() throws Exception {
        SymptomRequest request = new SymptomRequest("test-diagnosis-id", Set.of());
        mockMvc.perform(post("/api/v1/diagnosis/symptom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("증상 제출 실패 - 서비스에서 SymptomException 발생 시 400 또는 500 반환")
    void submitSymptom_ServiceThrowsSymptomException_ErrorResponse() throws Exception {
        String diagnosisId = "test-diagnosis-id";
        Set<Symptoms> symptoms = Set.of(Symptoms.FEVER);
        SymptomRequest request = new SymptomRequest(diagnosisId, symptoms);
        doThrow(new SymptomException("SYMPTOM_SAVE_ERROR", "증상 저장 오류"))
                .when(symptomService).saveSymptomsToRedis(diagnosisId, symptoms);
        mockMvc.perform(post("/api/v1/diagnosis/symptom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString())
                        .contains("SYMPTOM_SAVE_ERROR"));
    }

    @Test
    @DisplayName("증상 제출 실패 - 잘못된 요청")
    void submitSymptom_WithInvalidRequest_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/diagnosis/symptom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}