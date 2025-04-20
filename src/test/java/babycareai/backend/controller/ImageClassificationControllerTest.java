package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.dto.ImageClassificationResponse;
import babycareai.backend.domain.diagnosis.dto.ImageUploadResponse;
import babycareai.backend.domain.diagnosis.service.ImageClassificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageClassificationController.class)
class ImageClassificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ImageClassificationService imageClassificationService;

    @Test
    @DisplayName("피부질환 예측 성공 - 확률이 기준치를 넘는 경우")
    void predictSkinDisease_Success() throws Exception {
        // given
        String diagnosisId = "test-diagnosis-id";
        ImageUploadResponse request = new ImageUploadResponse(diagnosisId);
        ImageClassificationResponse response = ImageClassificationResponse.builder()
                .success(true)
                .message("이미지 분류가 성공적으로 완료되었습니다.")
                .bodyPart("face")
                .classificationResult("[{\"class\":\"shingles\",\"probability\":0.8},{\"class\":\"Chickenpox\",\"probability\":0.2}]")
                .build();
        when(imageClassificationService.classifySkinDisease(diagnosisId)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/diagnosis/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("이미지 분류가 성공적으로 완료되었습니다."))
                .andExpect(jsonPath("$.bodyPart").value("face"))
                .andExpect(jsonPath("$.classificationResult").value("[{\"class\":\"shingles\",\"probability\":0.8},{\"class\":\"Chickenpox\",\"probability\":0.2}]"));
    }

    @Test
    @DisplayName("피부질환 예측 실패 - 확률이 기준치를 넘지 못하는 경우")
    void predictSkinDisease_Failure() throws Exception {
        // given
        String diagnosisId = "test-diagnosis-id";
        ImageUploadResponse request = new ImageUploadResponse(diagnosisId);
        ImageClassificationResponse response = ImageClassificationResponse.builder()
                .success(false)
                .message("최고 확률이 기준치(50%)를 넘지 못했습니다.")
                .bodyPart("face")
                .classificationResult("[{\"class\":\"shingles\",\"probability\":0.3},{\"class\":\"Chickenpox\",\"probability\":0.2}]")
                .build();
        when(imageClassificationService.classifySkinDisease(diagnosisId)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/diagnosis/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("최고 확률이 기준치(50%)를 넘지 못했습니다."))
                .andExpect(jsonPath("$.bodyPart").value("face"))
                .andExpect(jsonPath("$.classificationResult").value("[{\"class\":\"shingles\",\"probability\":0.3},{\"class\":\"Chickenpox\",\"probability\":0.2}]"));
    }

    @Test
    @DisplayName("피부질환 예측 실패 - 잘못된 요청")
    void predictSkinDisease_WithInvalidRequest_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/diagnosis/classify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}


