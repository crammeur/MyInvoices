package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public interface CollectionIterator<E, S extends Serializable> extends Iterable<E>, Iterator<E>, SerializableValueOf<S> {

    int NULL_INDEX = -1;
    int MIN_INDEX = 0;
    int MAX_COLLECTION_INDEX = Integer.MAX_VALUE;

    @NotNull
    S size();

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
    int collectionSizeOf(@NotNull S pIndex);

    boolean isEmpty();

    /**
     * Return mIndex for current data in currentCollection method
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
    int collectionIndexOf(@NotNull S pIndex);

    /**
     * Return the specific collection where mIndex is
     *
     * @return Collection of current mIndex
     */
    @NotNull
    Collection<E> currentCollection();

    @NotNull
    Iterable<Collection<E>> allCollections();

    /**
     * Return specific collection where mIndex is
     *
     * @param pIndex Index
     * @return Collection where mIndex is
     */
    @NotNull
    Collection<E> collectionOf(@NotNull S pIndex);

    void add(@Nullable E pEntity);

    int nextIndex();

    @Nullable
    E next();

    boolean hasPrevious();

    int previousIndex();

    @Nullable
    E previous();

    void set(@Nullable E pEntity);

    boolean contains(@Nullable E pEntity);

    <E2 extends E> boolean containsAll(@NotNull CollectionIterator<E2, S> pCollectionIterator);

    boolean equals(@NotNull CollectionIterator<E, S> pCollectionIterator);
}
