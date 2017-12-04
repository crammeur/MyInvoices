package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataListIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator;

/**
 * Created by Marc-Antoine on 2017-09-18.*/



public final class DataIntegerListIterator<T extends Data<Integer>> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator<T, Integer> implements DataListIterator<T,Integer> {

    protected final LinkedList<T> values;
    protected transient volatile int mIndex = NULL_INDEX;

    public DataIntegerListIterator() {
        values = new LinkedList<T>() {

            @Override
            public boolean addAll(@NotNull Collection<? extends T> c) {
                final boolean[] result = new boolean[1];
                final Collection<T> collection = this;
                result[0] = !c.isEmpty();
                Parallel.For(c, new Parallel.Operation<T>() {
                    @Override
                    public void perform(T pParameter) {
                        synchronized (result) {
                            synchronized (collection) {
                                result[0] = collection.add(pParameter);
                            }
                        }
                    }

                    @Override
                    public boolean follow() {
                        return result[0];
                    }
                });
                return result[0];
            }

            @Override
            public boolean addAll(int index, Collection<? extends T> c) {
                final boolean[] result = new boolean[1];
                final int[] index2 = new int[1];
                final List<T> list = this;
                result[0] = !c.isEmpty();
                index2[0] = index;
                Parallel.For(c, new Parallel.Operation<T>() {
                    @Override
                    public void perform(T pParameter) {
                        synchronized (result) {
                            try {
                                synchronized (list) {
                                    list.add(index2[0], pParameter);
                                }
                            } catch (Throwable t) {
                                result[0] = false;
                                throw t;
                            }
                        }
                        if (result[0]) {
                            synchronized (index2) {
                                index2[0]++;
                            }
                        }
                    }

                    @Override
                    public boolean follow() {
                        return result[0];
                    }
                });
                return result[0];
            }

            @Override
            public int indexOf(final Object o) {
                final int[] result = new int[1];
                result[0] = -1;
                Parallel.For(this, new Parallel.Operation<T>() {
                    int index = 0;
                    @Override
                    public void perform(T pParameter) {
                        if ((o == null && pParameter == null) || (pParameter != null && pParameter.equals(o))) {
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
                        return result[0] == -1;
                    }
                });
                return result[0];
            }

            @Override
            public int lastIndexOf(final Object o) {
                final int[] result = new int[1];
                result[0] = -1;
                Parallel.For(this, new Parallel.Operation<T>() {
                    int index = 0;
                    @Override
                    public void perform(T pParameter) {
                        if ((o == null && pParameter == null) || (pParameter != null && pParameter.equals(o))) {
                            synchronized (result) {
                                result[0] = index;
                            }
                        }
                        synchronized (this) {
                            index++;
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
            public boolean contains(final Object o) {
                final boolean[] result = new boolean[1];
                Parallel.For(this, new Parallel.Operation<T>() {
                    @Override
                    public void perform(T pParameter) {
                        if ((o == null && pParameter == null) || (pParameter != null && pParameter.equals(o))) {
                            synchronized (result) {
                                result[0] = true;
                            }
                        }
                    }

                    @Override
                    public boolean follow() {
                        return !result[0];
                    }
                });
                return result[0];
            }

            @SuppressWarnings("SuspiciousMethodCalls")
            @Override
            public boolean containsAll(@NotNull Collection<?> c) {
                final boolean[] result = new boolean[1];
                final Collection<T> collection = this;
                result[0] = !c.isEmpty();
                Parallel.For(c, new Parallel.Operation<Object>() {
                    @Override
                    public void perform(Object pParameter) {
                        synchronized (result) {
                            synchronized (collection) {
                                result[0] = collection.contains(pParameter);
                            }
                        }
                    }

                    @Override
                    public boolean follow() {
                        return result[0];
                    }
                });
                return result[0];
            }

            @SuppressWarnings("SuspiciousMethodCalls")
            @Override
            public boolean removeAll(Collection<?> c) {
                final boolean[] result = new boolean[1];
                result[0] = !c.isEmpty();
                final Collection<T> collection = this;
                Parallel.For(c, new Parallel.Operation<Object>() {
                    @Override
                    public void perform(Object pParameter) {
                        synchronized (result) {
                            synchronized (collection) {
                                result[0] = collection.remove(pParameter);
                            }
                        }
                    }

                    @Override
                    public boolean follow() {
                        return result[0];
                    }
                });
                return result[0];
            }

            @Override
            public boolean retainAll(final Collection<?> c) {
                final boolean[] result = new boolean[1];
                final Collection<T> delete = new LinkedList<>();
                result[0] = !c.isEmpty();
                Parallel.For(this, new Parallel.Operation<T>() {
                    @Override
                    public void perform(T pParameter) {
                        if (!c.contains(pParameter)) {
                            synchronized (result) {
                                synchronized (delete) {
                                    result[0] = delete.add(pParameter);
                                }
                            }
                        }
                    }

                    @Override
                    public boolean follow() {
                        return result[0];
                    }
                });
                return result[0] && this.removeAll(delete);
            }
        };
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mIndex = NULL_INDEX;
    }

    @Override
    public final T get(@NotNull Integer pIndex) {
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
    public final <E extends T> void addAll(@NotNull final Integer pIndex, @NotNull CollectionIterator<E, Integer> pDataCollectionIterator) {
        Parallel.For(pDataCollectionIterator.currentCollection(), new Parallel.Operation<E>() {
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
                return true;
            }
        });
    }

    @Override
    public final T remove(@NotNull Integer pIndex) {
        return values.remove((int)pIndex);
    }

    @NotNull
    @Override
    public final Integer indexOf(@Nullable T pData) {
        return values.indexOf(pData);
    }

    @NotNull
    @Override
    public final Integer lastIndexOf(@Nullable T pData) {
        return values.lastIndexOf(pData);
    }

    @NotNull
    @Override
    public final Integer indexOfKey(@Nullable final Integer pKey) {
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
        Parallel.For(this.currentCollection(), operation);
        return result[0];
    }

    @NotNull
    @Override
    public final Integer lastIndexOfKey(@Nullable final Integer pKey) {
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
        Parallel.For(this.currentCollection(), operation);
        return result[0];
    }

    @NotNull
    @Override
    public final List<T> currentCollection() {
        return values;
    }

    /**
     * Use currentCollection
     * @return
     */
    //@SuppressWarnings("unchecked")
    @Deprecated
    @NotNull
    @Override
    public Iterable<List<T>> allCollections() {
        return new Iterable<List<T>>() {
            @NotNull
            @Override
            public Iterator<List<T>> iterator() {
                return new Iterator<List<T>>() {
                    private final LinkedList<T> values = DataIntegerListIterator.this.values;
                    private transient volatile int mIndex = NULL_INDEX;
                    private transient volatile int mSize = 1;

                    @Override
                    public boolean hasNext() {
                        return mIndex + 1 < mSize;
                    }

                    @Override
                    public List<T> next() {
                        return values;
                    }
                };
            }
        };
    }

    /**
     * Use currentCollection
     * @param pIndex
     * @return
     */
    @Deprecated
    @NotNull
    @Override
    public final List<T> collectionOf(@NotNull Integer pIndex) {
        if (pIndex < MIN_INDEX || pIndex > values.size() - 1) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        return currentCollection();
    }

    @NotNull
    @Override
    public final Integer count(@Nullable final T pEntity) {
        final int[] result = new int[1];
        Parallel.For(values, new Parallel.Operation<T>() {
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
        return result[0];
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
    public final <E extends T> boolean retainAll(@NotNull CollectionIterator<E, Integer> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = true;
        final DataIntegerListIterator<T> retain = new DataIntegerListIterator<>();
        for (Collection<E> collection : pCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {
                @Override
                public void perform(E pParameter) {
                    if (!retain.addAtEnd(pParameter)) throw new RuntimeException("The value has not been added");
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }

        final DataIntegerListIterator<T> delete = new DataIntegerListIterator<>();
        Parallel.For(this.currentCollection(), new Parallel.Operation<T>() {
            @Override
            public void perform(T pParameter) {
                if (!retain.contains(pParameter)){
                    if (!delete.addAtEnd(pParameter)) throw new RuntimeException("The value has not been added");
                }
            }

            @Override
            public boolean follow() {
                return false;
            }
        });

        Parallel.For(this.currentCollection(), new Parallel.Operation<T>() {

            @Override
            public void perform(T pParameter) {
                synchronized (result) {
                    result[0] = DataIntegerListIterator.this.remove(pParameter);
                }
            }

            @Override
            public boolean follow() {
                return result[0];
            }
        });

        return result[0];
    }

    @Override
    public final void clear() {
        values.clear();
        mIndex = NULL_INDEX;
    }

    @NotNull
    @Override
    public final java.util.ListIterator listIterator() {
        return values.listIterator();
    }

    @NotNull
    @Override
    public final java.util.ListIterator listIterator(@NotNull Integer pIndex) {
        return values.listIterator(pIndex);
    }

    @NotNull
    @Override
    public final ListIterator<T, Integer> subDataListIterator(@NotNull final Integer pIndex1, @NotNull final Integer pIndex2) {
        final ListIterator<T, Integer> result = new DataIntegerListIterator<>();
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
        Parallel.For(this.currentCollection(), operation);
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

    /**
     * Return pIndex
     * @param pIndex Index
     * @return pIndex
     */
    @Deprecated
    @Override
    public final int collectionIndexOf(@NotNull Integer pIndex) {
        return pIndex;
    }
}
