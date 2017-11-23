package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeyCollectionIterator<K extends Serializable, S extends Serializable> extends Iterable<K>, CollectionIterator<K,S> {

    boolean remove(@Nullable K pKey);

    <E extends K> boolean removeAll(@NotNull CollectionIterator<E,S> pKeyCollectionIterator);

    <E extends K> boolean retainAll(@NotNull CollectionIterator<E,S> pKeyCollectionIterator);

    void clear();
}
