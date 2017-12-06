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
    S size();

    /**
     * Return size of actual collection
     *
     * @return Size of actual collection
     */
    int collectionSize();

    /**
     * Return size of the specific collection where mIndex is
     *
     * @param pIndex Index
     * @return Size of specific collection where mIndex is
     */
    int collectionSizeOf(@NotNull S pIndex);

    boolean isEmpty();

    int collectionIndex();

    /**
     * Return mIndex of specific collection where mIndex is
     *
     * @param pIndex Index
     * @return Index of specific collection where mIndex is
     */
    int collectionIndexOf(@NotNull S pIndex);

    boolean hasNextCollection();

    /**
     * Increment index and return the next collection
     *
     * @return the next Collection
     * @throws NoSuchElementException
     */
    @NotNull
    Collection<E> nextCollection() throws NoSuchElementException;

    /**
     * Return the actual collection
     *
     * @return actual Collection where index is
     * @throws IndexOutOfBoundsException
     */
    Collection<E> actualCollection() throws IndexOutOfBoundsException;

    boolean hasPreviousCollection();

    /**
     * Decrement index and return the previous collection
     *
     * @return the previous collection
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
     * @return Specific collection where index is
     */
    @NotNull
    Collection<E> collectionOf(@NotNull S pIndex);

    @NotNull
    S count(@Nullable E pEntity);

    void add(@Nullable E pEntity);

    int nextIndex();

    /**
     * Increment index and return the next element
     *
     * @return the next elements
     * @throws NoSuchElementException
     */
    @Nullable
    E next() throws NoSuchElementException;

    /**
     * Return the actual element
     *
     * @return the actual element
     * @throws IndexOutOfBoundsException
     */
    @Nullable
    E actual() throws IndexOutOfBoundsException;

    boolean hasPrevious();

    int previousIndex();

    /**
     * Decrement index and return the previous element
     *
     * @return the previous element
     * @throws IndexOutOfBoundsException
     */
    @Nullable
    E previous() throws IndexOutOfBoundsException;

    void set(@Nullable E pEntity);

    boolean contains(@Nullable E pEntity);

    <E2 extends E> boolean containsAll(@NotNull CollectionIterator<E2, S> pCollectionIterator);

    boolean equals(@Nullable CollectionIterator<E, S> pCollectionIterator);

    boolean remove(@Nullable E pData);

    <E2 extends E> boolean removeAll(@NotNull CollectionIterator<E2, S> pCollectionIterator);

    <E2 extends E> boolean retainAll(@NotNull CollectionIterator<E2, S> pCollectionIterator);

    void clear();

}
