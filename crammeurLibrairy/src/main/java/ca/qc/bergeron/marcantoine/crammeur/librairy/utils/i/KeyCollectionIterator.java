package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.ListIterator;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface KeyCollectionIterator<K extends Serializable> extends Iterable<K>, ListIterator<K> {
    K size();

    boolean isEmpty();

    boolean contains(K o);

    boolean remove(K o);

    boolean containsAll(@NotNull Collection<? extends K> collection);

    boolean addAll(@NotNull @Flow(sourceIsContainer = true, targetIsContainer = true) Collection<? extends K> collection);

    boolean removeAll(@NotNull Collection<? extends K> collection);

    boolean retainAll(@NotNull Collection<? extends K> collection);

    void clear();
}
