package babycareai.backend.controller;

import babycareai.backend.service.ImageUploadService;
import babycareai.backend.service.SkinDiseasePredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;
    private final SkinDiseasePredictionService skinDiseasePredictionService;

    @Tag(name = "이미지 업로드", description = "이미지 업로드 -> 질환 예측 -> 결과 저장 -> 진단 ID 반환")
    @Operation(summary = "이미지 업로드", description = "증상이 있는 신체 부위를 찍은 이미지를 업로드 하면 이미지 예측 모델이 질환을 예측하고 그 결과를 redis에 저장합니다. 그리고 진단 ID를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @CrossOrigin(origins = "${cors.allowedOrigins}")
    @PostMapping(value = "/api/diagnosis/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile image) throws IOException {
        String diagnosisId = UUID.randomUUID().toString();
        skinDiseasePredictionService.predict(imageUploadService.upload(image), diagnosisId);
        return ResponseEntity.ok(diagnosisId);
    }
}