package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

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

    <K2 extends K, V2 extends V> void putAll(@NotNull Map<K2, V2> pMap);

    void clear();

    SetIterator<K,K> keySet();

    CollectionIterator<V, K> values();

    EntrySetIterator<V, K> entrySet();

    boolean equals(@Nullable Map<K, V> pMap);

    interface Entry<K extends Serializable, V> {

        K getKey();

        V getValue();

        V setValue(V pValue);

    }

    interface EntryCollectionIterator<T, K extends Serializable> extends CollectionIterator<Entry<K,T>,K> {

        boolean equals(@Nullable EntryCollectionIterator<T, K> pEntryCollectionIterator);

        boolean remove(@Nullable Entry<K,T> pEntry);

        <E2 extends T> boolean removeAll(@NotNull EntryCollectionIterator<E2, K> pEntryCollectionIterator);

        <E2 extends T> boolean retainAll(@NotNull EntryCollectionIterator<E2, K> pEntryCollectionIterator);

        void clear();

    }

    interface EntrySetIterator<T, K extends Serializable> extends EntryCollectionIterator<T, K> {

        @NotNull
        @Override
        SetIterator.PartialSet<Entry<K, T>> nextCollection();

        @NotNull
        @Override
        SetIterator.PartialSet<Entry<K, T>> collectionOf(@NotNull K pIndex);

        boolean equals(@Nullable EntrySetIterator<T, K> pEntrySetIterator);
    }
}
