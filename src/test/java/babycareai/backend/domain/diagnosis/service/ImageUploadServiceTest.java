package babycareai.backend.domain.diagnosis.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @Mock
    private AmazonS3Client s3Client;

    @InjectMocks
    private ImageUploadService imageUploadService;

    private final String bucketName = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageUploadService, "bucket", bucketName);
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void upload_Success() throws IOException {
        // given
        String diagnosisId = "test-diagnosis-id";
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // when
        String result = imageUploadService.upload(diagnosisId, file);

        // then
        assertThat(result).isEqualTo(diagnosisId);
        verify(s3Client).putObject(
                eq(bucketName),
                eq(diagnosisId),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );
    }
}