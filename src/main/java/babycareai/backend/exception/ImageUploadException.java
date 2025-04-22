package babycareai.backend.exception;

public class ImageUploadException extends RuntimeException {
    private final String code;

    public ImageUploadException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ImageUploadException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
} 