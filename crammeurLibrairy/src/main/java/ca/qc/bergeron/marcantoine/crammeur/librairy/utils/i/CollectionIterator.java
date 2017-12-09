package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public interface CollectionIterator<E, S extends Serializable> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Collection<E,S>, Iterator<E> {

    int MAX_COLLECTION_SIZE = Integer.MAX_VALUE;

    @NotNull
    S getIndex();

    void setIndex(@NotNull S pIndex) throws IndexOutOfBoundsException;

    /**
     * Return size of get collection
     *
     * @return Size of get collection
     */
    int collectionSize();

    /**
     * Return size of the specific collection where index is
     *
     * @param pIndex Index
     * @return Size of specific collection where index is
     */
    int collectionSizeOf(@NotNull S pIndex) throws IndexOutOfBoundsException;

    int collectionIndex();

    int nextIndex();

    int previousIndex();

    /**
     * Return index of specific collection where index is
     *
     * @param pIndex Index
     * @return Index of specific collection where index is
     * @throws IndexOutOfBoundsException
     */
    int collectionIndexOf(@NotNull S pIndex) throws IndexOutOfBoundsException;

    boolean hasNextCollection();

    /**
     * Increment index and return the next collection
     *
     * @return The next Collection
     * @throws NoSuchElementException
     */
    @NotNull
    Collection<E> nextCollection() throws NoSuchElementException;


    /**
     * Return the actual collection
     *
     * @return Actual Collection where index is
     * @throws IndexOutOfBoundsException
     */
    Collection<E> getCollection() throws IndexOutOfBoundsException;

    boolean hasPreviousCollection();

    /**
     * Decrement index and return the previous collection
     *
     * @return The previous collection
     * @throws NoSuchElementException
     */
    @NotNull
    Collection<E> previousCollection() throws NoSuchElementException;

    @NotNull
    <T extends Collection<E>> Iterable<T> allCollections();

    /**
     * Return specific collection where index is
     *
     * @param pIndex Index
     * @return Specific collection where index is
     * @throws IndexOutOfBoundsException
     */
    @NotNull
    Collection<E> collectionOf(@NotNull S pIndex) throws IndexOutOfBoundsException;

    @NotNull
    @Override
    Iterator<E> iterator();

    <T extends Collection<E>> Iterator<T> collectionIterator();

}
