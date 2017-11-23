package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.ListIterator;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeyCollectionIterator<K extends Serializable> extends Iterable<K>, ListIterator<K> {

    int NULL_INDEX = -1;
    int MIN_INDEX = 0;
    int MAX_COLLECTION_INDEX = Integer.MAX_VALUE;

    @NotNull
    K size();

    /**
     * Return size of current specific collection
     *
     * @return Size of current specific collection
     */
    int currentCollectionSize();

    /**
     * Return size of the specific collection where mIndex is
     *
     * @param pIndex Index
     * @return Size of specific collection where mIndex is
     */
    int collectionSizeOf(@NotNull K pIndex);

    boolean isEmpty();

    /**
     * Return mIndex for current key in currentCollection method
     *
     * @return Index for currentCollection method
     */
    int currentCollectionIndex();

    /**
     * Return mIndex of specific collection where mIndex is
     *
     * @param pIndex Index
     * @return Index of specific collection where mIndex is
     */
    int collectionIndexOf(@NotNull K pIndex);

    /**
     * Return the specific collection where mIndex is
     *
     * @return Collection of current mIndex
     */
    @NotNull
    Collection<K> currentCollection();

    @NotNull
    Iterable<Collection<K>> allCollections();

    /**
     * Return specific collection where mIndex is
     *
     * @param pIndex Index
     * @return Collection where mIndex is
     */
    @NotNull
    Collection<K> collectionOf(@NotNull K pIndex);

    @Override
    void add(@Nullable K pKey);

    boolean addAtEnd(@Nullable K pKey);

    <E extends K> boolean addAllAtEnd(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) KeyCollectionIterator<E> pKeyCollectionIterator);

    @Nullable
    @Override
    K next();

    @Nullable
    @Override
    K previous();

    boolean contains(@Nullable K pKey);

    <E extends K> boolean containsAll(@NotNull KeyCollectionIterator<E> pKeyCollectionIterator);

    boolean equals(@NotNull KeyCollectionIterator<K> pKeyCollectionIterator);

    @Override
    void set(@Nullable K pKey);

    boolean remove(@Nullable K pKey);

    <E extends K> boolean removeAll(@NotNull KeyCollectionIterator<E> pKeyCollectionIterator);

    <E extends K> boolean retainAll(@NotNull KeyCollectionIterator<E> pKeyCollectionIterator);

    void clear();
}
