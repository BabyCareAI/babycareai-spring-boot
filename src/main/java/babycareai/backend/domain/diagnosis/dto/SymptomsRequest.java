package babycareai.backend.domain.diagnosis.dto;

import babycareai.backend.domain.diagnosis.entity.SymptomType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SymptomsRequest {
    private String diagnosisId;
    private List<SymptomType> symptoms;
}