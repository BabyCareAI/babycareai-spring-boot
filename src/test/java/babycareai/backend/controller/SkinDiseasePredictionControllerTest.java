package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.dto.ImageUploadResponse;
import babycareai.backend.domain.diagnosis.service.SkinDiseasePredictionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SkinDiseasePredictionController.class)
class SkinDiseasePredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SkinDiseasePredictionService skinDiseasePredictionService;

    @Test
    @DisplayName("피부질환 예측 성공")
    void predictSkinDisease_Success() throws Exception {
        // given
        String diagnosisId = "test-diagnosis-id";
        ImageUploadResponse request = new ImageUploadResponse(diagnosisId);
        doNothing().when(skinDiseasePredictionService).predictSkinDisease(diagnosisId);

        // when & then
        mockMvc.perform(post("/api/diagnosis/skin-disease-model-pedicition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(skinDiseasePredictionService).predictSkinDisease(diagnosisId);
    }

    @Test
    @DisplayName("피부질환 예측 실패 - 잘못된 요청")
    void predictSkinDisease_WithInvalidRequest_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/diagnosis/skin-disease-model-pedicition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

}


