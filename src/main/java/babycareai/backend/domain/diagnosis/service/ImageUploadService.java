package babycareai.backend.domain.diagnosis.service;

import babycareai.backend.domain.diagnosis.enums.BodyPart;
import babycareai.backend.exception.ImageUploadException;
import babycareai.backend.exception.S3UploadException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final AmazonS3Client s3Client;

    @Value("${s3.bucket}")
    private String bucket;

    public String upload(String diagnosisId, MultipartFile image, BodyPart bodyPart) {
        validateImage(image);

        try {
            // S3에 업로드할 파일의 메타데이터 생성
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(image.getContentType());
            metadata.setContentLength(image.getSize());
            metadata.addUserMetadata("bodyPart", bodyPart.name());

            // S3에 파일 업로드
            s3Client.putObject(bucket, diagnosisId, image.getInputStream(), metadata);

            log.info("이미지 업로드 완료 - 부위: {}", bodyPart.getKoreanName());
            return diagnosisId;
        } catch (IOException e) {
            log.error("이미지 업로드 중 IO 예외 발생 (진단ID: {})", diagnosisId, e);
            throw new ImageUploadException("IMAGE_UPLOAD_IO_ERROR", "이미지 업로드 중 IO 오류가 발생했습니다. 진단ID: " + diagnosisId, e);
        } catch (Exception e) {
            log.error("S3 업로드 중 예외 발생 (진단ID: {})", diagnosisId, e);
            throw new S3UploadException("S3_UPLOAD_ERROR", "S3 업로드 중 오류가 발생했습니다. 진단ID: " + diagnosisId, e);
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ImageUploadException("IMAGE_MISSING", "업로드할 이미지가 없습니다.");
        }

        if (image.getContentType() == null || !image.getContentType().startsWith("image/")) {
            throw new ImageUploadException("IMAGE_INVALID_TYPE", "이미지 파일만 업로드 가능합니다.");
        }
    }
}