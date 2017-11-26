package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.ContainsException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;

/**
 * Created by Marc-Antoine on 2017-11-26.
 */

public final class KeyIntegerSetIterator extends KeySetIterator<Integer> {

    protected final HashSet<Integer> values;
    protected transient volatile int mIndex = NULL_INDEX;

    private KeyIntegerSetIterator(HashSet<Integer> pHashSetOne) {
        values = pHashSetOne;
    }

    public KeyIntegerSetIterator() {
        values = new HashSet<>();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mIndex = NULL_INDEX;
    }

    @NotNull
    @Override
    public Integer size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public int currentCollectionIndex() {
        return mIndex;
    }

    @Deprecated
    @Override
    public int collectionIndexOf(@NotNull Integer pIndex) {
        return pIndex;
    }

    @NotNull
    @Override
    public Set<Integer> currentCollection() {
        return values;
    }

    @NotNull
    @Override
    public Iterable<Collection<Integer>> allCollections() {
        return new Iterable<Collection<Integer>>() {
            @NotNull
            @Override
            public Iterator<Collection<Integer>> iterator() {
                return new Iterator<Collection<Integer>>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public Collection<Integer> next() {
                        return null;
                    }
                };
            }
        };
    }

    @NotNull
    @Override
    public Set<Integer> collectionOf(@NotNull Integer pIndex) {
        return values;
    }

    @NotNull
    @Override
    public Integer count(@Nullable final Integer pEntity) {
        final int[] result = new int[1];
        Parallel.For(values, new Parallel.Operation<Integer>() {
            @Override
            public void perform(Integer pParameter) {
                if ((pEntity == null && pParameter == null) || (pEntity != null && pEntity.equals(pParameter))) {
                    synchronized (result) {
                        result[0]++;
                    }
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        });
        return result[0];
    }

    @Override
    public void add(@Nullable Integer pEntity) {
        if (!values.add(pEntity)) throw new ContainsException("The value is already present");
    }

    @Override
    public final int nextIndex() {
        return mIndex + 1;
    }

    @Override
    public final boolean hasNext() {
        return mIndex + 1 < values.size();
    }

    @Nullable
    @Override
    public Integer next() {
        return null;
    }

    @Override
    public final boolean hasPrevious() {
        return mIndex - 1 >= MIN_INDEX;
    }

    @Override
    public final int previousIndex() {
        if (mIndex == NULL_INDEX) return NULL_INDEX;
        return mIndex - 1;
    }

    @Nullable
    @Override
    public Integer previous() {
        return null;
    }

    @Override
    public void set(@Nullable Integer pEntity) {
        values.add(pEntity);
    }

    @Override
    public boolean remove(@Nullable Integer pKey) {
        return values.remove(pKey);
    }

    @Override
    public <E extends Integer> boolean retainAll(@NotNull CollectionIterator<E, Integer> pKeyCollectionIterator) {
        return false;
    }

    @Override
    public final void clear() {
        values.clear();
        mIndex = NULL_INDEX;
    }

    @NotNull
    @Override
    public final Iterator<Integer> iterator() {
        return new KeyIntegerSetIterator(values);
    }
}
