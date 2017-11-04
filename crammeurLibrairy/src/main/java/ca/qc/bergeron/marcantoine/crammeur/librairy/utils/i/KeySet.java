package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeySet<K extends Serializable> extends KeyCollection<K> {
    boolean equals(KeySet<K> o);
}
