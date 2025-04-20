package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.enums.BodyPart;
import babycareai.backend.domain.diagnosis.service.ImageUploadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageUploadController.class)
class ImageUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageUploadService imageUploadService;

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImage_Success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        String diagnosisId = "test-diagnosis-id";
        BodyPart bodyPart = BodyPart.FACE;
        
        when(imageUploadService.upload(any(), any(), any())).thenReturn(diagnosisId);

        // when & then
        mockMvc.perform(multipart("/api/v1/diagnosis/image-upload")
                        .file(file)
                        .param("bodyPart", bodyPart.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosisId").value(diagnosisId));
    }

    @Test
    @DisplayName("파일이 없는 경우 이미지 업로드 실패")
    void uploadImage_WithoutFile_BadRequest() throws Exception {
        // when & then
        mockMvc.perform(multipart("/api/v1/diagnosis/image-upload")
                        .param("bodyPart", BodyPart.FACE.name()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("부위 정보가 없는 경우 이미지 업로드 실패")
    void uploadImage_WithoutBodyPart_BadRequest() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/diagnosis/image-upload")
                        .file(file))
                .andExpect(status().isBadRequest());
    }
}


