package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.dto.ImageUploadResponse;
import babycareai.backend.domain.diagnosis.service.SkinDiseasePredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class SkinDiseasePredictionController {

    private final SkinDiseasePredictionService skinDiseasePredictionService;

    @Tag(name = "피부 질환 예측(배포한 예측 모델)", description = "진단 ID, 이미지 URL 입력 -> 피부 질환 예측 -> 결과 저장")
    @Operation(summary = "피부 질환 예측(배포한 예측 모델)", description = "진단 ID를 받으면 배포한 예측 모델이 피부 질환을 예측 후 결과를 Redis에 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/api/diagnosis/skin-disease-model-pedicition")
    public ResponseEntity<Void> predictSkinDisease(@RequestBody ImageUploadResponse imageUploadResponse) throws IOException {
        String diagnosisId = imageUploadResponse.getDiagnosisId();
        skinDiseasePredictionService.predictSkinDisease(diagnosisId);
        return ResponseEntity.ok().build();
    }

}
