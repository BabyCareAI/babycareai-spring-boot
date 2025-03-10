package babycareai.backend.domain.diagnosis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageUploadResponse {
    private String diagnosisId;
}
