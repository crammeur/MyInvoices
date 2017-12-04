package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeySetIterator<K extends Serializable> extends CollectionIterator<K,K> {
    @NotNull
    @Override
    Set<K> currentCollection();

    @NotNull
    @Override
    Set<K> collectionOf(@NotNull K pIndex);

    boolean equals(@Nullable final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.KeySetIterator<K> pKeySetIterator);
}
