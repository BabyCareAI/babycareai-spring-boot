package babycareai.backend.domain.diagnosis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "이미지 분류 응답")
public class ImageClassificationResponse {
    private boolean success;
    private String message;
    private String classificationResult;
    private String bodyPart;
} 