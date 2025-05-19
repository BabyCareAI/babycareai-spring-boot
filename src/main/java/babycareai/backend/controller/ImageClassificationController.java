package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.dto.ImageClassificationResponse;
import babycareai.backend.domain.diagnosis.dto.ImageUploadResponse;
import babycareai.backend.domain.diagnosis.service.ImageClassificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequiredArgsConstructor
public class ImageClassificationController {

    private final ImageClassificationService imageClassificationService;

    @Tag(name = "진단")
    @Operation(
            summary = "피부 질환 예측(배포한 예측 모델)",
            description = "진단 ID를 받으면 배포한 예측 모델이 피부 질환을 예측 후 결과를 인메모리 데이터베이스에 저장합니다.\n\n" +
                    "순서:\n" +
                    "  1. 클라이언트: 진단 ID 입력\n" +
                    "  2. 서버: 피부 질환 예측 후 결과 저장\n" +
                    "  3. 서버: 상태 코드 200을 반환\n"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ImageClassificationResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping(
            value = "/api/v1/diagnosis/classify",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    /*
      진단 ID를 받아 이미지 분류 결과를 반환합니다.
     */
    public ResponseEntity<ImageClassificationResponse> classifySkinDisease(@Valid @RequestBody ImageUploadResponse imageUploadResponse) {
        String diagnosisId = imageUploadResponse.getDiagnosisId();
        ImageClassificationResponse response = imageClassificationService.classifySkinDisease(diagnosisId);
        return ResponseEntity.ok(response);
    }
}
