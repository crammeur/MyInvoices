package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataCollectionIterator;

/**
 * Created by Marc-Antoine on 2017-09-22.
 */

abstract class DataListIterator<T extends Data<K>, K extends Serializable> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator<T, K> {

    @Override
    public final int currentCollectionSize() {
        return this.currentCollection().size();
    }

    @Override
    public final int collectionSizeOf(@NotNull K pKey) {
        return this.collectionOf(pKey).size();
    }

    @Override
    public final boolean contains(@Nullable final T pData) {
        final boolean[] result = new boolean[1];

        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<T>() {
                boolean follow = true;

                @Override
                public void perform(T pParameter) {
                    if ((pData == null && pParameter == null) || (pData != null && pData.equals(pParameter))) {
                        synchronized (result) {
                            result[0] = true;
                        }
                        synchronized (this) {
                            follow = false;
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return follow;
                }
            });
        }

        return result[0];
    }

    @Override
    public final <E extends T> boolean containsAll(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator<E, K> pDataListIterator) {
        final boolean[] result = new boolean[1];
        result[0] = (this.isEmpty() && pDataListIterator.isEmpty());
        for (Collection<E> collection : pDataListIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {
                boolean follow = true;
                @Override
                public void perform(E pParameter) {
                    synchronized (result) {
                        result[0] = DataListIterator.this.contains(pParameter);
                    }
                    synchronized (this) {
                        follow = result[0];
                    }
                }

                @Override
                public boolean follow() {
                    return follow;
                }
            });
        }
        return result[0];
    }

    @Override
    public final <E extends T> boolean addAllAtEnd(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) DataCollectionIterator<E, K> pDataCollectionIterator) {
        final boolean[] result = new boolean[1];
        for (Collection<E> collection : pDataCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {
                boolean follow = true;
                @Override
                public void perform(E pParameter) {
                    synchronized (DataListIterator.this) {
                        synchronized (result) {
                            result[0] = DataListIterator.this.addAtEnd(pParameter);
                        }
                    }
                    synchronized (this) {
                        follow = result[0];
                    }
                }

                @Override
                public boolean follow() {
                    return follow;
                }
            });
        }
        return result[0];
    }

    @Override
    public final <E extends T> boolean removeAll(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator<E, K> pDataListIterator) {
        final boolean[] result = new boolean[1];
        for (Collection<E> collection : pDataListIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {
                boolean follow = true;
                @Override
                public void perform(E pParameter) {
                    synchronized (DataListIterator.this) {
                        result[0] = DataListIterator.this.remove(pParameter);
                    }
                    synchronized (this) {
                        follow = result[0];
                    }
                }

                @Override
                public boolean follow() {
                    return follow;
                }
            });
        }
        return result[0];
    }

    @Override
    public final boolean equals(@NotNull DataCollectionIterator<T, K> pDataCollectionIterator) {
        //Save time
        if (this.equals((Object) pDataCollectionIterator)) return true;
        final boolean[] result = new boolean[1];
        if ((result[0] = this.size().equals(pDataCollectionIterator.size())) && !this.isEmpty()) {
            final Iterator<Collection<T>> collections = pDataCollectionIterator.allCollections().iterator();
            for (Collection<T> collection : this.allCollections()) {
                final Iterator<T> iterator = collections.next().iterator();
                Parallel.For(collection, new Parallel.Operation<T>() {
                    boolean follow = true;
                    @Override
                    public void perform(T pParameter) {
                        synchronized (result) {
                            result[0] = pParameter.equals(iterator.next());
                        }
                        synchronized (this) {
                            this.follow = !result[0];
                        }
                    }

                    @Override
                    public boolean follow() {
                        return follow;
                    }
                });
                if (!result[0]) break;
            }
        }
        return result[0];
    }
}
