package happypanda.services;

public class SadPandaException extends HappyPandaException {

    public SadPandaException(Throwable cause) {
        super(cause);
    }

    @Override
    public String prettyMessage() {
        return "A Sad Panda image was found. Are the cookies correct?";
    }

}
