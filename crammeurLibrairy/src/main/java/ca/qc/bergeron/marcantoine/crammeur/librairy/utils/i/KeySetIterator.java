package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeySetIterator<K extends Serializable> extends KeyCollectionIterator<K,K> {

    boolean addToSet(@Nullable K pKey);

    <E extends K> boolean addAllToSet(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) CollectionIterator<E, K> pDataCollectionIterator);

}
