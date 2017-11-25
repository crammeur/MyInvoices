package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public abstract class KeySetIterator<K extends Serializable> extends CollectionIterator<K,K> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.KeySetIterator<K> {

    @Override
    public boolean equals(@Nullable final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator<K, K> pCollectionIterator) {
        if (pCollectionIterator == null) return false;
        //Save time
        if (this.equals((Object) pCollectionIterator)) return true;
        final boolean[] result = new boolean[1];
        if ((result[0] = this.size().equals(pCollectionIterator.size())) && !this.isEmpty()) {
            for (Collection<K> collection : this.allCollections()) {
                Parallel.For(collection, new Parallel.Operation<K>() {
                    boolean follow = true;
                    @Override
                    public void perform(final K pParameter) {
                        if (!pCollectionIterator.contains(pParameter)) {
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
