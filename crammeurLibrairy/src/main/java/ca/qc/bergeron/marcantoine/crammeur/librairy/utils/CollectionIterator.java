package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public abstract class CollectionIterator<E, S extends Serializable> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E,S> {

    @Override
    public final int currentCollectionSize() {
        return this.currentCollection().size();
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
                boolean follow = true;

                @Override
                public void perform(E pParameter) {
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
    public final <E2 extends E> boolean containsAll(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E2, S> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = (this.isEmpty() && pCollectionIterator.isEmpty());
        for (Collection<E2> collection : pCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E2>() {
                boolean follow = true;
                @Override
                public void perform(E2 pParameter) {
                    synchronized (result) {
                        result[0] = CollectionIterator.this.contains(pParameter);
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
    public boolean equals(@NotNull final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E, S> pCollectionIterator) {
        //Save time
        if (this.equals((Object) pCollectionIterator)) return true;
        final boolean[] result = new boolean[1];
        if ((result[0] = this.size().equals(pCollectionIterator.size())) && !this.isEmpty()) {
            for (Collection<E> collection : this.allCollections()) {
                Parallel.For(collection, new Parallel.Operation<E>() {
                    boolean follow = true;
                    @Override
                    public void perform(final E pParameter) {
                        if (!CollectionIterator.this.count(pParameter).equals(pCollectionIterator.count(pParameter))) {
                            synchronized (result) {
                                result[0] = false;
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
                if (!result[0]) break;
            }
        }
        return result[0];
    }
}
