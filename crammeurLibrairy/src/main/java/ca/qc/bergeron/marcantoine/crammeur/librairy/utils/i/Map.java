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

    interface EntryCollectionIterator<E extends Entry<K, T>, T, K extends Serializable> extends CollectionIterator<E,K> {

        boolean equals(@NotNull EntryCollectionIterator<E, T, K> pEntryCollectionIterator);

        boolean remove(@Nullable E pEntry);

        <E2 extends T> boolean removeAll(@NotNull EntryCollectionIterator<Entry<K,E2>, E2, K> pEntryCollectionIterator);

        <E2 extends T> boolean retainAll(@NotNull EntryCollectionIterator<Entry<K,E2>, E2, K> pEntryCollectionIterator);

        void clear();

    }

    interface EntrySetIterator<E extends Entry<K, T>, T, K extends Serializable> extends EntryCollectionIterator<E, T, K> {
        boolean equals(@NotNull EntrySetIterator<E, T, K> pEntrySetIterator);
    }
}
