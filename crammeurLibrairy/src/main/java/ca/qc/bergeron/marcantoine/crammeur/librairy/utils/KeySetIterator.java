package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public abstract class KeySetIterator<K extends Serializable> extends CollectionIterator<K,K> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.KeySetIterator<K> {

    public boolean equals(@Nullable final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.KeySetIterator<K> pKeySetIterator) {
        return pKeySetIterator != null && (this.equals((Object) pKeySetIterator) || this.containsAll(pKeySetIterator));
    }
}
