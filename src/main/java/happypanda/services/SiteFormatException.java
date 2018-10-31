package happypanda.services;

public class SiteFormatException extends HappyPandaException {

    @Override
    public void handle(Thread thread) {
        System.err.println("The format of the site has changed.");
        System.err.println();
        printStackTrace();
    }

}
