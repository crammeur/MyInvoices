/*
 * Copyright (c) 2016.
 */

package ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions;

/**
 * Created by Marc-Antoine on 2016-06-12.
 */
public final class DeleteException extends Exception {
    public DeleteException() {
        super();
    }

    public DeleteException(String message) {
        super(message);
    }

    public DeleteException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeleteException(Throwable cause) {
        super(cause);
    }
}
