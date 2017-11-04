package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-22.
 */

abstract class DataListIterator<T extends Data<K>, K extends Serializable> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator<T, K> {

    transient final float PERCENT_MEMORY_MAX = 0.7f;

    @Override
    public final int currentCollectionSize() {
        return this.currentCollection().size();
    }

    @Override
    public final int collectionSizeOf(@NotNull K pKey) {
        return this.collectionOf(pKey).size();
    }

    @Override
    public final boolean containsAll(@NotNull Iterable<? extends T> collection) {
        boolean result = true;
        for (final T data : collection) {
            if (!this.contains(data)) result = false;
            if (!result) break;
        }
        System.gc();
        return result;
    }

    @Override
    public final boolean addAll(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) Iterable<? extends T> collection) {
        boolean result = true;
        for (final T data : collection) {
            this.add(data);
            if (!this.contains(data)) result = false;
            if (!result) break;
        }
        System.gc();
        return result;
    }

    @Override
    public final boolean removeAll(@NotNull Iterable<? extends T> collection) {
        boolean result = true;
        for (final T data : collection) {
            if (!this.contains(data) || (this.contains(data) && !this.remove(data))) result = false;
            if (!result) break;
        }
        System.gc();
        return result;
    }

}
