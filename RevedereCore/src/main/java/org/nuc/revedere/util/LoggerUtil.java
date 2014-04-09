package org.nuc.revedere.util;

import org.apache.log4j.Logger;

public class LoggerUtil {
    public static void startLoggingSession(Logger logger, String text) {
        StringBuilder stringBuilder = new StringBuilder("\n");
        stringBuilder.append("--------------------------------------------------------------------------------");
        stringBuilder.append("\n");
        stringBuilder.append(text);
        stringBuilder.append("\n");
        stringBuilder.append("--------------------------------------------------------------------------------");
        logger.info(stringBuilder);
    }
}
