package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeySortedSet<K extends Serializable> extends KeySet<K> {
    Comparator<? super K> comparator();

    K first();

    KeySortedSet<K> headSet(K toElement);

    K last();

    KeySortedSet<K> subSet(K fromElement, K toElement);

    KeySortedSet<K> tailSet(K fromElement);
}
