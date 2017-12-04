package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface SortedMap<K extends Serializable, V> extends Map<K, V> {
    Comparator<? extends K> comparator();

    K firstKey();

    SortedMap<K, V> headMap(K toKey);

    K lastKey();

    Map<K, V> subDataMap(K fromKey, K toKey);

    SortedMap<K, V> tailDataMap(K fromKey);
}
