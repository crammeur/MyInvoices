package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

/**
 * Created by Marc-Antoine on 2017-12-07.
 */

public interface Iterator<E> extends java.util.Iterator<E> {

    /**
     * Increment index and return the next element
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
    E get() throws IndexOutOfBoundsException;

    boolean hasPrevious();

    /**
     * Decrement index and return the previous element
     *
     * @return The previous element
     * @throws NoSuchElementException
     */
    @Nullable
    E previous() throws NoSuchElementException;

    void add(@Nullable E pEntity);


    void set(@Nullable E pEntity);

}
