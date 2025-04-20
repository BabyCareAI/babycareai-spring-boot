package babycareai.backend.domain.diagnosis.dto;

import babycareai.backend.domain.diagnosis.entity.SymptomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
public class SymptomRequest {

    @NotBlank(message = "진단 ID는 필수 값입니다.")
    private String diagnosisId;

    @NotNull(message = "증상은 필수 값입니다.")
    @Size(min = 1, message = "최소 1개 이상의 증상을 선택해야 합니다.")
    private Set<SymptomType> symptoms;

    public SymptomRequest(String diagnosisId, Set<SymptomType> symptoms) {
        this.diagnosisId = diagnosisId;
        this.symptoms = symptoms;
    }
}