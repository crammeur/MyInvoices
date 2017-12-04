package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface SortedSetIterator<E extends Serializable, K extends Serializable> extends SetIterator<E, K> {
    Comparator<? extends E> comparator();

    E first();

    SortedSetIterator<E, K> headSet(E toElement);

    E last();

    SortedSetIterator<E, K> subSet(E fromElement, E toElement);

    SortedSetIterator<E, K> tailSet(E fromElement);
}
