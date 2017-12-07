package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public interface CollectionIterator<E, S extends Serializable> extends Iterator<E> {

    int NULL_INDEX = -1;
    int MIN_INDEX = 0;
    int MAX_COLLECTION_SIZE = Integer.MAX_VALUE;

    @NotNull
    S getIndex();

    void setIndex(@NotNull S pIndex);

    S size();

    /**
     * Return size of actual collection
     *
     * @return Size of actual collection
     */
    int collectionSize();

    /**
     * Return size of the specific collection where pIndex is
     *
     * @param pIndex Index
     * @return Size of specific collection where getIndex is
     */
    int collectionSizeOf(@NotNull S pIndex);

    boolean isEmpty();

    int collectionIndex();

    /**
     * Return getIndex of specific collection where pIndex is
     *
     * @param pIndex Index
     * @return Index of specific collection where pIndex is
     */
    int collectionIndexOf(@NotNull S pIndex);

    boolean hasNextCollection();

    /**
     * Increment getIndex and return the next collection
     *
     * @return The next Collection
     * @throws NoSuchElementException
     */
    @NotNull
    Collection<E> nextCollection() throws NoSuchElementException;


    /**
     * Return the actual collection
     *
     * @return Actual Collection where getIndex is
     * @throws IndexOutOfBoundsException
     */
    Collection<E> actualCollection() throws IndexOutOfBoundsException;

    boolean hasPreviousCollection();

    /**
     * Decrement getIndex and return the previous collection
     *
     * @return The previous collection
     * @throws NoSuchElementException
     */
    @NotNull
    Collection<E> previousCollection() throws NoSuchElementException;

    @NotNull
    <T extends Collection<E>> Iterable<T> allCollections();

    /**
     * Return specific collection where pIndex is
     *
     * @param pIndex Index
     * @return Specific collection where getIndex is
     */
    @NotNull
    Collection<E> collectionOf(@NotNull S pIndex);

    @NotNull
    S count(@Nullable E pEntity);

    void add(@Nullable E pEntity);

    int nextIndex();

    /**
     * Increment getIndex and return the next element
     *
     * @return The next elements
     * @throws NoSuchElementException
     */
    @Nullable
    E next() throws NoSuchElementException;

    /**
     * Return the actual element
     *
     * @return The actual element
     * @throws IndexOutOfBoundsException
     */
    @Nullable
    E actual() throws IndexOutOfBoundsException;

    boolean hasPrevious();

    int previousIndex();

    /**
     * Decrement getIndex and return the previous element
     *
     * @return The previous element
     * @throws NoSuchElementException
     */
    @Nullable
    E previous() throws NoSuchElementException;

    void set(@Nullable E pEntity);

    boolean contains(@Nullable E pEntity);

    <E2 extends E> boolean containsAll(@NotNull CollectionIterator<E2, S> pCollectionIterator);

    boolean equals(@Nullable CollectionIterator<E, S> pCollectionIterator);

    boolean remove(@Nullable E pData);

    <E2 extends E> boolean removeAll(@NotNull CollectionIterator<E2, S> pCollectionIterator);

    <E2 extends E> boolean retainAll(@NotNull CollectionIterator<E2, S> pCollectionIterator);

    void clear();

}
