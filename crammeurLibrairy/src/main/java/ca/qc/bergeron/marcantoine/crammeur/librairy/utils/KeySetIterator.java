package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

abstract class KeySetIterator<K extends Serializable> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.KeySetIterator<K> {

    @Override
    public final int currentCollectionSize() {
        return this.currentCollection().size();
    }

    @Override
    public final int collectionSizeOf(@NotNull K pIndex) {
        return this.collectionOf(pIndex).size();
    }
}
