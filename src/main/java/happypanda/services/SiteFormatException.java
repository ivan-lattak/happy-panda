package happypanda.services;

public class SiteFormatException extends HappyPandaException {

    @Override
    public String prettyMessage() {
        return "Unknown site format. Is the URL correct?";
    }

}
