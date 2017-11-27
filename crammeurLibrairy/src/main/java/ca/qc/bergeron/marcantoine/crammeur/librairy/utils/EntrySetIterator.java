package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;

import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map;

/**
 * Created by Marc-Antoine on 2017-11-27.
 */

abstract class EntrySetIterator<T, S extends Serializable> extends EntryCollectionIterator<T,S> implements Map.EntrySetIterator<T,S> {

    @Override
    public <E2 extends T> boolean removeAll(@NotNull Map.EntryCollectionIterator<E2, S> pEntryCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = true;
        for (Collection<Map.Entry<S,E2>> collection : pEntryCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<Map.Entry<S, E2>>() {

                @Override
                public void perform(final Map.Entry<S, E2> pParameter) {
                    if (!EntrySetIterator.this.remove(new Map.Entry<S, T>() {
                        @Override
                        public S getKey() {
                            return pParameter.getKey();
                        }

                        @Override
                        public T getValue() {
                            return pParameter.getValue();
                        }

                        @Deprecated
                        @Override
                        public T setValue(T pValue) {
                            throw new UnsupportedOperationException();
                        }
                    })) {
                        synchronized (result) {
                            result[0] = false;
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return result[0];
                }
            });
        }
        return result[0];
    }

    @Override
    public boolean equals(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map.EntrySetIterator<T, S> pEntrySetIterator) {
        return pEntrySetIterator != null && this.containsAll(pEntrySetIterator);
    }
}
