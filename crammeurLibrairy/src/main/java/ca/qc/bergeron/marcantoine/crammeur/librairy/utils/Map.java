package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Set;

import ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.SetIterator;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public abstract class Map<K extends Serializable,V> extends Object implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map<K,V> {

    @Override
    public final <K2 extends K, V2 extends V> void putAll(@NotNull final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map<K2, V2> pMap) {
        for (Set<K2> collection : pMap.keySet().<SetIterator.PartialSet<K2>>allCollections()) {
            Parallel.For(collection, new Parallel.Operation<K2>() {
                @Override
                public void perform(K2 pParameter) {
                    Map.this.put(pParameter, pMap.get(pParameter));
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }
    }

    @Override
    public final boolean equals(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map<K, V> pMap) {
        return pMap != null && (this.keySet() == pMap.keySet() || this.keySet().equals(pMap.keySet())) && (this.values() == pMap.values() || this.values().equals(pMap.values()));
    }
}
