package babycareai.backend.controller;

import babycareai.backend.service.ImageUploadAndPredictService;
import com.fasterxml.jackson.databind.JsonNode;
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
public class ImageUploadAndPredictController {

    private final ImageUploadAndPredictService imageUploadAndPredictService;

    @Tag(name = "이미지 업로드 및 예측", description = "이미지를 업로드하고 SageMaker를 통해 예측 결과를 반환하는 API")
    @Operation(summary = "이미지 업로드 및 예측", description = "이미지를 업로드하고, 해당 이미지에 대한 SageMaker 예측 결과를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예측 성공"),
            @ApiResponse(responseCode = "400", description = "부적합한 파일 형식"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "413", description = "이미지 크기 초과"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류"),
            @ApiResponse(responseCode = "503", description = "서비스 불가 상태")
    })
    @CrossOrigin(origins = "${cors.allowedOrigins}")
    @PostMapping(value = "/api/upload-and-predict", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadAndPredict(@RequestParam("image") MultipartFile image) {
        try {
            JsonNode predictionResult = imageUploadAndPredictService.uploadAndPredict(image);
            return ResponseEntity.ok().body(predictionResult);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorMessage", "이미지 업로드 또는 예측 중 오류가 발생했습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errorMessage", "부적합한 파일 형식입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("errorMessage", "서비스를 이용할 수 없습니다."));
        }
    }
}
