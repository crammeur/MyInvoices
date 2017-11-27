package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataCollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public final class DataLongListIterator<T extends Data<Long>> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.DataListIterator<T, Long> {

    protected final LinkedList<LinkedList<T>>[] values = new LinkedList[2];
    protected transient volatile long mIndex = NULL_INDEX;
    protected transient volatile long mSize = 0L;

    private DataLongListIterator(@NotNull LinkedList<LinkedList<T>> pListOne, @NotNull LinkedList<LinkedList<T>> pListTwo, long pSize) {
        values[0] = pListOne;
        values[1] = pListTwo;
        mSize = pSize;
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
            long index = NULL_INDEX;

            @Override
            public void perform(T pParameter) {
                if (pParameter != null && ((pKey == null && pParameter.getId() == null) || (pKey != null && pKey.equals(pParameter.getId())))) {
                    synchronized (this) {
                        index++;
                        follow = false;
                    }
                    synchronized (result) {
                        result[0] = index;
                    }
                } else {
                    synchronized (this) {
                        index++;
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
    public final Long lastIndexOfKey(@Nullable final Long pKey) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            long index = NULL_INDEX;

            @Override
            public void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                if (pParameter != null) {
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
    public final boolean hasNext() {
        return mIndex + 1 < mSize;
    }

    @Nullable
    @Override
    public final T next() {
        if (hasNext()) {
            mIndex++;
            return actual();
        } else
            throw new NoSuchElementException();
    }

    @Override
    public final boolean hasPrevious() {
        return (mIndex != NULL_INDEX) && mIndex - 1 >= MIN_INDEX;
    }

    @Nullable
    @Override
    public final T previous() {
        if (hasPrevious()) {
            mIndex--;
            return actual();
        } else
            throw new NoSuchElementException();
    }

    @Nullable
    protected final T actual() {
        if (mIndex != NULL_INDEX && mIndex < Long.MAX_VALUE) {
            final int arrayIndex = (int) (mIndex / ((long) MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
            final int listIndex = (arrayIndex == 1)
                    ? (int) ((mIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                    : (int) (mIndex / ((long) MAX_COLLECTION_SIZE + 1));
            return values[arrayIndex].get(listIndex).get(collectionIndexOf(mIndex));
        } else
            throw new IndexOutOfBoundsException(String.valueOf(mIndex));

    }

    @Override
    public final int collectionIndexOf(@NotNull Long pIndex) {
        return (int) (pIndex % ((long) Integer.MAX_VALUE + 1));
    }

    @Override
    @NotNull
    public final LinkedList<T> currentCollection() {
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_SIZE + 1));
        return values[arrayIndex].get(listIndex);
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
                    private transient volatile long mSize = (long) values[0].size() + values[1].size();

                    @Override
                    public boolean hasNext() {
                        return mIndex + 1 < mSize;
                    }

                    @Override
                    public Collection<T> next() {
                        if (mIndex + 1 < Integer.MAX_VALUE) {
                            return values[0].get((int) (++mIndex));
                        } else {
                            return values[1].get((int) ((++mIndex) % Integer.MAX_VALUE));
                        }
                    }

                    @Override
                    public void remove() {
                        if (mIndex == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
                        if (mIndex < Integer.MAX_VALUE) {
                            values[0].remove((int) mIndex);
                        } else {
                            values[1].remove((int) (mIndex % Integer.MAX_VALUE));
                        }
                        mIndex--;
                    }

                };
            }
        };
    }

    @Override
    @NotNull
    public final LinkedList<T> collectionOf(@NotNull Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_SIZE + 1));
        return values[arrayIndex].get(listIndex);
    }

    @NotNull
    @Override
    public Long count(@Nullable final T pEntity) {
        final long[] result = new long[1];
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<T>() {
                @Override
                public void perform(T pParameter) {
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
    public final int nextIndex() {
        if (mIndex + 1 != Long.MAX_VALUE && mIndex + 1 < mSize) {
            return collectionIndexOf(mIndex + 1);
        } else {
            if (collectionIndexOf(mSize) == 0)
                return Integer.MAX_VALUE;
            else
                return collectionIndexOf(mSize);
        }
    }

    @Override
    public final int previousIndex() {
        if (mIndex != NULL_INDEX && mIndex - 1 >= MIN_INDEX) {
            return collectionIndexOf(mIndex - 1);
        } else {
            return NULL_INDEX;
        }
    }

    @Override
    public final void remove() {
        if (mIndex == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_SIZE + 1));
        values[arrayIndex].get(listIndex).remove(this.currentCollectionIndex());
        mIndex--;
    }

    @Override
    public final void set(@Nullable T t) {
        if (mIndex == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_SIZE + 1));
        values[arrayIndex].get(listIndex).set(this.currentCollectionIndex(), t);
    }

    @Override
    public final void add(@Nullable T pData) {
        long index;
        if (mIndex == NULL_INDEX)
            index = mIndex + 1;
        else
            index = mIndex;
        final int arrayIndex = (int) (index / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((index % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (index / ((long) MAX_COLLECTION_SIZE + 1));
        if (values[arrayIndex].isEmpty() || (listIndex > 0 && values[arrayIndex].get(listIndex - 1).size() == Integer.MAX_VALUE)) {
            values[arrayIndex].add(new LinkedList<T>());
        }
        values[arrayIndex].get(values[arrayIndex].size() - 1).add(collectionIndexOf(index),pData);
        mSize++;
    }

    @Override
    public boolean addAtEnd(@Nullable T pData) {
        final int arrayIndex = (int) ((mSize - 1) / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) (((mSize - 1) % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) ((mSize - 1) / ((long) MAX_COLLECTION_SIZE + 1));
        if (values[arrayIndex].isEmpty() || (listIndex > 0 && values[arrayIndex].get(listIndex - 1).size() == Integer.MAX_VALUE)) {
            values[arrayIndex].add(new LinkedList<T>());
        }
        boolean exception = false;
        try {
            return values[arrayIndex].get(values[arrayIndex].size() - 1).add(pData);
        } catch (Throwable t) {
            t.printStackTrace();
            exception = true;
            throw t;
        } finally {
            if (!exception)
                mSize++;
        }

    }

    @Override
    public final boolean remove(@Nullable final T pData) {
        final long[] index = new long[1];
        index[0] = NULL_INDEX;
        final boolean[] result = new boolean[1];

        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            boolean follow = true;

            @Override
            public void perform(final T pParameter) {
                synchronized (index) {
                    index[0]++;
                }
                synchronized (result) {
                    result[0] = (pData == pParameter || (pData != null && pData.equals(pParameter)));
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
    public final <E extends T> boolean retainAll(@NotNull CollectionIterator<E, Long> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = true;
        final DataLongListIterator<T> retain = new DataLongListIterator<T>();
        for (Collection<E> collection : pCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {

                @Override
                public void perform(E pParameter) {
                    retain.addAtEnd(pParameter);
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }

        final DataLongListIterator<T> delete = new DataLongListIterator<>();
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<T>() {
                @Override
                public void perform(T pParameter) {
                    if (!retain.contains(pParameter)) {
                        delete.addAtEnd(pParameter);
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }

        for (Collection<T> collection : delete.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<T>() {
                boolean follow = true;
                @Override
                public void perform(T pParameter) {
                    synchronized (result) {
                        result[0] = DataLongListIterator.this.remove(pParameter);
                    }
                    synchronized (this) {
                        follow = result[0];
                    }
                }

                @Override
                public boolean follow() {
                    return follow;
                }
            });
        }
        return result[0];
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
        return new DataLongListIterator<T>(values[0], values[1], mSize);
    }

    @Override
    public final <E extends T> void addAll(@NotNull final Long pIndex, @NotNull DataCollectionIterator<E, Long> pDataCollectionIterator) {
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            long index = pIndex;

            @Override
            public void perform(E pParameter) {
                final int arrayIndex = (int) (index / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
                final int listIndex = (arrayIndex == 1)
                        ? (int) ((index % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                        : (int) (index / ((long) MAX_COLLECTION_SIZE + 1));
                synchronized (values) {
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
        for (Collection<E> collection : pDataCollectionIterator.allCollections()) {
            Parallel.For(collection, operation);
        }
    }

    @Override
    @Nullable
    public final T get(@NotNull Long pIndex) {
        if (pIndex > mSize - 1) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
        //if (values[arrayIndex].get(listIndex) == null) return null;
        return values[arrayIndex].get(listIndex).get(collectionIndexOf(pIndex));
    }

    @Override
    @Nullable
    public final T set(@NotNull Long pIndex, @Nullable T pData) {
        if (pIndex > mSize - 1) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
        //if (values[arrayIndex].get(listIndex) == null) return null;
        return values[arrayIndex].get(listIndex).set(collectionIndexOf(pIndex), pData);
    }

    @Override
    public final void add(@NotNull final Long pIndex, @Nullable final T pData) {
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
        if (values[arrayIndex].get(listIndex) != null) {
            values[arrayIndex].get(listIndex).add(collectionIndexOf(pIndex), pData);
        } else {
            values[arrayIndex].add(listIndex, new LinkedList<T>() {{
                add(collectionIndexOf(pIndex), pData);
            }});
        }
        if (pIndex <= this.mIndex) this.mIndex++;
    }

    @Override
    @NotNull
    public final Long indexOf(@Nullable final T pData) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            long index = NULL_INDEX;
            boolean follow = true;

            @Override
            public void perform(T pParameter) {
                if ((pData == pParameter) || (pData != null && pData.equals(pParameter))) {
                    synchronized (this) {
                        index++;
                        follow = false;
                    }
                    synchronized (result) {
                        result[0] = index;
                    }
                } else {
                    synchronized (this) {
                        index++;
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
        }
        return result[0];
    }

    @Override
    @NotNull
    public final Long lastIndexOf(@Nullable final T pData) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            long index = NULL_INDEX;

            @Override
            public void perform(T pParameter) {
                synchronized (this) {
                    index++;
                }
                if ((pData == pParameter) || (pData != null && pData.equals(pParameter))) {
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

    @NotNull
    @Override
    public final DataListIterator<T, Long> listIterator() {
        return new DataLongListIterator<>(values[0], values[1],mSize);
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
                    synchronized (result) {
                        result.addAtEnd(pParameter);
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
                    synchronized (result) {
                        result.addAtEnd(pParameter);
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
            Parallel.For(collection, operation);
        }
        return result;
    }
}
