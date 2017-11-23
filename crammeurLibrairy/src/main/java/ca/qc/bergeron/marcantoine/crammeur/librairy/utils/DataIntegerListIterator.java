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
    protected transient volatile int mIndex = NULL_INDEX;

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
    public final void add(@NotNull Integer pIndex, @Nullable T pData) {
        values.add(pIndex,pData);
    }

    @Override
    public final <E extends T> void addAll(@NotNull final Integer pIndex, @NotNull DataListIterator<E, Integer> pDataListIterator) {
        Parallel.For(pDataListIterator.currentCollection(), new Parallel.Operation<E>() {
            int index = pIndex;
            @Override
            public void perform(E pParameter) {
                synchronized (values) {
                    values.add(index,pParameter);
                }
                synchronized (this) {
                    index++;
                }
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
    public Integer indexOfKey(@Nullable final Integer pKey) {
        final int[] result = new int[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            boolean follow = true;
            int index = NULL_INDEX;

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
    public Integer lastIndexOfKey(@Nullable final Integer pKey) {
        final int[] result = new int[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<T> operation = new Parallel.Operation<T>() {
            int index = NULL_INDEX;

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
    public final List<T> collectionOf(@NotNull Integer pIndex) {
        if (pIndex > values.size() - 1) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        return values;
    }

    @Override
    public final void add(@Nullable T pData) {
        values.add(mIndex,pData);
    }

    @Override
    public final boolean addAtEnd(@Nullable T pData) {
        return values.add(pData);
    }

    @Override
    public final boolean hasNext() {
        return mIndex + 1 < values.size();
    }

    @Nullable
    @Override
    public final T next() {
        if (hasNext()) {
            return values.get(++mIndex);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public final boolean hasPrevious() {
        return (mIndex != NULL_INDEX) && mIndex - 1 != NULL_INDEX;
    }

    @Nullable
    @Override
    public final T previous() {
        if (hasPrevious()) {
            return values.get(--mIndex);
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public final int nextIndex() {
        if (mIndex + 1 != Integer.MAX_VALUE && mIndex + 1 < values.size()) {
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
    public final <E extends T> boolean retainAll(@NotNull DataListIterator<E, Integer> pDataListIterator) {
        final boolean[] result = new boolean[1];
        result[0] = true;
        final DataIntegerListIterator<T> retain = new DataIntegerListIterator<>();
        for (Collection<E> collection : pDataListIterator.allCollections()) {
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

        final DataIntegerListIterator<T> delete = new DataIntegerListIterator<>();
        for (Collection<T> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<T>() {
                @Override
                public void perform(T pParameter) {
                    if (!retain.contains(pParameter)){
                        delete.addAtEnd(pParameter);
                    }
                }

                @Override
                public boolean follow() {
                    return false;
                }
            });
        }

        for (Collection<T> collection : delete.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<T>() {
                boolean follow = true;
                @Override
                public void perform(T pParameter) {
                    synchronized (result) {
                        result[0] = DataIntegerListIterator.this.remove(pParameter);
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
        values.clear();
        mIndex = NULL_INDEX;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return values.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(@NotNull Integer pIndex) {
        return values.listIterator(pIndex);
    }

    @NotNull
    @Override
    public final DataListIterator<T, Integer> subDataListIterator(@NotNull final Integer pIndex1, @NotNull final Integer pIndex2) {
        final DataListIterator<T, Integer> result = new DataIntegerListIterator<>();
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
            Parallel.For(collection, operation);
        }
        return result;
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

    @Deprecated
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
