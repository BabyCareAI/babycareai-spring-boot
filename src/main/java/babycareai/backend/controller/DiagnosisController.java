package babycareai.backend.controller;

import babycareai.backend.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class DiagnosisController {

    private final ImageUploadService imageUploadService;

    @Tag(name = "진단", description = "이미지를 업로드 -> s3에 저장 -> 예측 모델로 예측 -> 결과를 FastAPI로 전송")
    @Operation(summary = "진단", description = "이미지를 업로드하면 s3에 저장하고 예측 모델이 이미지를 예측하고 결과를 FastAPI로 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @CrossOrigin(origins = "${cors.allowedOrigins}")
    @PostMapping(value = "/api/diagnosis", consumes = {"multipart/form-data"})
    public ResponseEntity<Void> diagnosis(@RequestParam("image") MultipartFile image) {
        try {
            imageUploadService.upload(image);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}