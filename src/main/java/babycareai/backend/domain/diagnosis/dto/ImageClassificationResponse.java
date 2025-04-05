package babycareai.backend.domain.diagnosis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageClassificationResponse {
    private boolean success;
    private String message;
    private String classificationResult;
} 