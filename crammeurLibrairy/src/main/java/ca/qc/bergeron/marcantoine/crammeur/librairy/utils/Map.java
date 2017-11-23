package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

abstract class Map<K extends Serializable,V> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map<K,V> {

    @Override
    public boolean equals(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map<K, V> pMap) {
        return false;
    }
}
