package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

abstract class MapIterator<K extends Serializable,V> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.MapIterator<K,V> {

    @Override
    public boolean isEmpty() {
        return this.size().equals(this.firstValue());
    }

    @Override
    public boolean equals(@NotNull ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.MapIterator<K, V> pMapIterator) {
        return false;
    }
}
