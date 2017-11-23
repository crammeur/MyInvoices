package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.ListIterator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataListIterator<T extends Data<K>, K extends Serializable> extends DataCollectionIterator<T, K>, ListIterator<T> {

    T get(@NotNull K pIndex);

    T set(@NotNull K pIndex, @Nullable T pData);

    boolean addToList(@Nullable T pData);

    <E extends T> boolean addAllToList(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) CollectionIterator<E, K> pDataCollectionIterator);

    void add(@NotNull K pIndex, @Nullable T pData);

    <E extends T> void addAll(@NotNull K pIndex, @NotNull DataCollectionIterator<E, K> pDataCollectionIterator);

    @NotNull
    K indexOf(@Nullable T pData);

    @NotNull
    K lastIndexOf(@Nullable T pData);

    @NotNull
    K indexOfKey(@Nullable K pKey);

    @NotNull
    K lastIndexOfKey(@Nullable K pKey);

    @NotNull
    @Override
    List<T> currentCollection();

    @NotNull
    @Override
    List<T> collectionOf(@NotNull K pIndex);

    @NotNull
    ListIterator<T> listIterator();

    @NotNull
    ListIterator<T> listIterator(@NotNull K pIndex);

    @NotNull
    DataListIterator<T, K> subDataListIterator(@NotNull K pIndex1, @NotNull K pIndex2);
}
