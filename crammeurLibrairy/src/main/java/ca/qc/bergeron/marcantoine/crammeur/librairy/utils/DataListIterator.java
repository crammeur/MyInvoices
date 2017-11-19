package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

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
    public final <E extends T> boolean containsAll(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator<E, K> pDataListIterator) {
        final boolean[] result = new boolean[1];
        result[0] = (this.isEmpty() && pDataListIterator.isEmpty());
        for (Collection<E> collection : pDataListIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E, Void>() {
                boolean follow = true;
                @Override
                public Void perform(E pParameter) {
                    synchronized (result) {
                        result[0] = DataListIterator.this.contains(pParameter);
                    }
                    synchronized (this) {
                        follow = result[0];
                    }
                    return null;
                }

                @Override
                public boolean follow() {
                    return follow;
                }

                @Override
                public boolean result() {
                    return false;
                }

                @Override
                public boolean async() {
                    return true;
                }
            });
        }
        return result[0];
    }

    @Override
    public final <E extends T> void addAll(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator<E, K> pDataListIterator) {
        for (Collection<E> collection : pDataListIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E, Void>() {
                @Override
                public Void perform(E pParameter) {
                    synchronized (DataListIterator.this) {
                        DataListIterator.this.add(pParameter);
                    }
                    return null;
                }

                @Override
                public boolean follow() {
                    return true;
                }

                @Override
                public boolean result() {
                    return false;
                }

                @Override
                public boolean async() {
                    return false;
                }
            });
        }
    }

    @Override
    public final <E extends T> void removeAll(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator<E, K> pDataListIterator) {
        for (Collection<E> collection : pDataListIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E, Void>() {
                @Override
                public Void perform(E pParameter) {
                    DataListIterator.this.remove(pParameter);
                    return null;
                }

                @Override
                public boolean follow() {
                    return true;
                }

                @Override
                public boolean result() {
                    return false;
                }

                @Override
                public boolean async() {
                    return true;
                }
            });
        }
    }

}
