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

/*    public DataLongListIterator(Iterable<T> pIterable) {
        this(pIterable,false);
    }

    public DataLongListIterator(Iterable<T> pIterable, final boolean pAsync) {
        values[0] = new LinkedList<>();
        values[1] = new LinkedList<>();
        Parallel.For(pIterable, new Parallel.Operation<T, Void>() {
            @Override
            public Void perform(T pParameter) {
                synchronized (DataLongListIterator.this) {
                    DataLongListIterator.this.add(pParameter);
                }
                return null;
            }

            @Override
            public boolean follow() {
                return true;
            }

            @Override
            public boolean result() {
                return false;
            }

            @Override
            public boolean async() {
                return pAsync;
            }
        });
    }*/

    private DataLongListIterator(@NotNull LinkedList<LinkedList<T>> pListOne, @NotNull LinkedList<LinkedList<T>> pListTwo) {
        values[0] = pListOne;
        values[1] = pListTwo;
        for (LinkedList<LinkedList<T>> lla : values) {
            Parallel.For(lla, new Parallel.Operation<LinkedList<T>, Void>() {
                @Override
                public Void perform(LinkedList<T> pParameter) {
                    synchronized (DataLongListIterator.this) {
                        mSize+=pParameter.size();
                    }
                    return null;
                }

                @Override
                public boolean follow() {
                    return true;
                }

                @Override
                public boolean result() {
                    return false;
                }

                @Override
                public boolean async() {
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
        long result = NULL_INDEX;
        for (Collection<T> collection : this.allCollections()) {
            Iterator<Integer> iterator = Parallel.For(collection, new Parallel.Operation<T, Integer>() {
                boolean follow = true;
                boolean result = false;
                int index = NULL_INDEX;
                @Override
                public synchronized Integer perform(T pParameter) {
                    index++;
                    if (pKey == null) {
                        result = pParameter.getId() == null;
                    } else {
                        result = pKey.equals(pParameter.getId());
                    }
                    follow = !result;
                    return index;
                }

                @Override
                public boolean follow() {
                    return follow;
                }

                @Override
                public boolean result() {
                    return result;
                }

                @Override
                public boolean async() {
                    return false;
                }
            }).iterator();
            if (iterator.hasNext()) {
                result = iterator.next();
            }
            if (result != NULL_INDEX) {
                break;
            }
        }
        return result;
    }

    @Override
    public final int currentCollectionIndex() {
        return this.collectionIndexOf(mIndex);
    }

    @Override
    public final boolean contains(@NotNull final T pData) {
        boolean result = false;
        for (final LinkedList<LinkedList<T>> lla : values) {
            Iterator<Boolean> iterator = Parallel.For(lla, new Parallel.Operation<LinkedList<T>, Boolean>() {
                boolean follow = true;
                boolean result = false;
                @Override
                public synchronized Boolean perform(LinkedList<T> pParameter) {
                    result = pParameter.contains(pData);
                    follow = !result;
                    return result;
                }

                @Override
                public boolean follow() {
                    return follow;
                }

                @Override
                public boolean result() {
                    return result;
                }

                @Override
                public boolean async() {
                    return true;
                }
            }).iterator();
            if (iterator.hasNext()) {
                result = iterator.next();
            }
            if (result) break;
        }
        return result;
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
        this.remove(this.actual());
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
    public final void remove(@NotNull final T pData) {
        class ParallelResult {
            long index;
            Data<Long> data;
        }
        long index = NULL_INDEX;
        for (final LinkedList<LinkedList<T>> lla : values) {
            LinkedList<T> list = null;
            boolean result = false;
            for (final LinkedList<T> ll : lla) {
                Data<Long> data = null;
                Parallel.Operation<T,ParallelResult> operation = new Parallel.Operation<T, ParallelResult>() {
                    boolean follow = true;
                    boolean result = false;
                    int tIndex = NULL_INDEX;
                    @Override
                    public synchronized ParallelResult perform(final T pParameter) {
                        tIndex++;
                        result = pData.equals(pParameter);
                        follow = !result;
                        return new ParallelResult(){{this.index = tIndex;this.data = pParameter;}};
                    }

                    @Override
                    public boolean follow() {
                        return follow;
                    }

                    @Override
                    public boolean result() {
                        return result;
                    }

                    @Override
                    public boolean async() {
                        return false;
                    }
                };
                for (ParallelResult parallelResult :
                        Parallel.For(ll, operation)) {
                    data = parallelResult.data;
                    index = parallelResult.index;
                }

                if (data != null) {
                    list = ll;
                    break;
                }
            }

            if (list != null) {
                result = list.remove(pData);
                if (result) {
                    if (index <= mIndex) {
                        mIndex--;
                    }
                    mSize--;
                }
            }
            if (result) break;
        }
    }

    @Override
    public final <E extends T> void retainAll(@NotNull DataListIterator<E, Long> pDataListIterator) {
        final DataLongListIterator<T> dlli = new DataLongListIterator<T>();
        for (Collection<E> collection : pDataListIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E,Void>() {

                @Override
                public Void perform(E pParameter) {
                    synchronized (dlli) {
                        dlli.add(pParameter);
                    }
                    return null;
                }

                @Override
                public boolean follow() {
                    return true;
                }

                @Override
                public boolean result() {
                    return false;
                }

                //No order preference
                @Override
                public boolean async() {
                    return true;
                }
            });
        }

        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<T, Void>() {
                @Override
                public Void perform(T pParameter) {
                    if (!dlli.contains(pParameter)) {
                        synchronized (DataLongListIterator.this) {
                            DataLongListIterator.this.remove(pParameter);
                        }
                    }
                    return null;
                }

                @Override
                public boolean follow() {
                    return true;
                }

                @Override
                public boolean result() {
                    return false;
                }

                @Override
                public boolean async() {
                    return true;
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
        Parallel.Operation<E,Void> operation = new Parallel.Operation<E, Void>() {
            long index = pIndex;
            @Override
            public Void perform(E pParameter) {
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
                return null;
            }

            @Override
            public boolean follow() {
                return true;
            }

            @Override
            public boolean result() {
                return false;
            }

            @Override
            public boolean async() {
                return false;
            }
        };
        for (Collection<E> collection : pDataListIterator.allCollections()) {
            Parallel.For(collection, operation);
        }
    }

    @Override
    public final boolean equals(@NotNull final DataListIterator<T, Long> pDataListIterator) {
        //Save time
        if (this.equals((Object)pDataListIterator)) return true;
        boolean result;
        if ((result = this.size().equals(pDataListIterator.size())) && this.size() != 0) {
            final Iterator<Collection<T>> collections = pDataListIterator.allCollections().iterator();
            for (Collection<T> collection : this.allCollections()) {
                final Iterator<T> iterator = collections.next().iterator();
                Iterator<Boolean> i = Parallel.For(collection, new Parallel.Operation<T, Boolean>() {
                    boolean follow = true;
                    boolean result = false;
                    @Override
                    public synchronized Boolean perform(T pParameter) {
                        boolean result = pParameter.equals(iterator.next());
                        this.result = !result;
                        this.follow = result;
                        return result;
                    }

                    @Override
                    public boolean follow() {
                        return follow;
                    }

                    @Override
                    public boolean result() {
                        return result;
                    }

                    @Override
                    public boolean async() {
                        return false;
                    }
                }).iterator();
                if (i.hasNext()) {
                    result = i.next();
                }
                if (!result) break;
            }
        }
        return result;
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

/*    @Override
    @Nullable
    public final T remove(@NotNull Long pIndex) {
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_INDEX + 1) * ((long) MAX_COLLECTION_INDEX + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_INDEX + 1)*((long) MAX_COLLECTION_INDEX +1))) / ((long) MAX_COLLECTION_INDEX + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        if (values[arrayIndex].get(listIndex) == null) return null;
        if (pIndex <= mIndex) {
            mIndex--;
        }
        return values[arrayIndex].get(listIndex).remove(collectionIndexOf(pIndex));
    }*/

    @Override
    @NotNull
    public final Long indexOf(@NotNull final T pData) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T,Void> operation = new Parallel.Operation<T, Void>() {
            long index = NULL_INDEX;
            boolean follow = true;
            @Override
            public Void perform(T pParameter) {
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
                return null;
            }

            @Override
            public boolean follow() {
                return follow;
            }

            @Override
            public boolean result() {
                return false;
            }

            @Override
            public boolean async() {
                return false;
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
        Parallel.Operation<T,Void> operation = new Parallel.Operation<T, Void>() {
            long index = NULL_INDEX;
            @Override
            public Void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                if (pData.equals(pParameter)) {
                    synchronized (DataLongListIterator.this) {
                        result[0] = index;
                    }
                }
                return null;
            }

            @Override
            public boolean follow() {
                return false;
            }

            @Override
            public boolean result() {
                return false;
            }

            @Override
            public boolean async() {
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
        Parallel.Operation<T,Void> operation = new Parallel.Operation<T, Void>() {
            long index = NULL_INDEX;
            @Override
            public Void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                if (index >= pIndex) {
                    synchronized (DataLongListIterator.this) {
                        result.add(pParameter);
                    }
                }
                return null;
            }

            @Override
            public boolean follow() {
                return true;
            }

            @Override
            public boolean result() {
                return false;
            }

            @Override
            public boolean async() {
                return false;
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
        Parallel.Operation<T,Void> operation = new Parallel.Operation<T, Void>() {
            long index = NULL_INDEX;
            boolean follow = true;
            @Override
            public Void perform(T pParameter) {
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
                return null;
            }

            @Override
            public boolean follow() {
                return follow;
            }

            @Override
            public boolean result() {
                return false;
            }

            @Override
            public boolean async() {
                return false;
            }
        };
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection,operation);
        }
        return result;
    }
}
