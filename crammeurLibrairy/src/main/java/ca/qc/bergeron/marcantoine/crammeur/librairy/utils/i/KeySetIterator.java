package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeySetIterator<K extends Serializable> extends KeyCollectionIterator<K,K> {
    @NotNull
    @Override
    Set<K> currentCollection();

    @NotNull
    @Override
    Set<K> collectionOf(@NotNull K pIndex);
}
