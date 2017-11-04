package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.util.Comparator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataSortedSetIterator<T extends Data<K>, K extends Number> extends DataSetIterator<T, K> {
    Comparator<? super T> comparator();

    T first();

    DataSortedSetIterator<T, K> headSet(T toElement);

    T last();

    DataSortedSetIterator<T, K> subSet(T fromElement, T toElement);

    DataSortedSetIterator<T, K> tailSet(T fromElement);
}
