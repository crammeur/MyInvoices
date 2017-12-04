package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-12-03.
 */

public interface DataListIterator<T extends Data<K>, K extends Serializable> extends ListIterator<T,K> {
    @NotNull
    K indexOfKey(@Nullable K pKey);

    @NotNull
    K lastIndexOfKey(@Nullable K pKey);
}
