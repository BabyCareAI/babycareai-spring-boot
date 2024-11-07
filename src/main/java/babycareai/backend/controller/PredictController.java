package babycareai.backend.controller;

import babycareai.backend.service.PredictService;
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

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PredictController {

    private final PredictService PredictService;

    @Tag(name = "이미지 예측", description = "이미지 url을 입력하면 s3에서 이미지를 다운로드하고 예측 결과를 반환합니다.")
    @Operation(summary = "이미지 예측", description = "이미지 업로드 api를 통해 반환받은 url을 입력하면 질환명을 예측하여 그 결과를 반환합니다. 실제로는 이미지가 업로드되면 Redis Stream에 게시되어 예측 메서드가 백엔드 내부에서 실행되기 때문에 이 api는 사용하지 않습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 예측 성공"),
            @ApiResponse(responseCode = "400", description = "부적합한 파일 형식"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 접근"),
            @ApiResponse(responseCode = "413", description = "이미지 크기 초과"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류"),
            @ApiResponse(responseCode = "503", description = "서비스 불가 상태")
    })
    @CrossOrigin(origins = "${cors.allowedOrigins}")
    @PostMapping(value = "/api/predict")
    public ResponseEntity<?> predict(@RequestParam("imageUrl") String imageUrl) {
        try {
            JsonNode predictionResult = PredictService.predict(imageUrl);
            return ResponseEntity.ok().body(Map.of("predictionResult", predictionResult));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorMessage", "이미지 예측 중 오류가 발생했습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("errorMessage", "부적합한 파일 형식입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("errorMessage", "서비스를 이용할 수 없습니다."));
        }
    }
}
