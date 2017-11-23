package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.ListIterator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataMap<K extends Serializable, V extends Data<K>> {

    K size();

    boolean isEmpty();

    boolean containsKey(K pKey);

    boolean containsValue(V pValue);

    V get(K pKey);

    V put(V pValue);

    V remove(K pKey);

    void putAll(@NotNull DataMap<? extends K, ? extends V> pDataMap);

    void clear();

    KeySetIterator<K> keySet();

    DataListIterator<V, K> values();

    EntrySet<Entry<K, V>, V, K> entrySet();

    boolean equals(@NotNull DataMap<K, V> pDataMap);

    interface Entry<K extends Serializable, V extends Data<K>> {

        K getKey();

        V getValue();

        V setValue(V pValue);

    }

    interface EntryCollection<E extends Entry<K, T>, T extends Data<K>, K extends Serializable> extends Iterable<E>, ListIterator<E> {

        int NULL_INDEX = -1;
        int MIN_INDEX = 0;
        int MAX_INDEX = Integer.MAX_VALUE;

        @NotNull
        K size();

        /**
         * Return size of current specific collection
         *
         * @return Size of current specific collection
         */
        int sizeActualCollection();

        /**
         * Return size of the specific collection where key is
         *
         * @param pKey Key
         * @return Size of specific collection where key is
         */
        int sizeCollectionOf(@NotNull K pKey);

        boolean isEmpty();

        /**
         * Return mIndex for actual data in currentCollection method
         *
         * @return Index for currentCollection method
         */
        int actualIndex();

        /**
         * Return the specific collection where mIndex is
         *
         * @return Collection of current mIndex
         */
        @NotNull
        Collection<T> actualCollection();

        /**
         * Return mIndex of specific collection where key is
         *
         * @param pKey Key
         * @return Index of specific collection where key is
         */
        int indexCollectionOf(@NotNull K pKey);

        /**
         * Return specific collection where key is
         *
         * @param pKey Key
         * @return Collection where key is
         */
        @NotNull
        Collection<T> collectionOf(@NotNull K pKey);

        boolean addAll(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) Iterable<? extends E> pIterable);

        boolean addAll(@NotNull K pIndex, @NotNull Iterable<? extends E> pDataIterable);

        boolean contains(@NotNull E pData);

        boolean containsAll(@NotNull Iterable<? extends E> pIterable);

        boolean remove(@NotNull E pData);

        boolean removeAll(@NotNull Iterable<? extends E> pIterable);

        boolean retainAll(@NotNull Iterable<? extends E> pIterable);

        void clear();

    }

    interface EntrySet<E extends Entry<K, T>, T extends Data<K>, K extends Serializable> extends EntryCollection<E, T, K> {
        boolean equals(@NotNull EntrySet<E, T, K> pEntrySet);
    }
}
