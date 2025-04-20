package babycareai.backend.domain.diagnosis.service;

import babycareai.backend.domain.diagnosis.enums.BodyPart;
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

    public String upload(String diagnosisId, MultipartFile image, BodyPart bodyPart) throws IOException {
        // S3에 업로드할 파일의 메타데이터 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());
        metadata.addUserMetadata("bodyPart", bodyPart.name());

        // S3에 파일 업로드
        s3Client.putObject(bucket, diagnosisId, image.getInputStream(), metadata);

        log.info("이미지 업로드 완료 - 부위: {}", bodyPart.getKoreanName());
        return diagnosisId;
    }
}