package babycareai.backend.controller;

import babycareai.backend.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService imageUploadService;

    @Tag(name = "이미지 업로드", description = "이미지를 업로드하는 API")
    @Operation(summary = "이미지 업로드", description = "이미지를 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이미지 업로드 성공", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = {@Content(mediaType = "application/json")})
    })
    @PostMapping("/upload")
    public String upload(MultipartFile image, Model model) throws IOException {
        /* 이미지 업로드 로직 */
        String imageUrl = imageUploadService.upload(image);

        /* View에게 전달할 데이터를 Model에 담음 */
        model.addAttribute("imageUrl", imageUrl);

        /* View의 이름을 반환하여 View를 렌더링하도록 함 */
        return "image";
    }
}