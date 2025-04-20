package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.dto.SymptomRequest;
import babycareai.backend.domain.diagnosis.entity.SymptomType;
import babycareai.backend.domain.diagnosis.service.SymptomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Set;

import static org.mockito.Mockito.verify;
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
        Set<SymptomType> symptoms = Set.of(SymptomType.ITCHINESS, SymptomType.FEVER_WARM_SKIN);
        SymptomRequest request = new SymptomRequest(diagnosisId, symptoms);

        // when & then
        mockMvc.perform(post("/api/v1/diagnosis/symptom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(symptomService).saveSymptomsToRedis(diagnosisId, request.getSymptoms());
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