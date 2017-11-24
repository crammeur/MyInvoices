package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public final class KeyLongSetIterator extends KeySetIterator<Long> {

    protected final HashSet<HashSet<Long>>[] values = new HashSet[2];
    protected transient volatile long mIndex = NULL_INDEX;
    protected transient volatile long mSize = 0;

    private KeyLongSetIterator(HashSet<HashSet<Long>> pHashSetOne, HashSet<HashSet<Long>> pHashSetTwo) {
        values[0] = pHashSetOne;
        values[1] = pHashSetTwo;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        values[0] = null;
        values[1] = null;
        mIndex = NULL_INDEX;
        mSize = 0;
    }

    @Override
    public boolean remove(@Nullable Long pKey) {
        return false;
    }

    @Override
    public <E extends Long> boolean removeAll(@NotNull CollectionIterator<E, Long> pKeyCollectionIterator) {
        return false;
    }

    @Override
    public <E extends Long> boolean retainAll(@NotNull CollectionIterator<E, Long> pKeyCollectionIterator) {
        return false;
    }

    @Override
    public void clear() {
        values[0].clear();
        values[1].clear();
        mIndex = NULL_INDEX;
        mSize = 0;
    }

    @NotNull
    @Override
    public Long size() {
        return mSize;
    }

    @Override
    public boolean isEmpty() {
        return mSize == 0;
    }

    @Override
    public int currentCollectionIndex() {
        return collectionIndexOf(mIndex);
    }

    @Override
    public int collectionIndexOf(@NotNull Long pIndex) {
        return (int) (pIndex % Integer.MAX_VALUE);
    }

    @NotNull
    @Override
    public Collection<Long> currentCollection() {
        return null;
    }

    @NotNull
    @Override
    public Iterable<Collection<Long>> allCollections() {
        return null;
    }

    @NotNull
    @Override
    public Collection<Long> collectionOf(@NotNull final Long pIndex) {
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        final long[] index = new long[1];
        final Collection<Long> result = new LinkedList<>();
        for (Set<Long> set : values[arrayIndex]) {
            Parallel.For(set, new Parallel.Operation<Long>() {;
                boolean follow = true;
                @Override
                public void perform(Long pParameter) {
                    if (pIndex <= index[0]) {
                        synchronized (result) {
                            if (!result.add(pParameter)) throw new RuntimeException("The result has not been added");
                        }
                    }
                    synchronized (this) {
                        index[0]++;
                        if (result.size() == Integer.MAX_VALUE) follow = false;
                    }
                }

                @Override
                public boolean follow() {
                    return follow;
                }
            });
            if (result.size() == Integer.MAX_VALUE) break;
        }
        return result;
    }

    @NotNull
    @Override
    public Long count(@Nullable Long pEntity) {
        final long[] result = new long[1];

        return result[0];
    }

    @Override
    public void add(@Nullable Long pEntity) {

    }

    @Override
    public int nextIndex() {
        return 0;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Nullable
    @Override
    public Long next() {
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public int previousIndex() {
        return 0;
    }

    @Nullable
    @Override
    public Long previous() {
        return null;
    }

    @Override
    public void set(@Nullable Long pEntity) {

    }

    @NotNull
    @Override
    public final Iterator<Long> iterator() {
        return new KeyLongSetIterator(values[0],values[1]);
    }
}
