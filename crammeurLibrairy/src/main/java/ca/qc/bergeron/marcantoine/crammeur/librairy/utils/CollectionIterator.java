package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public abstract class CollectionIterator<E, S extends Serializable> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E,S>,Iterable<E> {

    @NotNull
    @Override
    public final Iterator<E> iterator() {
        return ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object.cloneObject(this);
    }

    @Override
    public final int collectionSizeOf(@NotNull S pIndex) {
        return this.collectionOf(pIndex).size();
    }

    @Override
    public final boolean contains(@Nullable final E pData) {
        final boolean[] result = new boolean[1];
        for (Collection<E> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {
                @Override
                public void perform(E pParameter) {
                    if ((pData == null && pParameter == null) || (pData != null && pData.equals(pParameter))) {
                        synchronized (result) {
                            result[0] = true;
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return !result[0];
                }
            });
        }

        return result[0];
    }

    @Override
    public final <E2 extends E> boolean containsAll(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E2, S> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = !pCollectionIterator.isEmpty();
        for (Collection<E2> collection : pCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E2>() {
                @Override
                public void perform(E2 pParameter) {
                    synchronized (result) {
                        result[0] = CollectionIterator.this.contains(pParameter);
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
    public final boolean equals(@Nullable final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E, S> pCollectionIterator) {
        if (pCollectionIterator == null) return false;
        //Save time
        if (this.equals((Object) pCollectionIterator)) return true;
        final boolean[] result = new boolean[1];
        if ((result[0] = this.size().equals(pCollectionIterator.size())) && !this.isEmpty()) {
            for (Collection<E> collection : this.allCollections()) {
                Parallel.For(collection, new Parallel.Operation<E>() {
                    @Override
                    public void perform(final E pParameter) {
                        if (!CollectionIterator.this.count(pParameter).equals(pCollectionIterator.count(pParameter))) {
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
                if (!result[0]) break;
            }
        }
        return result[0];
    }
}
