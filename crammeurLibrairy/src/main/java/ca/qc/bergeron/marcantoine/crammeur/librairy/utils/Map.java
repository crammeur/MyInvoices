package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

abstract class Map<K extends Serializable,V> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map<K,V> {

    @Override
    public final boolean equals(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map<K, V> pMap) {
        return pMap != null && this.keySet().equals(pMap.keySet()) && this.values().equals(pMap.values());
    }
}
