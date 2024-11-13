package babycareai.backend.controller;

import babycareai.backend.dto.SymptomsRequest;
import babycareai.backend.enums.SymptomType;
import babycareai.backend.service.SymptomsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SymptomController {

    private final SymptomsService symptomsService;

    @Tag(name = "증상 입력", description = "진단 ID와 증상 데이터 입력 -> 저장.")
    @Operation(summary = "증상 입력", description = "진단 ID와 증상 데이터를 받아 Redis에 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @CrossOrigin(origins = "${cors.allowedOrigins}")
    @PostMapping("/api/diagnosis/symptom")
    public ResponseEntity<Void> submitSymptom(@RequestBody SymptomsRequest symptomsRequest) {
        String diagnosisId = symptomsRequest.getDiagnosisId();
        List<SymptomType> symptomsData = symptomsRequest.getSymptoms();
        symptomsService.saveSymptomsToRedis(diagnosisId, symptomsData);
        return ResponseEntity.ok().build();
    }
}
