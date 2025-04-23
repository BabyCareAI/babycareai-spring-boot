package babycareai.backend.exception;

public class ImageClassificationException extends RuntimeException {
    private final String code;

    public ImageClassificationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ImageClassificationException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
