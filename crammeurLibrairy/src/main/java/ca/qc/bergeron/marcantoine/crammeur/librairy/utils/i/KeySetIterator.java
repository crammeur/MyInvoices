package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-12-03.
 */

public interface KeySetIterator<K extends Serializable> extends SetIterator<K,K> {

    boolean equals(ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.KeySetIterator<K> pKeySetIterator);
}
