package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
    public final Iterable<Collection<Integer>> allCollections() {
        return new Iterable<Collection<Integer>>() {
            @NotNull
            @Override
            public Iterator<Collection<Integer>> iterator() {
                return new Iterator<Collection<Integer>>() {

                    private final HashSet<Integer> values = KeyIntegerSetIterator.this.values;
                    private transient volatile int mIndex = NULL_INDEX;
                    private transient volatile int mSize = 1;

                    @Override
                    public boolean hasNext() {
                        return mIndex + 1 < mSize;
                    }

                    @Override
                    public Collection<Integer> next() {
                        return values;
                    }
                };
            }
        };
    }

    @Deprecated
    @NotNull
    @Override
    public final Set<Integer> collectionOf(@NotNull Integer pIndex) {
        return values;
    }

    @NotNull
    @Override
    public final Integer count(@Nullable final Integer pEntity) {
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
    public final void add(@Nullable Integer pEntity) {
        if (!values.add(pEntity)) {
            if (values.contains(pEntity)) {
                throw new ContainsException("The value is already present");
            } else {
                throw new RuntimeException("The value has not been added");
            }
        }
    }

    protected final Integer actual() {
        if (mIndex != NULL_INDEX && mIndex < values.size()) {
            final Integer[] result = new Integer[1];
            final int[] index = new int[1];
            index[0] = NULL_INDEX;
            Parallel.For(values, new Parallel.Operation<Integer>() {
                @Override
                public void perform(Integer pParameter) {
                    synchronized (index) {
                        index[0]++;
                    }
                    if (index[0] == mIndex) {
                        synchronized (result) {
                            result[0] = pParameter;
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return index[0] < mIndex;
                }
            });
            return result[0];
        } else
            throw new IndexOutOfBoundsException(String.valueOf(mIndex));

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
    public final Integer next() {
        if (hasNext()) {
            mIndex++;
            return actual();
        } else
            throw new NoSuchElementException();
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
    public final Integer previous() {
        if (hasPrevious()) {
            mIndex--;
            return actual();
        } else
            throw new NoSuchElementException();
    }

    @Override
    public final void set(@Nullable Integer pEntity) {
        values.add(pEntity);
    }

    @Override
    public final boolean remove(@Nullable Integer pKey) {
        return values.remove(pKey);
    }

    @Override
    public final <E extends Integer> boolean retainAll(@NotNull CollectionIterator<E, Integer> pKeyCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = true;
        final KeyIntegerSetIterator retain = new KeyIntegerSetIterator();
        for (Collection<E> collection : pKeyCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {

                @Override
                public void perform(E pParameter) {
                    retain.add(pParameter);
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }

        final KeyIntegerSetIterator delete = new KeyIntegerSetIterator();
        for (Collection<Integer> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<Integer>() {
                @Override
                public void perform(Integer pParameter) {
                    if (!retain.contains(pParameter)) {
                        delete.add(pParameter);
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }

        for (Collection<Integer> collection : delete.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<Integer>() {
                @Override
                public void perform(Integer pParameter) {
                    synchronized (result) {
                        result[0] = KeyIntegerSetIterator.this.remove(pParameter);
                    }
                }

                @Override
                public boolean follow() {
                    return result[0];
                }
            });
        }
        return result[0];
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
