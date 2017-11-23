package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeySetIterator<K extends Serializable> extends KeyCollectionIterator<K> {
    boolean equals(KeySetIterator<K> o);
}
