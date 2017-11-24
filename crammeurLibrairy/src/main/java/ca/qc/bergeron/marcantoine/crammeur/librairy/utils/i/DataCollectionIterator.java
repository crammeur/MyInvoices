package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataCollectionIterator<T extends Data<K>, K extends Serializable> extends CollectionIterator<T,K> {

    boolean remove(@Nullable T pData);

    <E extends T> boolean removeAll(@NotNull CollectionIterator<E, K> pCollectionIterator);

    <E extends T> boolean retainAll(@NotNull CollectionIterator<E, K> pCollectionIterator);

    void clear();

}
