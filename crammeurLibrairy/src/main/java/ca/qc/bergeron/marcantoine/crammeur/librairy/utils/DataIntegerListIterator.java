package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataCollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public final class DataIntegerListIterator<T extends Data<Integer>> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.DataListIterator<T, Integer> {

    protected final LinkedList<T> values;
    protected transient int mIndex = NULL_INDEX;

    private DataIntegerListIterator(LinkedList<T> pValues) {
        values = pValues;
    }

    public DataIntegerListIterator(Iterable<T> pIterable) {
        this(pIterable,false);

    }

    public DataIntegerListIterator(@NotNull Iterable<T> pIterable, final boolean pAsync) {
        values = new LinkedList<T>();
        Parallel.For(pIterable, new Parallel.Operation<T, Void>() {
            @Override
            public Void perform(T pParameter) {
                synchronized (values) {
                    values.add(pParameter);
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
    }

    public DataIntegerListIterator() {
        values = new LinkedList<T>();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mIndex = NULL_INDEX;
    }

    @Override
    public final boolean hasNext() {
        final int nextIndex = nextIndex();
        return nextIndex <= values.size() && nextIndex != NULL_INDEX;
    }

    @Override
    public synchronized final T next() {
        if (hasNext()) {
            mIndex++;
            return values.get(mIndex);
        } else
            return null;
    }

    @Override
    public final boolean hasPrevious() {
        if (mIndex == NULL_INDEX) return false;
        return previousIndex() != NULL_INDEX;
    }

    @Override
    public synchronized final T previous() {
        if (hasPrevious()) {
            mIndex--;
            return values.get(mIndex);
        } else
            return null;

    }

    @Override
    public final int nextIndex() {
        if (mIndex != MAX_COLLECTION_INDEX) {
            return mIndex + 1;
        } else
            return MIN_INDEX;

    }

    @Override
    public final int previousIndex() {
        if (mIndex != MIN_INDEX) {
            return mIndex - 1;
        } else
            return MAX_COLLECTION_INDEX;
    }

    @Override
    public final void remove() {
        values.remove(mIndex);
    }

    @Override
    public final void set(@NotNull T e) {
        if (mIndex == NULL_INDEX) throw new IndexOutOfBoundsException(String.valueOf(NULL_INDEX));
        values.set(mIndex, e);
    }

    @Override
    public final Integer size() {
        return values.size();
    }

    @Override
    public final boolean isEmpty() {
        return this.size() == 0;
    }

    @NotNull
    @Override
    public final Integer indexOf(@Nullable final Integer pKey) {
        int result = NULL_INDEX;
        boolean gc = false;
        for (Collection<T> collecttion : this.allCollections()) {
            gc = true;
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
            if (!iterator.hasNext()) {
                result = iterator.next();
            }
            if (result != NULL_INDEX) {
                break;
            }
        }
        if (gc && Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        return result;
    }

    @Override
    public int currentCollectionIndex() {
        return mIndex;
    }

    @Override
    public int collectionIndexOf(@NotNull Integer pIndex) {
        return pIndex;
    }

    @NotNull
    @Override
    public LinkedList<T> currentCollection() {
        return new LinkedList<>(values);
    }

    @NotNull
    @Override
    public Iterable<Collection<T>> allCollections() {
        List<Collection<T>> result = new LinkedList<>();
        result.add(values);
        return result;
    }

    @NotNull
    @Override
    @Deprecated
    public LinkedList<T> collectionOf(@NotNull Integer pKey) {
        return new LinkedList<>(values);
    }

    @Override
    public boolean contains(@NotNull T o) {
        return values.contains(o);
    }

    @Override
    public void add(@NotNull T e) {
        values.add(e);
    }

    @Override
    public boolean remove(@NotNull T o) {
        return values.remove(o);
    }

    @Override
    public void clear() {
        values.clear();
        mIndex = NULL_INDEX;
    }

    @Override
    public final Iterator<T> iterator() {
        return new DataIntegerListIterator<T>(values);
    }

    @Override
    public boolean retainAll(@NotNull Iterable<? extends T> pIterable) {
        final DataCollectionIterator<T, Integer> c = new DataIntegerListIterator<T>();
        for (final T data : pIterable) {
            c.add(data);
        }
        boolean result = true;
        for (final T data : this) {
            if (!c.contains(data) && !this.remove(data)) result = false;
            if (!result) break;
        }
        if (Runtime.getRuntime().maxMemory() / 2 < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        return result;
    }

    @Override
    public final boolean addAll(@NotNull Integer pIndex, @NotNull Iterable<? extends T> pIterable) {
        boolean result = true;
        for (T data : pIterable) {
            this.add(data);
            if (!this.contains(data)) result = false;
            if (!result) break;
        }
        if (Runtime.getRuntime().maxMemory() / 2 < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        return result;
    }

    @Override
    public final boolean equals(@NotNull DataListIterator<T, Integer> pDataListIterator) {
        if (this.equals((Object)pDataListIterator)) return true;
        boolean result = true;
        Integer size = 0;
        boolean gc = false;
        for (T data : this) {
            gc = true;
            size++;
            if (!pDataListIterator.contains(data)) result = false;
            if (!result) break;
            if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                System.gc();
            if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                gc = false;
        }
        if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        Integer size2 = 0;
        if (result) {
            gc = false;
            for (T data : pDataListIterator) {
                gc = (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                size2++;
                if (!this.contains(data)) result = false;
                if (!result || size2 > size) break;
                if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                    System.gc();
                if ((Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                    gc = false;

            }
            if (gc && (Runtime.getRuntime().maxMemory() * PERCENT_MEMORY_MAX) < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                System.gc();
        }
        return result && size.equals(size2);
    }

    @Override
    public T get(@NotNull Integer pIndex) {
        return values.get(pIndex);
    }

    @Override
    public T set(@NotNull Integer pIndex1, @NotNull T pIndex2) {
        return values.set(pIndex1, pIndex2);
    }

    @Override
    public void add(@NotNull Integer pIndex, @NotNull T pData) {
        values.add(pIndex, pData);
    }

    @Override
    public T remove(@NotNull Integer pIndex) {
        return values.remove((int) pIndex);
    }

    @Override
    @NotNull
    public LinkedList<T> lastListOf(@NotNull Integer pData) {
        return new LinkedList<>(values);
    }

    @Override
    public Integer listIndexOf(@NotNull T pData) {
        return values.indexOf(pData);
    }

    @Override
    public Integer lastListIndexOf(@NotNull T pData) {
        return values.lastIndexOf(pData);
    }


    @Override
    @NotNull
    public DataListIterator<T, Integer> dataListIteratorIterator() {
        return new DataIntegerListIterator<>(values);
    }

    @Override
    @NotNull
    public DataListIterator<T, Integer> dataListIteratorIterator(@NotNull Integer pIndex) {
        final DataListIterator<T, Integer> result = new DataIntegerListIterator<>();
        {
            int index = NULL_INDEX;
            final ListIterator<T> li = values.listIterator(pIndex);
            while (li.hasNext()) {
                index++;
                if (index >= pIndex) {
                    result.add(li.next());
                }
            }
        }
        if (Runtime.getRuntime().maxMemory() / 2 < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        return result;
    }

    @Override
    @NotNull
    public DataListIterator<T, Integer> subDataListIterator(@NotNull Integer pIndex1, @NotNull Integer pIndex2) {
        final DataListIterator<T, Integer> result = new DataIntegerListIterator<>();
        for (T data : values.subList(pIndex1, pIndex2)) {
            result.add(data);
        }
        if (Runtime.getRuntime().maxMemory() / 2 < Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
            System.gc();
        return result;
    }
}
