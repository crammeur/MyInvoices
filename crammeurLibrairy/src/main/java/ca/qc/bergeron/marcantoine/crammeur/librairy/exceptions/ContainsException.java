package ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions;

/**
 * Created by Marc-Antoine on 2017-11-25.
 */

public final class ContainsException extends RuntimeException {
    public ContainsException() {
        super();
    }

    public ContainsException(String message) {
        super(message);
    }
}
