package happypanda.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final DefaultExceptionHandler instance = new DefaultExceptionHandler();

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        logger.warn("An uncaught exception occurred!", exception);
    }

    public static DefaultExceptionHandler getInstance() {
        return instance;
    }

}
