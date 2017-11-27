package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map;

/**
 * Created by Marc-Antoine on 2017-11-27.
 */

abstract class EntryCollectionIterator<T, S extends Serializable> extends CollectionIterator<Map.Entry<S,T>,S> implements Map.EntryCollectionIterator<T,S> {

    @Override
    public final boolean equals(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map.EntryCollectionIterator<T,S> pEntryCollectionIterator) {
        return super.equals(pEntryCollectionIterator);
    }
}
