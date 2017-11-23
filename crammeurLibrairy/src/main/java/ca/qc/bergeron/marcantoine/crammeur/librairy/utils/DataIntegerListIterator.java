package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator;

/**
 * Created by Marc-Antoine on 2017-09-18.*/



public final class DataIntegerListIterator<T extends Data<Integer>> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.DataListIterator<T, Integer> {

    protected final LinkedList<T> values;
    protected transient int mIndex = NULL_INDEX;

    private DataIntegerListIterator(LinkedList<T> pValues) {
        values = pValues;
    }

    public DataIntegerListIterator() {
        values = new LinkedList<>();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mIndex = NULL_INDEX;
    }

    @Override
    public T get(@NotNull Integer pIndex) {
        return values.get(pIndex);
    }

    @Override
    public final T set(@NotNull Integer pIndex, @Nullable T pData) {
        return values.set(pIndex,pData);
    }

    @Override
    public void add(@NotNull Integer pIndex, @Nullable T pData) {
        values.add(pIndex,pData);
    }

    @Override
    public <E extends T> void addAll(@NotNull Integer pIndex, @NotNull DataListIterator<E, Integer> pDataListIterator) {
        Parallel.For(pDataListIterator.currentCollection(), new Parallel.Operation<E>() {
            @Override
            public void perform(E pParameter) {

            }

            @Override
            public boolean follow() {
                return false;
            }
        });
    }

    @NotNull
    @Override
    public Integer indexOf(@Nullable T pData) {
        return values.indexOf(pData);
    }

    @NotNull
    @Override
    public Integer lastIndexOf(@Nullable T pData) {
        return values.lastIndexOf(pData);
    }

    @NotNull
    @Override
    public Integer indexOfKey(@Nullable Integer pKey) {
        return null;
    }

    @NotNull
    @Override
    public Integer lastIndexOfKey(@Nullable Integer pKey) {
        return null;
    }

    @NotNull
    @Override
    public final List<T> currentCollection() {
        return values;
    }

    @NotNull
    @Override
    public final Iterable<Collection<T>> allCollections() {
        return new Iterable<Collection<T>>() {
            @NotNull
            @Override
            public Iterator<Collection<T>> iterator() {
                return new Iterator<Collection<T>>() {
                    private final LinkedList<T> values = DataIntegerListIterator.this.values;
                    private transient volatile int mIndex = NULL_INDEX;
                    private transient volatile int mSize = 1;

                    @Override
                    public boolean hasNext() {
                        return mIndex + 1 < mSize;
                    }

                    @Override
                    public Collection<T> next() {
                        return values;
                    }
                };
            }
        };
    }

    @NotNull
    @Override
    public List<T> collectionOf(@NotNull Integer pIndex) {
        if (pIndex > values.size() - 1) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        return values;
    }

    @Override
    public final void add(@Nullable T pData) {
        values.add(this.currentCollectionIndex(),pData);
    }

    @Override
    public boolean addAtEnd(@Nullable T pData) {
        return values.add(pData);
    }

    @Override
    public boolean hasNext() {
        return mIndex + 1 < values.size();
    }

    @Nullable
    @Override
    public T next() {
        if (hasNext()) {
            return values.get(++mIndex);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public boolean hasPrevious() {
        return (mIndex != NULL_INDEX) && mIndex - 1 != NULL_INDEX;
    }

    @Nullable
    @Override
    public T previous() {
        if (hasPrevious()) {
            return values.get(--mIndex);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public final int nextIndex() {
        if (mIndex + 1 != Long.MAX_VALUE && mIndex + 1 < values.size()) {
            return mIndex + 1;
        } else {
            return values.size();
        }
    }

    @Override
    public final int previousIndex() {
        if (mIndex - 1 >= MIN_INDEX && mIndex != NULL_INDEX) {
            return mIndex - 1;
        } else {
            return NULL_INDEX;
        }
    }

    @Override
    public final void remove() {
        values.remove(mIndex);
    }

    @Override
    public final void set(@Nullable T pData) {
        values.set(mIndex,pData);
    }

    @Override
    public final boolean remove(@Nullable T pData) {
        return values.remove(pData);
    }

    @Override
    public <E extends T> boolean retainAll(@NotNull DataListIterator<E, Integer> pDataListIterator) {
        return false;
    }

    @Override
    public final void clear() {
        values.clear();
        mIndex = NULL_INDEX;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return null;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(@NotNull Integer pIndex) {
        return null;
    }

    @NotNull
    @Override
    public DataListIterator<T, Integer> subDataListIterator(@NotNull Integer pIndex1, @NotNull Integer pIndex2) {
        return null;
    }

    @NotNull
    @Override
    public final Integer size() {
        return values.size();
    }

    @Override
    public final boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public final int currentCollectionIndex() {
        return mIndex;
    }

    @Override
    public final int collectionIndexOf(@NotNull Integer pIndex) {
        return pIndex;
    }

    @NotNull
    @Override
    public final Iterator<T> iterator() {
        return new DataIntegerListIterator<>(values);
    }
}
