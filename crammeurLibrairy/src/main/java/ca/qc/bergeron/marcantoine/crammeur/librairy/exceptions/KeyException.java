/*
 * Copyright (c) 2016.
 */

package ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions;

/**
 * Created by Marc-Antoine on 2016-03-29.
 */
public final class KeyException extends Exception {
    public KeyException() {
        super();
    }

    public KeyException(String message) {
        super(message);
    }

    public KeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyException(Throwable cause) {
        super(cause);
    }
}
