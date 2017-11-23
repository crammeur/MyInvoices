package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.util.Comparator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataSortedMap<K extends Number, V extends Data<K>> extends DataMap<K, V> {
    Comparator<? extends K> comparator();

    K firstKey();

    DataSortedMap<K, V> headMap(K toKey);

    KeySetIterator<K> keySet();

    K lastKey();

    DataMap<K, V> subDataMap(K fromKey, K toKey);

    DataSortedMap<K, V> tailDataMap(K fromKey);

    DataListIterator<V, K> values();
}
