package org.nuc.revedere.service.core.hb;

public enum ServiceState {
    // Service is starting
    STARTING,

    // Service runs normally
    NO_ERROR,

    // Service encountered a warning
    WARNING,

    // Service encountered an error
    ERROR,

    // Service encountered a fatal error.
    FATAL;
}
