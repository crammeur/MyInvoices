package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataSetIterator<T extends Data<K>, K extends Number> extends DataCollectionIterator<T, K> {

    boolean addToSet(@Nullable T pData);

    <E extends T> boolean addAllToSet(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) CollectionIterator<E, K> pDataCollectionIterator);
}
