package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public interface Map<K extends Serializable, V> {
    K size();

    boolean isEmpty();

    boolean containsKey(K pKey);

    boolean containsValue(V pValue);

    V get(K pKey);

    V put(K pKey, V pValue);

    V remove(K pKey);

    void putAll(@NotNull Map<? extends K, ? extends V> pMap);

    void clear();

    KeySetIterator<K> keySet();

    CollectionIterator<V, K> values();

    EntrySetIterator<Entry<K, V>, V, K> entrySet();

    boolean equals(@Nullable Map<K, V> pMap);

    interface Entry<K extends Serializable, V> {

        K getKey();

        V getValue();

        V setValue(V pValue);

    }

    interface EntryCollectionIterator<E extends Entry<K, T>, T, K extends Serializable> extends Iterable<E>,Iterator<E> {

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
         * Return mIndex for current entry in currentCollection method
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
        Collection<E> collectionOf(@NotNull K pIndex);

        void add(@Nullable E pEntry);

        boolean addAtEnd(@Nullable E pEntry);

        <E2 extends T> boolean addAllAtEnd(@NotNull EntryCollectionIterator<Entry<K,E2>, E2, K> pEntryCollectionIterator);

        int nextIndex();

        @Nullable
        @Override
        E next();

        boolean hasPrevious();

        int previousIndex();

        @Nullable
        E previous();

        boolean contains(@Nullable E pEntry);

        <E2 extends T> boolean containsAll(@NotNull EntryCollectionIterator<Entry<K,E2>, E2, K> pEntryCollectionIterator);

        boolean equals(@NotNull EntryCollectionIterator<E, T, K> pEntryCollectionIterator);

        void set(@Nullable E pEntry);

        boolean remove(@Nullable E pEntry);

        <E2 extends T> boolean removeAll(@NotNull EntryCollectionIterator<Entry<K,E2>, E2, K> pEntryCollectionIterator);

        <E2 extends T> boolean retainAll(@NotNull EntryCollectionIterator<Entry<K,E2>, E2, K> pEntryCollectionIterator);

        void clear();

    }

    interface EntrySetIterator<E extends Entry<K, T>, T, K extends Serializable> extends EntryCollectionIterator<E, T, K> {
        boolean equals(@NotNull EntrySetIterator<E, T, K> pEntrySetIterator);
    }
}
