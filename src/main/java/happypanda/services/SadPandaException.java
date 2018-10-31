package happypanda.services;

public class SadPandaException extends HappyPandaException {

    public SadPandaException(Throwable cause) {
        super(cause);
    }

    @Override
    public void handle(Thread thread) {
        System.err.println("A Sad Panda image was accessed by thread: " + thread.getName());
        System.err.println("Maybe you forgot to set your cookies?");
        System.err.println();
        printStackTrace();
    }

}
