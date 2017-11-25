package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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

    public KeyLongSetIterator() {
        values[0] = new HashSet<>();
        values[1] = new HashSet<>();
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
        for (Collection<Long> collection : this.allCollections()) {

        }
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
    public final Long size() {
        return mSize;
    }

    @Override
    public final boolean isEmpty() {
        return mSize == 0;
    }

    @Override
    public final int currentCollectionIndex() {
        return collectionIndexOf(mIndex);
    }

    @Override
    public final int collectionIndexOf(@NotNull Long pIndex) {
        return (int) (pIndex % Integer.MAX_VALUE);
    }

    @NotNull
    @Override
    public final Set<Long> currentCollection() {
        return collectionOf(mIndex);
    }

    @NotNull
    @Override
    public Iterable<Collection<Long>> allCollections() {
        return null;
    }

    @NotNull
    @Override
    public Set<Long> collectionOf(@NotNull Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final Set<Long> result = new LinkedHashSet<>();
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        long size = (arrayIndex == 1)?(long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX:0;
        for (final Set<Long> set : values[arrayIndex]) {
            size+=set.size();
            if (pIndex < size) {
                Parallel.For(set, new Parallel.Operation<Long>() {
                    @Override
                    public void perform(Long pParameter) {
                        synchronized (result) {
                            if (!result.add(pParameter)) throw new RuntimeException("The result has not been added");
                        }
                    }

                    @Override
                    public boolean follow() {
                        return true;
                    }
                });
            }
            if (result.size() == Integer.MAX_VALUE) break;
        }
        return result;
    }

    @NotNull
    @Override
    public Long count(@Nullable final Long pEntity) {
        final long[] result = new long[1];
        for (Collection<Long> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<Long>() {
                @Override
                public void perform(Long pParameter) {
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
        }
        return result[0];
    }

    @Override
    public void add(@Nullable final Long pEntity) {
        final boolean[] contain = new boolean[1];
        final long[] traveled = new long[1];
        if (this.isEmpty()) {
            values[0].add(new HashSet<Long>(){{if (!add(pEntity)) throw new RuntimeException("The value has not been added");}});
        } else {
            for (int arrayIndex=0; arrayIndex<values.length; arrayIndex++) {
                final int finalArrayIndex = arrayIndex;
                Parallel.For(values[arrayIndex], new Parallel.Operation<HashSet<Long>>() {
                    boolean follow = true;
                    @Override
                    public void perform(HashSet<Long> pParameter) {
                        if (pParameter.contains(pEntity)) {
                            synchronized (contain) {
                                contain[0] = true;
                            }
                            synchronized (this) {
                                follow = false;
                            }
                        }
                        synchronized (traveled) {
                            traveled[0]+=pParameter.size();
                        }
                        if (!contain[0] && traveled[0] == mSize) {
                            if (pParameter.size() == Integer.MAX_VALUE) {
                                HashSet<Long> set = new HashSet<>();
                                if (set.add(pEntity)) {
                                    synchronized (values) {
                                        if (values[finalArrayIndex].add(set)) {
                                            synchronized (KeyLongSetIterator.this) {
                                                KeyLongSetIterator.this.mSize++;
                                            }
                                        } else {
                                            throw new RuntimeException("The value has not been added");
                                        }
                                    }
                                } else {
                                    throw new RuntimeException("The value has not been added");
                                }
                            } else {
                                if (pParameter.add(pEntity)) {
                                    synchronized (KeyLongSetIterator.this) {
                                        KeyLongSetIterator.this.mSize++;
                                    }
                                } else {
                                    throw new RuntimeException("The value has not been added");
                                }
                            }
                        }
                    }

                    @Override
                    public boolean follow() {
                        return follow;
                    }
                });
                if (contain[0]) break;
            }
            if (contain[0]) throw new RuntimeException();
        }
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
