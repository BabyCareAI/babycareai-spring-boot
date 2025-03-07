package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.dto.SymptomsRequest;
import babycareai.backend.domain.diagnosis.entity.SymptomType;
import babycareai.backend.domain.diagnosis.service.SymptomInputService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SymptomInputController.class)
class SymptomInputControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SymptomInputService symptomInputService;

    @Test
    @DisplayName("증상 제출 성공")
    void submitSymptom_성공() throws Exception {
        // given
        SymptomsRequest request = new SymptomsRequest();
        request.setDiagnosisId("test-diagnosis-id"); // diagnosisId 설정
        request.setSymptoms(Arrays.asList(SymptomType.FEVER, SymptomType.ITCHING)); // 증상 설정

        // 증상 저장 서비스의 동작을 모킹
        doNothing().when(symptomInputService).saveSymptomsToRedis(anyString(), any());

        // when & then
        mockMvc.perform(post("/api/diagnosis/symptom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("증상 제출 실패 - 잘못된 요청")
    void submitSymptom_잘못된요청_실패() throws Exception {
        // given
        SymptomsRequest request = new SymptomsRequest();
        // diagnosisId를 설정하지 않음

        // when & then
        mockMvc.perform(post("/api/diagnosis/symptom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 잘못된 요청이므로 400 에러가 발생해야 함
    }
}