package babycareai.backend.domain.diagnosis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "이미지 업로드 응답")
public class ImageUploadResponse {

    @NotBlank(message = "Diagnosis ID is required.")
    private String diagnosisId;

    @JsonCreator
    public ImageUploadResponse(@JsonProperty("diagnosisId") String diagnosisId) {
        this.diagnosisId = diagnosisId;
    }
}
