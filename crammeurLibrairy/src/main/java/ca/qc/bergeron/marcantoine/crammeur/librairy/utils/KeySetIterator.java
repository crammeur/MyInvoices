package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public abstract class KeySetIterator<K extends Serializable> extends CollectionIterator<K,K> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.KeySetIterator<K> {

    @Override
    public <E extends K> boolean removeAll(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<E, K> pKeyCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = true;
        for (Collection<E> collection : pKeyCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {

                @Override
                public void perform(E pParameter) {
                    if (!KeySetIterator.this.remove(pParameter)) {
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
    public final boolean equals(@Nullable final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.KeySetIterator<K> pKeySetIterator) {
        return pKeySetIterator != null && (this.equals((Object) pKeySetIterator) || this.containsAll(pKeySetIterator));
    }
}
