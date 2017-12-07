package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface ListIterator<E, S extends Serializable> extends CollectionIterator<E, S>, java.util.ListIterator<E> {

    E get(@NotNull S pIndex);

    E set(@NotNull S pIndex, @Nullable E pData);

    boolean addAtEnd(@Nullable E pData);

    void add(@NotNull S pIndex, @Nullable E pData);

    <E2 extends E> boolean addAllAtEnd(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) CollectionIterator<E2, S> pCollectionIterator);

    <E2 extends E> void addAll(@NotNull S pIndex, @NotNull CollectionIterator<E2, S> pCollectionIterator);

    E remove(@NotNull S pIndex);

    @NotNull
    S indexOf(@Nullable E pData);

    @NotNull
    S lastIndexOf(@Nullable E pData);

    @NotNull
    @Override
    List<E> nextCollection();

    @NotNull
    @Override
    List<E> collectionOf(@NotNull S pIndex);

    @NotNull
    ListIterator<E, S> listIterator();

    @NotNull
    ListIterator<E, S> listIterator(@NotNull S pIndex);

    @NotNull
    ListIterator<E, S> subListIterator(@NotNull S pIndex1, @NotNull S pIndex2);

    boolean equals(@Nullable ListIterator<E, S> pListIterator);
}
