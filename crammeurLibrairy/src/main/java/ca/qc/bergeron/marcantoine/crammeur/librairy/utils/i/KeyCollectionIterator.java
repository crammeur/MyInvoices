package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeyCollectionIterator<K extends Serializable> extends Iterable<K>, CollectionIterator<K,K> {

    <E extends K> boolean containsAll(@NotNull KeyCollectionIterator<E> pKeyCollectionIterator);

    boolean equals(@NotNull KeyCollectionIterator<K> pKeyCollectionIterator);

    boolean remove(@Nullable K pKey);

    <E extends K> boolean removeAll(@NotNull KeyCollectionIterator<E> pKeyCollectionIterator);

    <E extends K> boolean retainAll(@NotNull KeyCollectionIterator<E> pKeyCollectionIterator);

    void clear();
}
