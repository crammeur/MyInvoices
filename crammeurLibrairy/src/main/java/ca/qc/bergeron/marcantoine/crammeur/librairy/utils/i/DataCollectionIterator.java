package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.ListIterator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataCollectionIterator<T extends Data<K>, K extends Serializable> extends Iterable<T>, ListIterator<T> {

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

    @NotNull
    K indexOfKey(@Nullable K pKey);

    @NotNull
    K lastIndexOfKey(@Nullable K pKey);

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
    int collectionIndexOf(@NotNull K pIndex);

    /**
     * Return the specific collection where mIndex is
     *
     * @return Collection of current mIndex
     */
    @NotNull
    Collection<T> currentCollection();

    @NotNull
    Iterable<Collection<T>> allCollections();

    /**
     * Return specific collection where mIndex is
     *
     * @param pIndex Index
     * @return Collection where mIndex is
     */
    @NotNull
    Collection<T> collectionOf(@NotNull K pIndex);

    <E extends T> void addAll(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) DataListIterator<E, K> pDataListIterator);

    <E extends T> void addAll(@NotNull K pIndex, @NotNull DataListIterator<E, K> pDataListIterator);

    boolean contains(@NotNull T pData);

    <E extends T> boolean containsAll(@NotNull DataListIterator<E, K> pDataListIterator);

    boolean remove(@NotNull T pData);

    <E extends T> void removeAll(@NotNull DataListIterator<E, K> pDataListIterator);

    <E extends T> void retainAll(@NotNull DataListIterator<E, K> pDataListIterator);

    void clear();

}
