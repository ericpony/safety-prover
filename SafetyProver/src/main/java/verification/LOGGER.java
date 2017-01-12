package verification;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

class LOGGER {
    private static Logger LOGGER = null;

    public static void setLevel(Level level) {
        if (LOGGER == null) LOGGER = LogManager.getLogger();
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(level);
        ctx.updateLoggers();
    }

    public static void debug(Object msg) {
        if (LOGGER == null) setLevel(Level.DEBUG);
        LOGGER.debug(msg);
        //System.err.println(msg);
    }
}

