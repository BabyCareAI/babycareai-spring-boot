package babycareai.backend.exception;

import org.springframework.web.bind.MethodArgumentNotValidException;

import babycareai.backend.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ErrorResponse> handleImageUploadException(ImageUploadException e, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getCode())
                .message("이미지 업로드 중 오류가 발생했습니다.")
                .detail(e.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(S3UploadException.class)
    public ResponseEntity<ErrorResponse> handleS3UploadException(S3UploadException e, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getCode())
                .message("S3 업로드 중 오류가 발생했습니다.")
                .detail(e.getMessage())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("FILE_SIZE_EXCEEDED")
                .message("파일 크기가 너무 큽니다.")
                .detail("최대 허용 파일 크기를 초과했습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("IMAGE_UPLOAD_ERROR")
                .message("이미지 업로드 중 오류가 발생했습니다.")
                .detail(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("IMAGE_UPLOAD_ERROR")
                .message("이미지 업로드 중 오류가 발생했습니다.")
                .detail(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String detail = e.getBindingResult().getAllErrors().stream()
                .map(org.springframework.context.support.DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst().orElse(e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("INVALID_REQUEST")
                .message("요청 값이 올바르지 않습니다.")
                .detail(detail)
                .timestamp(java.time.LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ImageClassificationException.class)
    public ResponseEntity<ErrorResponse> handleImageClassificationException(ImageClassificationException e, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getCode())
                .message("이미지 분류 중 오류가 발생했습니다.")
                .detail(e.getMessage())
                .timestamp(java.time.LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("서버 내부 오류가 발생했습니다.")
                .detail(e.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 