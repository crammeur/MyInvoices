package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataSetIterator<T extends Data<K>, K extends Serializable> extends SetIterator<T, K> {

    boolean equals(@Nullable final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataSetIterator<T,K> pDataSetIterator);
}
