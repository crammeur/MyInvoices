package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;

/**
 * Created by Marc-Antoine on 2017-09-22.
 */

public abstract class ListIterator<E, S extends Serializable> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.CollectionIterator<E, S> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator<E, S> {

    public final <E2 extends E> boolean addAllAtEnd(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E2, S> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            boolean follow = true;
            @Override
            public void perform(E pParameter) {
                synchronized (ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator.this) {
                    synchronized (result) {
                        result[0] = ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator.this.addAtEnd(pParameter);
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
        };
        for (Collection<E2> collection : pCollectionIterator.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result[0];
    }

    @Override
    public final <E2 extends E> boolean removeAll(@NotNull CollectionIterator<E2, S> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            boolean follow = true;
            @Override
            public void perform(E pParameter) {
                synchronized (ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator.this) {
                    result[0] = ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator.this.remove(pParameter);
                }
                synchronized (this) {
                    follow = result[0];
                }
            }

            @Override
            public boolean follow() {
                return follow;
            }
        };
        for (Collection<E2> collection : pCollectionIterator.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result[0];
    }

    @Override
    public final boolean equals(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator<E, S> pListIterator) {
        if (pListIterator == null) return false;
        //Save time
        if (this.equals((java.lang.Object) pListIterator)) return true;
        final boolean[] result = new boolean[1];
        if ((result[0] = this.size().equals(pListIterator.size())) && !this.isEmpty()) {
            final Iterator<Collection<E>> collections = pListIterator.allCollections().iterator();
            for (Collection<E> list : this.allCollections()) {
                final Iterator<E> iterator = collections.next().iterator();
                Parallel.For(list, new Parallel.Operation<E>() {
                    @Override
                    public void perform(E pParameter) {
                        synchronized (result) {
                                result[0] = pParameter.equals(iterator.next());
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
