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
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @Tag(name = "이미지 업로드(image)", description = "이미지를 업로드하는 API")
    @Operation(summary = "이미지 업로드", description = "이미지를 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
            @ApiResponse(responseCode = "413", description = "이미지 크기 초과"),
            @ApiResponse(responseCode = "500", description = "이미지 업로드 실패")
    })
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<?> upload(@RequestParam("image") MultipartFile image) { // max-file-size=10MB로 설정시켜놓음.
        try {
            // 이미지 업로드 서비스 호출
            String imageUrl = imageUploadService.upload(image);

            // 성공적으로 업로드되었을 때의 응답
            return ResponseEntity.ok().body(Map.of("imageUrl", imageUrl));

        } catch (IOException e) {
            // 오류 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorMessage", "이미지 업로드 중 오류가 발생했습니다."));
        }
    }
}
