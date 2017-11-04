package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataSetIterator<T extends Data<K>, K extends Number> extends DataCollectionIterator<T, K> {
    boolean equals(DataSetIterator<T, K> pDataSet);
}
