package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;

/**
 * Created by Marc-Antoine on 2017-09-22.
 */

abstract class DataListIterator<T extends Data<K>, K extends Serializable> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.CollectionIterator<T,K> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator<T,K>{

    public <E extends T> boolean addAllAtEnd(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E, K> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        for (Collection<E> collection : pCollectionIterator.allCollections()) {
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
    public final <E extends T> boolean removeAll(@NotNull CollectionIterator<E, K> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        for (Collection<E> collection : pCollectionIterator.allCollections()) {
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
    public final boolean equals(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<T, K> pCollectionIterator) {
        //Save time
        if (this.equals((Object) pCollectionIterator)) return true;
        final boolean[] result = new boolean[1];
        if ((result[0] = this.size().equals(pCollectionIterator.size())) && !this.isEmpty()) {
            final Iterator<Collection<T>> collections = pCollectionIterator.allCollections().iterator();
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
