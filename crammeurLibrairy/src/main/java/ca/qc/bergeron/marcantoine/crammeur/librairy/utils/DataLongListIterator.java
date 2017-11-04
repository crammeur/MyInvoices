package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataCollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public final class DataLongListIterator<T extends Data<Long>> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.DataListIterator<T, Long> {

    protected final LinkedList<LinkedList<T>>[] values = new LinkedList[2];
    protected transient volatile long mIndex = NULL_INDEX;
    private transient volatile long mSize = 0L;

    public DataLongListIterator(Iterable<T> pIterable) {
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
            public synchronized boolean async() {
                return pAsync;
            }
        });
    }

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
    protected void finalize() throws Throwable {
        super.finalize();
        values[0] = null;
        values[1] = null;
        mIndex = NULL_INDEX;
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
    public final Long indexOf(@Nullable final Long pKey) {
        long result = NULL_INDEX;
        for (Collection<T> collecttion : this.allCollections()) {
            Iterator<Integer> iterator = Parallel.For(collecttion, new Parallel.Operation<T, Integer>() {
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
                    if (result) follow = false;
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
        final int arrayIndex = (int) (mIndex / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX));
        final int listIndex = (arrayIndex == 1)
                ? BigDecimal.valueOf(((mIndex / ((long) MAX_COLLECTION_INDEX + 1)) + 1)).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP).add(BigDecimal.ONE.negate()).multiply(BigDecimal.valueOf(2)).intValue()
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        return new LinkedList<>(values[arrayIndex].get(listIndex));
    }

    @Override
    @NotNull
    public final Iterable<Collection<T>> allCollections() {
        final List<Collection<T>> result = new ArrayList<>();
        for (int arrayIndex=0;arrayIndex<2;arrayIndex++) {
            Parallel.For(values[arrayIndex], new Parallel.Operation<LinkedList<T>, Void>() {
                @Override
                public Void perform(LinkedList<T> pParameter) {
                    synchronized (result) {
                        result.add(pParameter);
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
            });
        }
        return result;
    }

    @Override
    @NotNull
    public final LinkedList<T> collectionOf(@NotNull Long pIndex) {
        final int arrayIndex = (int) (pIndex / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX));
        final int listIndex = (arrayIndex == 1)
                ? BigDecimal.valueOf(((pIndex / ((long) MAX_COLLECTION_INDEX + 1)) + 1)).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP).add(BigDecimal.ONE.negate()).multiply(BigDecimal.valueOf(2)).intValue()
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        return new LinkedList<>(values[arrayIndex].get(listIndex));
    }

    @Override
    public final int nextIndex() {
        if (mIndex != Long.MAX_VALUE) {
            return collectionIndexOf(mIndex + 1);
        } else {
            return MIN_INDEX;
        }
    }

    @Override
    public final int previousIndex() {
        if (mIndex != MIN_INDEX) {
            return collectionIndexOf(mIndex - 1);
        } else {
            return MAX_COLLECTION_INDEX;
        }
    }

    @Override
    public final void remove() {
        if (mIndex == NULL_INDEX) throw new IndexOutOfBoundsException(String.valueOf(NULL_INDEX));
        this.remove(this.actual());
    }

    @Override
    public final void set(@NotNull T t) {
        if (mIndex == NULL_INDEX) throw new IndexOutOfBoundsException(String.valueOf(NULL_INDEX));
        final int arrayIndex = (int) (mIndex / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX));
        final int listIndex = (arrayIndex == 1)
                ? BigDecimal.valueOf(((mIndex / ((long) MAX_COLLECTION_INDEX + 1)) + 1)).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP).add(BigDecimal.ONE.negate()).multiply(BigDecimal.valueOf(2)).intValue()
                : (int) (mIndex / ((long) MAX_COLLECTION_INDEX + 1));
        final int currentCollectionIndex = this.currentCollectionIndex();
        values[arrayIndex].get(listIndex).set(currentCollectionIndex, t);
    }

    /**
     * Add data to list iterator
     * @param pData Data
     */
    @Override
    public final void add(@NotNull T pData) {
        final int arrayIndex = (int) ((mSize - 1) / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX));
        final int listIndex = (arrayIndex == 1)
                ? BigDecimal.valueOf((((mSize - 1) / ((long) MAX_COLLECTION_INDEX + 1)) + 1)).divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP).add(BigDecimal.ONE.negate()).multiply(BigDecimal.valueOf(2)).intValue()
                : (int) ((mSize - 1) / ((long) MAX_COLLECTION_INDEX + 1));
        if (values[arrayIndex].isEmpty() || (listIndex > 0 && values[arrayIndex].get(listIndex-1).size() == Integer.MAX_VALUE)) {
            values[arrayIndex].add(new LinkedList<T>());
        }
        values[arrayIndex].get(values[arrayIndex].size() - 1).add(pData);
        mSize++;
    }

    @Override
    public final boolean remove(@NotNull final T o) {
        boolean result = false;
        boolean gc = false;
        class ParallelResult {
            long index;
            Data data;
        }
        long index = NULL_INDEX;
        final ParallelResult parallelResult = new ParallelResult();
        for (final LinkedList<LinkedList<T>> lla : values) {
            gc = true;
            LinkedList<T> list = null;
            for (final LinkedList<T> ll : lla) {
                Data d = null;
                Parallel.Operation<T,ParallelResult> parallelOperation = new Parallel.Operation<T, ParallelResult>() {
                    boolean follow = true;
                    boolean result = false;
                    int tIndex = NULL_INDEX;
                    @Override
                    public synchronized ParallelResult perform(final T pParameter) {
                        tIndex++;
                        result = o.equals(pParameter);
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
                for (ParallelResult parallelResult1 :
                        Parallel.For(ll, parallelOperation)) {
                    d = parallelResult1.data;
                    index = parallelResult.index;
                }

                if (d != null) {
                    list = ll;
                    break;
                }
                if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                    System.gc();
                if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                    gc = false;
            }

            if (list != null) {
                result = list.remove(o);
                if (result) {
                    if (index <= mIndex) {
                        mIndex--;
                    }
                    mSize--;
                }
            }
            if (result) break;
            if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                System.gc();
            if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                gc = false;

        }
        if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) System.gc();
        return result;
    }

    @Override
    public boolean retainAll(@NotNull Iterable<? extends T> collection) {
        final DataCollectionIterator<T, Long> c = new DataLongListIterator<T>();
        boolean gc = false;
        for (final T data : collection) {
            gc = true;
            c.add(data);
            if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                System.gc();
            if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                gc = false;

        }
        if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        gc = (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        boolean result = true;
        for (final T data : this) {
            if (!c.contains(data) && !this.remove(data)) result = false;
            if (!result) break;
            if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                System.gc();
            if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                gc = false;
        }
        if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        return result;
    }

    @Override
    public void clear() {
        values[0].clear();
        values[1].clear();
        mIndex = NULL_INDEX;
    }

    @Override
    public final Iterator<T> iterator() {
        return new DataLongListIterator<T>(values[0], values[1]);
    }

    @Override
    public boolean addAll(@NotNull Long pIndex, @NotNull Iterable<? extends T> var2) {
        boolean result = true;
        long index = pIndex;
        boolean gc = false;
        for (final T data : var2) {
            gc = true;
            final int arrayIndex = (int) (index / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX));
            final int listIndex = (arrayIndex == 1)
                    ? BigDecimal.valueOf(((index / ((long) MAX_COLLECTION_INDEX + 1)) + 1))
                    .divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP)
                    .add(BigDecimal.ONE.negate())
                    .multiply(BigDecimal.valueOf(2)).intValue()
                    : (int) (index / ((long) MAX_COLLECTION_INDEX + 1));
            values[arrayIndex].get(listIndex).add(collectionIndexOf(index), data);
            if (!values[arrayIndex].get(listIndex).contains(data)) result = false;
            if (!result) break;
            index++;
            if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                System.gc();
            if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                gc = false;
        }
        if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        return result;
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
                        boolean result;
                        result = pParameter.equals(iterator.next());
                        if (!result) this.result = true;
                        return follow = !this.result;
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
            if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                System.gc();
        }
        return result;
    }

    @Override
    @Nullable
    public T get(@NotNull Long pIndex) {
        final int arrayIndex = (int) (pIndex / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX + 1));
        final int listIndex = (arrayIndex == 1)
                ? BigDecimal.valueOf(((pIndex / ((long) MAX_COLLECTION_INDEX + 1)) + 1))
                .divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP)
                .add(BigDecimal.ONE.negate())
                .multiply(BigDecimal.valueOf(2)).intValue()
                : (int) (pIndex / ((long) MAX_COLLECTION_INDEX + 1));
        if (values[arrayIndex].get(listIndex) == null) return null;
        return values[arrayIndex].get(listIndex).get(collectionIndexOf(pIndex));
    }

    @Override
    @Nullable
    public T set(@NotNull Long pIndex, @NotNull T pData) {
        final int arrayIndex = (int) (pIndex / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX + 1));
        final int listIndex = (arrayIndex == 1)
                ? BigDecimal.valueOf(((pIndex / ((long) MAX_COLLECTION_INDEX + 1)) + 1))
                .divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP)
                .add(BigDecimal.ONE.negate())
                .multiply(BigDecimal.valueOf(2)).intValue()
                : (int) (pIndex / ((long) MAX_COLLECTION_INDEX + 1));
        if (values[arrayIndex].get(listIndex) == null) return null;
        return values[arrayIndex].get(listIndex).set(collectionIndexOf(pIndex), pData);
    }

    @Override
    public void add(@NotNull Long pIndex, @NotNull T pData) {
        final long index = pIndex;
        final T data = pData;
        final int arrayIndex = (int) (pIndex / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX + 1));
        final int listIndex = (arrayIndex == 1)
                ? BigDecimal.valueOf(((pIndex / ((long) MAX_COLLECTION_INDEX + 1)) + 1))
                .divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP)
                .add(BigDecimal.ONE.negate())
                .multiply(BigDecimal.valueOf(2)).intValue()
                : (int) (pIndex / ((long) MAX_COLLECTION_INDEX + 1));
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
    @Nullable
    public T remove(@NotNull Long pIndex) {
        final int arrayIndex = (int) (pIndex / ((long) MAX_COLLECTION_INDEX * MAX_COLLECTION_INDEX + 1));
        final int listIndex = (arrayIndex == 1)
                ? BigDecimal.valueOf(((pIndex / ((long) MAX_COLLECTION_INDEX + 1)) + 1))
                .divide(BigDecimal.valueOf(2), BigDecimal.ROUND_HALF_UP)
                .add(BigDecimal.ONE.negate())
                .multiply(BigDecimal.valueOf(2)).intValue()
                : (int) (pIndex / ((long) MAX_COLLECTION_INDEX + 1));
        if (values[arrayIndex].get(listIndex) == null) return null;
        if (pIndex <= mIndex) {
            mIndex--;
        }
        return values[arrayIndex].get(listIndex).remove(collectionIndexOf(pIndex));
    }

    @NotNull
    @Override
    public LinkedList<T> lastListOf(@NotNull Long pIndex) {
        return collectionOf(pIndex);
    }

    @Override
    @NotNull
    public Long listIndexOf(@NotNull T pData) {
        return indexOf(pData.getId());
    }

    @Override
    @NotNull
    public Long lastListIndexOf(@NotNull T pData) {
        long result = NULL_INDEX;
        long index = NULL_INDEX;
        boolean gc = false;
        for (final LinkedList<LinkedList<T>> lla : values) {
            gc = true;
            for (final LinkedList<T> ll : lla) {
                for (final T data : ll) {
                    index++;
                    if (pData.equals(data)) {
                        result = index;
                    }
                    if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                        System.gc();
                    if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) {
                        gc = false;
                    }

                }
                if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                    System.gc();
                if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) {
                    gc = false;
                }
            }
            if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                System.gc();
            if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                gc = false;
        }
        if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        return result;
    }

    @NotNull
    @Override
    public DataListIterator<T, Long> dataListIteratorIterator() {
        return new DataLongListIterator<>(values[0], values[1]);
    }

    @NotNull
    @Override
    public DataListIterator<T, Long> dataListIteratorIterator(@NotNull Long pIndex) {
        DataListIterator<T, Long> result = new DataLongListIterator<>();
        long index = NULL_INDEX;
        boolean gc = false;
        for (T data : this) {
            index++;
            gc = true;
            if (index >= pIndex) {
                result.add(data);
            }
            if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                System.gc();
            if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                gc = false;
        }
        if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        return result;
    }

    @NotNull
    @Override
    public DataListIterator<T, Long> subDataListIterator(@NotNull Long pIndex1, @NotNull Long pIndex2) {
        final DataListIterator<T, Long> result = new DataLongListIterator<>();
        boolean find = false;
        long index = NULL_INDEX;
        boolean gc = false;
        for (final LinkedList<LinkedList<T>> lla : values) {
            gc = true;
            boolean gc2 = false;
            for (final LinkedList<T> ll : lla) {
                gc2 = gc;
                boolean gc3 = false;
                for (final T data : ll) {
                    index++;
                    if (index >= pIndex1 && index <= pIndex2) {
                        result.add(data);
                    } else if (index > pIndex2) {
                        find = true;
                        break;
                    }
                    gc3 = gc2;
                    if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                        System.gc();
                    if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) {
                        gc = false;
                        gc2 = false;
                        gc3 = false;
                    }
                }
                if (find) break;
                if (gc3 && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                    System.gc();
                if (gc3 && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) {
                    gc = false;
                    gc2 = false;
                }
            }
            if (find) break;
            if (gc2 && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                System.gc();
            if (gc2 && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                gc = false;
        }
        if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        return result;
    }
}
