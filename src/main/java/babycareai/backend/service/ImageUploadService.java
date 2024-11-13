package babycareai.backend.service;

import babycareai.backend.util.RedisStreamHelper;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final AmazonS3Client s3Client;
    private final RedisStreamHelper redisStreamHelper;

    @Value("${s3.bucket}")
    private String bucket;

    public String upload(MultipartFile image) throws IOException {
        /* 업로드할 파일의 이름을 변경 */
        String originalFileName = image.getOriginalFilename();
        String fileName = changeFileName(originalFileName);

        /* S3에 업로드할 파일의 메타데이터 생성 */
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        /* S3에 파일 업로드 */
        s3Client.putObject(bucket, fileName, image.getInputStream(), metadata);

        /* 업로드한 파일의 S3 URL 주소*/
        String imageUrl = s3Client.getUrl(bucket, fileName).toString();

        // 업로드 성공 후 Redis Stream에 게시
        publishImageUrlToRedisStream(imageUrl);

        return imageUrl;
    }

    private String changeFileName(String originalFileName) {
        /* 업로드할 파일의 이름을 변경하는 로직 */
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return originalFileName + "_" + LocalDateTime.now().format(formatter);
    }

    private void publishImageUrlToRedisStream(String imageUrl) {
        Map<String, String> message = new HashMap<>();
        message.put("imageUrl", imageUrl);

        redisStreamHelper.publishToStream("diagnosis:image:url:stream", message);
    }

}