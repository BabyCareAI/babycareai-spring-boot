package babycareai.backend.domain.diagnosis.service;

import com.amazonaws.services.s3.AmazonS3Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @InjectMocks
    private ImageUploadService imageUploadService;

    @Mock
    private AmazonS3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageUploadService, "bucket", "test-bucket");
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void upload_성공() throws IOException {
        // given
        String fileName = "test.jpg";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/test.jpg";

        when(multipartFile.getOriginalFilename()).thenReturn(fileName);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(s3Client.getUrl(any(), any())).thenReturn(new URL(expectedUrl));

        // when
        String resultUrl = imageUploadService.upload(multipartFile);

        // then
        assertThat(resultUrl).isNotNull();
        assertThat(resultUrl).contains("https://test-bucket.s3.amazonaws.com");
    }
}