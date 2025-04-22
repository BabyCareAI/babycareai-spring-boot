package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.enums.BodyPart;
import babycareai.backend.domain.diagnosis.service.ImageUploadService;
import babycareai.backend.exception.S3UploadException;
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IMAGE_MISSING"))
                .andExpect(jsonPath("$.message").value("이미지 업로드 중 오류가 발생했습니다."))
                .andExpect(jsonPath("$.detail").value("업로드할 이미지가 없습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/diagnosis/image-upload"));
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BODY_PART_MISSING"))
                .andExpect(jsonPath("$.message").value("이미지 업로드 중 오류가 발생했습니다."))
                .andExpect(jsonPath("$.detail").value("부위 정보가 누락되었습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/diagnosis/image-upload"));
    }

    @Test
    @DisplayName("빈 파일 업로드 시 예외 발생")
    void uploadImage_EmptyFile_ThrowsException() throws Exception {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/diagnosis/image-upload")
                        .file(emptyFile)
                        .param("bodyPart", BodyPart.FACE.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IMAGE_MISSING"))
                .andExpect(jsonPath("$.message").value("이미지 업로드 중 오류가 발생했습니다."))
                .andExpect(jsonPath("$.detail").value("업로드할 이미지가 없습니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/diagnosis/image-upload"));
    }

    @Test
    @DisplayName("이미지가 아닌 파일 업로드 시 예외 발생")
    void uploadImage_NonImageFile_ThrowsException() throws Exception {
        // given
        MockMultipartFile nonImageFile = new MockMultipartFile(
                "image",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/diagnosis/image-upload")
                        .file(nonImageFile)
                        .param("bodyPart", BodyPart.FACE.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IMAGE_INVALID_TYPE"))
                .andExpect(jsonPath("$.message").value("이미지 업로드 중 오류가 발생했습니다."))
                .andExpect(jsonPath("$.detail").value("이미지 파일만 업로드 가능합니다."))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/diagnosis/image-upload"));
    }

    @Test
    @DisplayName("S3 업로드 중 예외 발생 시 500 에러 반환")
    void uploadImage_S3UploadException_InternalServerError() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(imageUploadService.upload(any(), any(), any()))
                .thenThrow(new S3UploadException("S3_UPLOAD_ERROR", "S3 업로드 중 오류가 발생했습니다."));

        // when & then
        mockMvc.perform(multipart("/api/v1/diagnosis/image-upload")
                        .file(file)
                        .param("bodyPart", BodyPart.FACE.name()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("S3_UPLOAD_ERROR"))
                .andExpect(jsonPath("$.message").value("S3 업로드 중 오류가 발생했습니다."));
    }
}


