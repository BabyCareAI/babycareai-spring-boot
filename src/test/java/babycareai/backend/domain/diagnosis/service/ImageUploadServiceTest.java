package babycareai.backend.domain.diagnosis.service;

import babycareai.backend.domain.diagnosis.enums.BodyPart;
import babycareai.backend.exception.ImageUploadException;
import babycareai.backend.exception.S3UploadException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
        BodyPart bodyPart = BodyPart.FACE;
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // when
        String result = imageUploadService.upload(diagnosisId, file, bodyPart);

        // then
        assertThat(result).isEqualTo(diagnosisId);
        
        ArgumentCaptor<ObjectMetadata> metadataCaptor = ArgumentCaptor.forClass(ObjectMetadata.class);
        verify(s3Client).putObject(
                eq(bucketName),
                eq(diagnosisId),
                any(InputStream.class),
                metadataCaptor.capture()
        );
        
        ObjectMetadata metadata = metadataCaptor.getValue();
        assertThat(metadata.getUserMetaDataOf("bodyPart")).isEqualTo(bodyPart.name());
    }

    @Test
    @DisplayName("빈 파일 업로드 시 예외 발생")
    void upload_EmptyFile_ThrowsException() {
        // given
        String diagnosisId = "test-diagnosis-id";
        BodyPart bodyPart = BodyPart.FACE;
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        // when & then
        assertThatThrownBy(() -> imageUploadService.upload(diagnosisId, emptyFile, bodyPart))
                .isInstanceOf(ImageUploadException.class)
                .satisfies(ex -> {
                    ImageUploadException e = (ImageUploadException) ex;
                    assertThat(e.getCode()).isEqualTo("IMAGE_MISSING");
                    assertThat(e.getMessage()).isEqualTo("업로드할 이미지가 없습니다.");
                });
    }

    @Test
    @DisplayName("이미지가 아닌 파일 업로드 시 예외 발생")
    void upload_NonImageFile_ThrowsException() {
        // given
        String diagnosisId = "test-diagnosis-id";
        BodyPart bodyPart = BodyPart.FACE;
        MockMultipartFile nonImageFile = new MockMultipartFile(
                "image",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> imageUploadService.upload(diagnosisId, nonImageFile, bodyPart))
                .isInstanceOf(ImageUploadException.class)
                .satisfies(ex -> {
                    ImageUploadException e = (ImageUploadException) ex;
                    assertThat(e.getCode()).isEqualTo("IMAGE_INVALID_TYPE");
                    assertThat(e.getMessage()).isEqualTo("이미지 파일만 업로드 가능합니다.");
                });
    }

    @Test
    @DisplayName("S3 업로드 중 IO 예외 발생 시 ImageUploadException으로 변환")
    void upload_IOException_ThrowsImageUploadException() throws IOException {
        // given
        String diagnosisId = "test-diagnosis-id";
        BodyPart bodyPart = BodyPart.FACE;
        MockMultipartFile file = spy(new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        ));
        doThrow(new IOException("IO error")).when(file).getInputStream();

        // when & then
        assertThatThrownBy(() -> imageUploadService.upload(diagnosisId, file, bodyPart))
                .isInstanceOf(ImageUploadException.class)
                .satisfies(ex -> {
                    ImageUploadException e = (ImageUploadException) ex;
                    assertThat(e.getCode()).isEqualTo("IMAGE_UPLOAD_IO_ERROR");
                    assertThat(e.getMessage()).contains("이미지 업로드 중 IO 오류가 발생했습니다.");
                });
    }

    @Test
    @DisplayName("S3 업로드 중 기타 예외 발생 시 S3UploadException으로 변환")
    void upload_GeneralException_ThrowsS3UploadException() throws IOException {
        // given
        String diagnosisId = "test-diagnosis-id";
        BodyPart bodyPart = BodyPart.FACE;
        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        doThrow(new RuntimeException("S3 error")).when(s3Client).putObject(
                eq(bucketName),
                eq(diagnosisId),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );

        // when & then
        assertThatThrownBy(() -> imageUploadService.upload(diagnosisId, file, bodyPart))
                .isInstanceOf(S3UploadException.class)
                .satisfies(ex -> {
                    S3UploadException e = (S3UploadException) ex;
                    assertThat(e.getCode()).isEqualTo("S3_UPLOAD_ERROR");
                    assertThat(e.getMessage()).contains("S3 업로드 중 오류가 발생했습니다.");
                });
    }
}