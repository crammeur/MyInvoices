package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;

/**
 * Created by Marc-Antoine on 2017-09-22.
 */

abstract class DataListIterator<T extends Data<K>, K extends Serializable> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.CollectionIterator<T,K> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator<T, K>{

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
}
