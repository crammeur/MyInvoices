package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.util.Comparator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataSortedMapIterator<K extends Number, V extends Data<K>> extends DataMapIterator<K, V> {
    Comparator<? extends K> comparator();

    K firstKey();

    DataSortedMapIterator<K, V> headMap(K toKey);

    KeySetIterator<K> keySet();

    K lastKey();

    DataMapIterator<K, V> subDataMap(K fromKey, K toKey);

    DataSortedMapIterator<K, V> tailDataMap(K fromKey);

    DataListIterator<V, K> values();
}
