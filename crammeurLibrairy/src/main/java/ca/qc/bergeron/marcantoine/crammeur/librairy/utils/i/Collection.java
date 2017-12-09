package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-12-08.
 */

public interface Collection<E, S extends Serializable> extends Iterable<E> {

    int NULL_INDEX = -1;
    int MIN_INDEX = 0;

    S size();

    boolean isEmpty();

    void add(@Nullable E pEntity);

    @NotNull
    S count(@Nullable E pEntity);

    boolean contains(@Nullable E pEntity);

    <E2 extends E> boolean containsAll(@NotNull CollectionIterator<E2, S> pCollectionIterator);

    boolean equals(@Nullable CollectionIterator<E, S> pCollectionIterator);

    boolean remove(@Nullable E pData);

    <E2 extends E> boolean removeAll(@NotNull CollectionIterator<E2, S> pCollectionIterator);

    <E2 extends E> boolean retainAll(@NotNull CollectionIterator<E2, S> pCollectionIterator);

    void clear();
}
