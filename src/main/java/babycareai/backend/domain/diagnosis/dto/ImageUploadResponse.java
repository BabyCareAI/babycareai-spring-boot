package babycareai.backend.domain.diagnosis.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ImageUploadResponse {

    private String diagnosisId;

    @JsonCreator
    public ImageUploadResponse(@JsonProperty("diagnosisId") String diagnosisId) {
        this.diagnosisId = diagnosisId;
    }
}
