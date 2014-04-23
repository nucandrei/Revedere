package org.nuc.revedere.util;

import org.apache.log4j.Logger;

public class LoggerUtil {
    private static final String LINE_SEPARATOR = "--------------------------------------------------------------------------------";


    public static void startLoggingSession(Logger logger, String text) {
        StringBuilder stringBuilder = new StringBuilder("\n");
        stringBuilder.append(LINE_SEPARATOR);
        stringBuilder.append("\n");
        stringBuilder.append(text);
        stringBuilder.append("\n");
        stringBuilder.append(LINE_SEPARATOR);
        logger.info(stringBuilder);
    }
    
    
    private LoggerUtil() {
        // empty constructor. It protects from creating instances of LoggerUtils
    }
}
