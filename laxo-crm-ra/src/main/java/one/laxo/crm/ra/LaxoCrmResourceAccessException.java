package one.laxo.crm.ra;

public class LaxoCrmResourceAccessException extends RuntimeException {

    public LaxoCrmResourceAccessException(String message) {
        super(message);
    }

    public LaxoCrmResourceAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
