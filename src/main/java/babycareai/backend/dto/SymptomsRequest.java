package babycareai.backend.dto;

import babycareai.backend.enums.SymptomType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SymptomsRequest {
    private String diagnosisId;
    private List<SymptomType> symptoms;
}