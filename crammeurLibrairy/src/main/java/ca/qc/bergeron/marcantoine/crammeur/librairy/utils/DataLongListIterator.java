package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public final class DataLongListIterator<T extends Data<Long>> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.DataListIterator<T, Long> {

    protected final LinkedList<LinkedList<T>>[] values = new LinkedList[2];
    protected transient volatile long mIndex = NULL_INDEX;
    protected transient volatile long mSize = 0L;

    private DataLongListIterator(@NotNull LinkedList<LinkedList<T>> pListOne, @NotNull LinkedList<LinkedList<T>> pListTwo) {
        values[0] = pListOne;
        values[1] = pListTwo;
        for (LinkedList<LinkedList<T>> lla : values) {
            Parallel.For(lla, new Parallel.Operation<LinkedList<T>>() {
                @Override
                public void perform(LinkedList<T> pParameter) {
                    synchronized (DataLongListIterator.this) {
                        mSize+=pParameter.size();
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }
    }

    public DataLongListIterator() {
        values[0] = new LinkedList<LinkedList<T>>();
        values[1] = new LinkedList<LinkedList<T>>();
    }

    @Override
    protected final void finalize() throws Throwable {
        super.finalize();
        values[0] = null;
        values[1] = null;
        mIndex = NULL_INDEX;
        mSize = 0L;
    }

    @Override
    @NotNull
    public final Long size() {
        return mSize;
    }

    @Override
    public final boolean isEmpty() {
        return mSize == 0;
    }

    @Override
    @NotNull
    public final Long indexOfKey(@Nullable final Long pKey) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            boolean follow = true;
            int index = NULL_INDEX;
            @Override
            public void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                if ((pKey == null && pParameter.getId() == null) || (pKey != null && pKey.equals(pParameter.getId()))) {
                    synchronized (result) {
                        result[0] = index;
                    }
                    synchronized (this) {
                        follow = false;
                    }
                }
            }

            @Override
            public boolean follow() {
                return follow;
            }
        };
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, operation);
            if (result[0] != NULL_INDEX) {
                break;
            }
        }
        return result[0];
    }

    @NotNull
    @Override
    public Long lastIndexOfKey(@Nullable final Long pKey) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            int index = NULL_INDEX;
            @Override
            public void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                boolean equals;
                if (pKey == null) {
                    equals = pParameter.getId() == null;
                } else {
                    equals = pKey.equals(pParameter.getId());
                }
                if (equals) {
                    synchronized (result) {
                        result[0] = index;
                    }
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        };
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result[0];
    }

    @Override
    public final int currentCollectionIndex() {
        return this.collectionIndexOf(mIndex);
    }

    @Override
    public final boolean contains(@NotNull final T pData) {
        final boolean[] result = new boolean[1];
        result[0] = false;
        for (final LinkedList<LinkedList<T>> lla : values) {
            Parallel.For(lla, new Parallel.Operation<LinkedList<T>>() {
                boolean follow = true;
                @Override
                public void perform(LinkedList<T> pParameter) {
                    boolean contains = pParameter.contains(pData);
                    if (contains) {
                        synchronized (result) {
                            result[0] = true;
                        }
                        synchronized (this) {
                            follow = false;
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return follow;
                }
            });
            if (result[0]) break;
        }
        return result[0];
    }

    @Override
    public final boolean hasNext() {
        return mIndex + 1 < mSize;
    }

    @Override
    @NotNull
    public final T next() {
        if (hasNext()) {
            mIndex++;
            return actual();
        } else
            throw new NoSuchElementException();
    }

    @Override
    public final boolean hasPrevious() {
        return (mIndex != NULL_INDEX) &&  mIndex - 1 != NULL_INDEX;
    }

    @Override
    @NotNull
    public final T previous() {
        if (hasPrevious()) {
            mIndex--;
            return actual();
        } else
             throw new NoSuchElementException();

    }

    @NotNull
    public final T actual() {
        if (mIndex != -1 && mIndex < Long.MAX_VALUE) {
            final int arrayIndex = (int) (mIndex / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX));
            final int listIndex = (arrayIndex == 1)
                    ? BigDecimal.valueOf(((mIndex / ((long) MAX_COLLECTION_INDEX + 1)) + 1)).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP).add(BigDecimal.ONE.negate()).multiply(BigDecimal.valueOf(2)).intValue()
                    : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
            return values[arrayIndex].get(listIndex).get(collectionIndexOf(mIndex));
        } else
            throw new IndexOutOfBoundsException(String.valueOf(mIndex));

    }

    @Override
    public final int collectionIndexOf(@NotNull Long pIndex) {
        return (int) (pIndex % Integer.MAX_VALUE);
    }

    @Override
    @NotNull
    public final LinkedList<T> currentCollection() {
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_INDEX + 1)*((long) MAX_COLLECTION_INDEX +1))) / ((long) MAX_COLLECTION_INDEX + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        return new LinkedList<>(values[arrayIndex].get(listIndex));
    }

    @Override
    @NotNull
    public final Iterable<Collection<T>> allCollections() {
        return new Iterable<Collection<T>>() {
            @NotNull
            @Override
            public Iterator<Collection<T>> iterator() {
                return new Iterator<Collection<T>>() {

                    private LinkedList<LinkedList<T>>[] values = DataLongListIterator.this.values;
                    private transient volatile long mIndex = NULL_INDEX;
                    private transient volatile long mSize = values[0].size() + values[1].size();

                    @Override
                    public boolean hasNext() {
                        return mIndex + 1 < mSize;
                    }

                    @Override
                    public Collection<T> next() {
                        if (mIndex + 1 < Integer.MAX_VALUE) {
                            return values[0].get((int)(++mIndex));
                        } else {
                            return values[1].get((int)((++mIndex)%Integer.MAX_VALUE));
                        }
                    }

                    @Override
                    public void remove() {
                        if (mIndex < Integer.MAX_VALUE) {
                            values[0].remove((int)mIndex);
                        } else {
                            values[1].remove((int)(mIndex%Integer.MAX_VALUE));
                        }
                    }

                };
            }
        };
    }

    @Override
    @NotNull
    public final LinkedList<T> collectionOf(@NotNull Long pIndex) {
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_INDEX + 1)*((long) MAX_COLLECTION_INDEX +1))) / ((long) MAX_COLLECTION_INDEX + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        return new LinkedList<>(values[arrayIndex].get(listIndex));
    }

    @Override
    public final int nextIndex() {
        if (mIndex + 1 != Long.MAX_VALUE && mIndex + 1 < mSize) {
            return collectionIndexOf(mIndex + 1);
        } else {
            return collectionIndexOf(mSize);
        }
    }

    @Override
    public final int previousIndex() {
        if (mIndex - 1 > MIN_INDEX && mIndex != NULL_INDEX) {
            return collectionIndexOf(mIndex - 1);
        } else {
            return NULL_INDEX;
        }
    }

    @Override
    public final void remove() {
        if (mIndex == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_INDEX + 1)*((long) MAX_COLLECTION_INDEX +1))) / ((long) MAX_COLLECTION_INDEX + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        values[arrayIndex].get(listIndex).remove(this.currentCollectionIndex());
        mIndex--;
    }

    @Override
    public final void set(@NotNull T t) {
        if (mIndex == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_INDEX + 1)*((long) MAX_COLLECTION_INDEX +1))) / ((long) MAX_COLLECTION_INDEX + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        values[arrayIndex].get(listIndex).set(this.currentCollectionIndex(), t);
    }

    /**
     * Add data to list iterator
     * @param pData Data
     */
    @Override
    public final void add(@NotNull T pData) {
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_INDEX + 1)*((long) MAX_COLLECTION_INDEX +1))) / ((long) MAX_COLLECTION_INDEX + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        if (values[arrayIndex].isEmpty() || (listIndex > 0 && values[arrayIndex].get(listIndex-1).size() == Integer.MAX_VALUE)) {
            values[arrayIndex].add(new LinkedList<T>());
        }
        values[arrayIndex].get(values[arrayIndex].size() - 1).add(pData);
        mSize++;
    }

    @Override
    public final boolean remove(@NotNull final T pData) {
        final long[] index = new long[1];
        index[0] = NULL_INDEX;
        final boolean[] result = new boolean[1];
        result[0] = false;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            boolean follow = true;
            @Override
            public void perform(final T pParameter) {
                synchronized (index) {
                    index[0]++;
                }
                synchronized (result) {
                    result[0] = pData.equals(pParameter);
                }
                synchronized (this) {
                    follow = !result[0];
                }
            }

            @Override
            public boolean follow() {
                return follow;
            }
        };
        for (final LinkedList<LinkedList<T>> lla : values) {
            LinkedList<T> list = null;
            for (final LinkedList<T> linkedList : lla) {

                Parallel.For(linkedList, operation);

                if (result[0]) {
                    list = linkedList;
                    break;
                }
            }

            if (list != null) {
                result[0] = list.remove(pData);
                if (result[0]) {
                    if (index[0] != NULL_INDEX && index[0] <= mIndex) {
                        mIndex--;
                    }
                    mSize--;
                }
            }
            if (result[0]) break;
        }
        return result[0];
    }

    @Override
    public final <E extends T> void retainAll(@NotNull DataListIterator<E, Long> pDataListIterator) {
        final DataLongListIterator<T> dlli = new DataLongListIterator<T>();
        for (Collection<E> collection : pDataListIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {

                @Override
                public void perform(E pParameter) {
                    dlli.add(pParameter);
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }

        final DataLongListIterator<T> dlliDelete = new DataLongListIterator<>();
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<T>() {
                @Override
                public void perform(T pParameter) {
                    if (!dlli.contains(pParameter)) {
                        dlliDelete.add(pParameter);
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }
        for (Collection<T> collection : dlliDelete.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<T>() {
                @Override
                public void perform(T pParameter) {
                    DataLongListIterator.this.remove(pParameter);
                }

                @Override
                public boolean follow() {
                    return false;
                }
            });
        }
    }

    @Override
    public final void clear() {
        values[0].clear();
        values[1].clear();
        mIndex = NULL_INDEX;
        mSize = 0L;
    }

    @Override
    public final Iterator<T> iterator() {
        return new DataLongListIterator<T>(values[0], values[1]);
    }

    @Override
    public <E extends T> void addAll(@NotNull final Long pIndex, @NotNull DataListIterator<E, Long> pDataListIterator) {
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            long index = pIndex;
            @Override
            public void perform(E pParameter) {
                final int arrayIndex = (int) (index / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX));
                final int listIndex = (arrayIndex == 1)
                        ? BigDecimal.valueOf(((index / ((long) MAX_COLLECTION_INDEX + 1)) + 1))
                        .divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP)
                        .add(BigDecimal.ONE.negate())
                        .multiply(BigDecimal.valueOf(2)).intValue()
                        : (int) (index / ((long) MAX_COLLECTION_INDEX + 1));
                synchronized (DataLongListIterator.this.values) {
                    values[arrayIndex].get(listIndex).add(collectionIndexOf(index), pParameter);
                }
                synchronized (this) {
                    index++;
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        };
        for (Collection<E> collection : pDataListIterator.allCollections()) {
            Parallel.For(collection, operation);
        }
    }

    @Override
    @Nullable
    public final T get(@NotNull Long pIndex) {
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_INDEX + 1)*((long) MAX_COLLECTION_INDEX +1))) / ((long) MAX_COLLECTION_INDEX + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        if (values[arrayIndex].get(listIndex) == null) return null;
        return values[arrayIndex].get(listIndex).get(collectionIndexOf(pIndex));
    }

    @Override
    @Nullable
    public final T set(@NotNull Long pIndex, @NotNull T pData) {
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_INDEX + 1)*((long) MAX_COLLECTION_INDEX +1))) / ((long) MAX_COLLECTION_INDEX + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        if (values[arrayIndex].get(listIndex) == null) return null;
        return values[arrayIndex].get(listIndex).set(collectionIndexOf(pIndex), pData);
    }

    @Override
    public final void add(@NotNull Long pIndex, @NotNull T pData) {
        final long index = pIndex;
        final T data = pData;
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_INDEX + 1)*((long) MAX_COLLECTION_INDEX +1))) / ((long) MAX_COLLECTION_INDEX + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        if (values[arrayIndex].get(listIndex) != null) {
            values[arrayIndex].get(listIndex).add(collectionIndexOf(index), data);
        } else {
            values[arrayIndex].add(listIndex, new LinkedList<T>() {{
                add(collectionIndexOf(index), data);
            }});
        }
        if (pIndex <= this.mIndex) this.mIndex++;
    }

    @Override
    @NotNull
    public final Long indexOf(@NotNull final T pData) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            long index = NULL_INDEX;
            boolean follow = true;
            @Override
            public void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                if (pData.equals(pParameter)) {
                    synchronized (DataLongListIterator.this) {
                        result[0] = index;
                    }
                    synchronized (this) {
                        follow = false;
                    }
                }
            }

            @Override
            public boolean follow() {
                return follow;
            }
        };
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection,operation);
        }
        return result[0];
    }

    @Override
    @NotNull
    public final Long lastIndexOf(@NotNull final T pData) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            long index = NULL_INDEX;
            @Override
            public void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                if (pData.equals(pParameter)) {
                    synchronized (DataLongListIterator.this) {
                        result[0] = index;
                    }
                }
            }

            @Override
            public boolean follow() {
                return false;
            }
        };
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection,operation);
        }
        return result[0];
    }

    @NotNull
    @Override
    public final DataListIterator<T, Long> listIterator() {
        return new DataLongListIterator<>(values[0],values[1]);
    }

    @NotNull
    @Override
    public final DataListIterator<T, Long> listIterator(@NotNull final Long pIndex) {
        final DataListIterator<T, Long> result = new DataLongListIterator<>();
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            long index = NULL_INDEX;
            @Override
            public void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                if (index >= pIndex) {
                    synchronized (DataLongListIterator.this) {
                        result.add(pParameter);
                    }
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        };
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result;
    }

    @NotNull
    @Override
    public final DataListIterator<T, Long> subDataListIterator(@NotNull final Long pIndex1, @NotNull final Long pIndex2) {
        final DataListIterator<T, Long> result = new DataLongListIterator<>();
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            long index = NULL_INDEX;
            boolean follow = true;
            @Override
            public void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                if (index >= pIndex1 && index < pIndex2) {
                    synchronized (DataLongListIterator.this) {
                        result.add(pParameter);
                    }
                } else if (index >= pIndex2) {
                    synchronized (this) {
                        follow = false;
                    }
                }
            }

            @Override
            public boolean follow() {
                return follow;
            }
        };
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection,operation);
        }
        return result;
    }
}
