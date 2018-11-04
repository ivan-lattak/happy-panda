package happypanda.services;

/**
 * The base class for all exceptions thrown by the HappyPanda application code.
 */
public abstract class HappyPandaException extends RuntimeException {

    public HappyPandaException() {
    }

    public HappyPandaException(String message) {
        super(message);
    }

    public HappyPandaException(String message, Throwable cause) {
        super(message, cause);
    }

    public HappyPandaException(Throwable cause) {
        super(cause);
    }

    public abstract String prettyMessage();

}
