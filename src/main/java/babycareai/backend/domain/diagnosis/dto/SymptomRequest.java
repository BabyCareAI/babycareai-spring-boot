package babycareai.backend.domain.diagnosis.dto;

import babycareai.backend.domain.diagnosis.entity.SymptomType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SymptomRequest {

    @NotBlank(message = "진단 ID는 필수 값입니다.")
    private String diagnosisId;

    private List<SymptomType> symptoms;

    public SymptomRequest(String diagnosisId, List<SymptomType> symptoms) {
        this.diagnosisId = diagnosisId;
        this.symptoms = symptoms;
    }
}