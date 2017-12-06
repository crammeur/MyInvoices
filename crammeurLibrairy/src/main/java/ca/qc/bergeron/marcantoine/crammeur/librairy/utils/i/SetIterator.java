package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface SetIterator<E, S extends Serializable> extends CollectionIterator<E, S> {
    @NotNull
    @Override
    Set<E> nextCollection();

    @NotNull
    @Override
    Set<E> collectionOf(@NotNull S pIndex);

    boolean equals(@Nullable final SetIterator<E,S> pSetIterator);

    E hashCode2();
}
