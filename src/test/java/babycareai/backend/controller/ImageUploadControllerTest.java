package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.service.ImageUploadService;
import babycareai.backend.domain.diagnosis.service.SkinDiseasePredictionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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

    @MockBean
    private SkinDiseasePredictionService skinDiseasePredictionService;

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImage_성공() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile( // 테스트용 이미지 파일 생성
                "image",             // 파라미터 이름
                "test.jpg",          // 파일 이름
                "image/jpeg",        // 파일 타입
                "test image content".getBytes()  // 파일 내용
        );

        // 이미지 업로드 서비스의 동작을 모킹, 업로드 후 URL 반환
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/test.jpg";

        when(imageUploadService.upload(any())).thenReturn(expectedUrl);
        // 예측 서비스의 동작을 모킹
        doNothing().when(skinDiseasePredictionService).predict(anyString(), anyString(), any());

        // when & then
        mockMvc.perform(multipart("/api/diagnosis/upload")
                        .file(file))  // 파일 첨부
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosisId").exists());  // 응답에서 diagnosisId가 있는지 확인
    }

    @Test
    @DisplayName("이미지 업로드 실패 - 이미지 없음")
    void uploadImage_이미지없음_실패() throws Exception {
        // when & then
        mockMvc.perform(multipart("/api/diagnosis/upload"))  // 파일 없이 요청
                .andExpect(status().isBadRequest());  // HTTP 상태가 400 BadRequest인지 확인
    }
}
