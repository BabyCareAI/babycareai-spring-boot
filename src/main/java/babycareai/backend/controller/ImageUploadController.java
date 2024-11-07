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

    @Tag(name = "이미지 업로드", description = "이미지를 업로드하면 s3에 저장하고 해당이미지의 s3URL을 반환합니다.")
    @Operation(summary = "이미지 업로드", description = "이미지를 업로드하면 s3에 저장하고 해당이미지의 s3URL을 반환합니다. 동시에 Redis Stream에 게시되어 예측 메서드가 실행됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 업로드 성공"),
            @ApiResponse(responseCode = "400", description = "부적합한 파일 형식"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "413", description = "이미지 크기 초과"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류"),
            @ApiResponse(responseCode = "503", description = "서비스 불가 상태")
    })
    @CrossOrigin(origins = "${cors.allowedOrigins}")
    @PostMapping(value = "/api/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<?> upload(@RequestParam("image") MultipartFile image) {
        try {
            String imageUrl = imageUploadService.upload(image);
            return ResponseEntity.ok().body(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorMessage", "이미지 업로드 중 오류가 발생했습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errorMessage", "부적합한 파일 형식입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("errorMessage", "서비스를 이용할 수 없습니다."));
        }
    }
}