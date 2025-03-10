package babycareai.backend.controller;

import babycareai.backend.domain.diagnosis.dto.ImageUploadResponse;
import babycareai.backend.domain.diagnosis.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @Tag(name = "이미지 업로드", description = "이미지 업로드 -> s3에 저장 -> 진단 ID, imageUrl 반환")
    @Operation(summary = "이미지 업로드", description = "이미지 업로드하면 s3에 저장하고 진단 ID와 imageUrl을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping(value = "/api/diagnosis/image-upload", consumes = {"multipart/form-data"})
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("image") MultipartFile image) throws IOException {
        String diagnosisId = UUID.randomUUID().toString();
        return ResponseEntity.ok(new ImageUploadResponse(imageUploadService.upload(diagnosisId, image)));
    }
}