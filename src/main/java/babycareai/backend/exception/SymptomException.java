package babycareai.backend.exception;

public class SymptomException extends RuntimeException {
    private final String code;

    public SymptomException(String code, String message) {
        super(message);
        this.code = code;
    }

    public SymptomException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
