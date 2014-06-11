package org.nuc.revedere.util;

import org.apache.log4j.Logger;

public class LoggerUtil {
    private static final String LINE_SEPARATOR = "--------------------------------------------------------------------------------";
    private static final Logger LOGGER = Logger.getLogger(LoggerUtil.class);

    public static void startLoggingSession(String text) {
        StringBuilder stringBuilder = new StringBuilder("\n");
        stringBuilder.append(LINE_SEPARATOR);
        stringBuilder.append("\n");
        stringBuilder.append(text);
        stringBuilder.append("\n");
        stringBuilder.append(LINE_SEPARATOR);
        LOGGER.info(stringBuilder);
    }

    private LoggerUtil() {
        // empty constructor. It protects from creating instances of LoggerUtils
    }
}
