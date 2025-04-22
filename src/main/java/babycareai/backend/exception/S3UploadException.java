package babycareai.backend.exception;

public class S3UploadException extends RuntimeException {
    private final String code;

    public S3UploadException(String code, String message) {
        super(message);
        this.code = code;
    }

    public S3UploadException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
} 