package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.dto.SymptomsRequest;
import babycareai.backend.domain.diagnosis.entity.SymptomType;
import babycareai.backend.domain.diagnosis.service.SymptomInputService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SymptomInputController {

    private final SymptomInputService symptomInputService;

    @Tag(name = "진단")
    @Operation(
            summary = "증상 입력",
            description = "진단 ID와 증상(리스트)을 받아 인메모리 데이터베이스에 저장합니다.\n\n" +
                    "순서:\n" +
                    "  1. 클라이언트: 진단 ID, 증상 입력(리스트)\n" +
                    "  2. 서버: 증상 저장\n" +
                    "  3. 서버: 상태 코드 200을 반환\n"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/api/diagnosis/symptom")
    public ResponseEntity<Void> submitSymptom(@Valid @RequestBody SymptomsRequest symptomsRequest) {
        String diagnosisId = symptomsRequest.getDiagnosisId();
        List<SymptomType> symptomsData = symptomsRequest.getSymptoms();
        symptomInputService.saveSymptomsToRedis(diagnosisId, symptomsData);
        return ResponseEntity.ok().build();
    }
}
