package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeySortedSetIterator<K extends Serializable> extends KeySetIterator<K> {
    Comparator<? extends K> comparator();

    K first();

    KeySortedSetIterator<K> headSet(K toElement);

    K last();

    KeySortedSetIterator<K> subSet(K fromElement, K toElement);

    KeySortedSetIterator<K> tailSet(K fromElement);
}
