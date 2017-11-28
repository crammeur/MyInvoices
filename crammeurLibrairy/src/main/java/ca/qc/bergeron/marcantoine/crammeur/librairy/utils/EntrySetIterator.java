package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map;

/**
 * Created by Marc-Antoine on 2017-11-27.
 */

abstract class EntrySetIterator<T, S extends Serializable> extends EntryCollectionIterator<T,S> implements Map.EntrySetIterator<T,S> {

    @Override
    public final boolean equals(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map.EntrySetIterator<T, S> pEntrySetIterator) {
        return pEntrySetIterator != null && this.containsAll(pEntrySetIterator);
    }
}
