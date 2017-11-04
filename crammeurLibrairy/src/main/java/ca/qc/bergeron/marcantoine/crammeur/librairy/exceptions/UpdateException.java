/*
 * Copyright (c) 2016.
 */

package ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions;

/**
 * Created by Marc-Antoine on 2016-06-12.
 */
public final class UpdateException extends Exception {
    public UpdateException() {
        super();
    }

    public UpdateException(String message) {
        super(message);
    }

    public UpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdateException(Throwable cause) {
        super(cause);
    }
}
