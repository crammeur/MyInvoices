package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

abstract class CollectionIterator<E, S extends Serializable> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E,S> {

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
    public final <E2 extends E> boolean addAllToCollection(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E2, S> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        for (Collection<E2> collection : pCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E2>() {
                boolean follow = true;
                @Override
                public void perform(E2 pParameter) {
                    synchronized (CollectionIterator.this) {
                        synchronized (result) {
                            result[0] = CollectionIterator.this.addToCollection(pParameter);
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
    public final boolean equals(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E, S> pCollectionIterator) {
        //Save time
        if (this.equals((Object) pCollectionIterator)) return true;
        final boolean[] result = new boolean[1];
        if ((result[0] = this.size().equals(pCollectionIterator.size())) && !this.isEmpty()) {
            final Iterator<Collection<E>> collections = pCollectionIterator.allCollections().iterator();
            for (Collection<E> collection : this.allCollections()) {
                final Iterator<E> iterator = collections.next().iterator();
                Parallel.For(collection, new Parallel.Operation<E>() {
                    boolean follow = true;
                    @Override
                    public void perform(E pParameter) {
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
