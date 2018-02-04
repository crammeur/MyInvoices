package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public abstract class SetIterator<E, S extends Serializable> extends CollectionIterator<E, S> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.SetIterator<E,S> {

    @Override
    public final <E2 extends E> boolean removeAll(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E2, S> pKeyCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = true;
        for (Collection<E2> collection : pKeyCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {

                @Override
                public void perform(E pParameter) {
                    if (!ca.qc.bergeron.marcantoine.crammeur.librairy.utils.SetIterator.this.remove(pParameter)) {
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
    public final boolean equals(@Nullable final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.SetIterator<E,S> pSetIterator) {
        return pSetIterator != null && (this == pSetIterator || this.containsAll(pSetIterator));
    }
}
