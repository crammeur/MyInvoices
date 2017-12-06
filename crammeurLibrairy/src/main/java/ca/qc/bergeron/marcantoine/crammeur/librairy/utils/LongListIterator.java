package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public class LongListIterator<E> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator<E, Long> {

    @SuppressWarnings("unchecked")
    private final LinkedList<LinkedList<E>>[] values = new LinkedList[2];
    private transient volatile long mIndex = NULL_INDEX;
    private transient volatile long mSize = 0L;

    public LongListIterator() {
        values[0] = new LinkedList<LinkedList<E>>();
        values[1] = new LinkedList<LinkedList<E>>();
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
    public final int collectionSize() {
        return this.collectionSizeOf(mIndex);
    }

    @Override
    public final boolean isEmpty() {
        return mSize == 0;
    }

    @Override
    public final int collectionIndex() {
        return this.collectionIndexOf(mIndex);
    }

    @Override
    public final int collectionIndexOf(@NotNull Long pIndex) {
        return (int) (pIndex % ((long) Integer.MAX_VALUE + 1));
    }

    @Override
    public final boolean hasNext() {
        return mIndex + 1 < mSize;
    }

    @Nullable
    @Override
    public final E next() throws NoSuchElementException {
        try {
            if (mIndex == NULL_INDEX)
                mIndex = MIN_INDEX;
            else if (mIndex + 1 <= mSize && mIndex + 1 > MIN_INDEX)
                mIndex++;
            else
                mIndex = mSize;

            return actual();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new NoSuchElementException();
        }
    }

    @Nullable
    public final E actual() throws IndexOutOfBoundsException {
        final int arrayIndex = (int) (mIndex / ((long) MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_SIZE + 1));
        return values[arrayIndex].get(listIndex).get(collectionIndexOf(mIndex));

    }

    @Override
    public final boolean hasPrevious() {
        return (mIndex != NULL_INDEX) && mIndex - 1 >= MIN_INDEX;
    }

    @Nullable
    @Override
    public final E previous() throws NoSuchElementException {
        try {
            if (mIndex - 1 >= MIN_INDEX)
                mIndex--;
            else
                mIndex = NULL_INDEX;

            return actual();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new NoSuchElementException();
        }
    }

    @Override
    public boolean hasNextCollection() {
        return mIndex + MAX_COLLECTION_SIZE < mSize && mIndex + MAX_COLLECTION_SIZE > MIN_INDEX;
    }

    @Override
    @NotNull
    public final List<E> nextCollection() throws NoSuchElementException {
        try {
            if (mIndex == NULL_INDEX)
                mIndex = MIN_INDEX;
            else
                mIndex += MAX_COLLECTION_SIZE;

            return actualCollection();
        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public final List<E> actualCollection() throws IndexOutOfBoundsException {
        return collectionOf(mIndex);
    }

    @Override
    public boolean hasPreviousCollection() {
        return mIndex - MAX_COLLECTION_SIZE >= MIN_INDEX;
    }

    @NotNull
    @Override
    public final List<E> previousCollection() throws NoSuchElementException {
        try {
            if (mIndex - MAX_COLLECTION_SIZE >= MIN_INDEX)
                mIndex -= MAX_COLLECTION_SIZE;
            else
                mIndex = NULL_INDEX;

            return actualCollection();
        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public final Iterable<List<E>> allCollections() {
        return new Iterable<List<E>>() {
            @NotNull
            @Override
            public Iterator<List<E>> iterator() {
                try {
                    return new Iterator<List<E>>() {

                        private final LongListIterator<E> values;

                        {
                            final java.util.Map<Field,Object> map = new HashMap<>();
                            map.put(LongListIterator.this.getClass().getDeclaredField("values"),
                                    LongListIterator.this.values.clone());
                            map.put(LongListIterator.this.getClass().getDeclaredField("mSize"),
                                    LongListIterator.this.mSize);
                            values =  ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object.createObject((Class<? extends LongListIterator<E>>) LongListIterator.this.getClass(),map);
                        }

                        @Override
                        public final boolean hasNext() {
                            return (values.mIndex == NULL_INDEX)
                                    ?MIN_INDEX < values.mSize
                                    :values.mIndex + MAX_COLLECTION_SIZE < values.mSize && mIndex + MAX_COLLECTION_SIZE > MIN_INDEX;
                        }

                        @Override
                        public final List<E> next() {
                            if (values.mIndex == NULL_INDEX) {
                                values.mIndex = MIN_INDEX;
                            } else {
                                values.mIndex += MAX_COLLECTION_SIZE;
                            }
                            return new List<E>() {

                                private ArrayList<E> currentCollection = new ArrayList<>(values.collectionOf(values.mIndex));

                                @Override
                                public final boolean addAll(final int pIndex, @NotNull final Collection<? extends E> c) {
                                    if (pIndex < 0 || pIndex > this.size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
                                    final boolean[] result = new boolean[1];
                                    final int[] index = new int[1];
                                    index[0] = pIndex;
                                    result[0] = !c.isEmpty();
                                    final List<E> collection = this;
                                    Parallel.For(c, new Parallel.Operation<E>() {
                                        @Override
                                        public void perform(E pParameter) {
                                            synchronized (collection) {
                                                collection.add(index[0],pParameter);
                                            }
                                            synchronized (index) {
                                                index[0]++;
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
                                public final E get(final int index) {
                                    return currentCollection.get(index);
                                }

                                @Override
                                public final E set(final int index, final E element) {
                                    final E result2 = currentCollection.set(index,element);
                                    final E result = values.get(((values.mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                                    if ((result == null && result2 == null) || (result != null && result.equals(result2))) {
                                        final E result3 = values.set(((values.mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, element);
                                        if ((result == null && result3 != null) || (result != null && !result.equals(result3))) {
                                            //Rollback
                                            values.set(((values.mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, result3);
                                            throw new RuntimeException("The values are not the same");
                                        }
                                        return result;
                                    } else {
                                        throw new RuntimeException("The values are not the same");
                                    }
                                }

                                @Override
                                public final void add(final int index, final E element) {
                                    if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                                    currentCollection.add(index,element);
                                    values.add((values.mIndex/MAX_COLLECTION_SIZE)*MAX_COLLECTION_SIZE+index,element);
                                }

                                @Override
                                public final E remove(final int index) {
                                    if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                                    final E result2 = currentCollection.remove(index);
                                    final long index2 = ((values.mIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index;
                                    final E result = values.get(index2);
                                    if ((result == null && result2 != null) || (result != null && !result.equals(result2))) {
                                        throw new RuntimeException("The values are not the same");
                                    } else {
                                        final E result3 = values.remove(index2);
                                        if ((result == null && result3 != null) || (result != null && !result.equals(result3))) {
                                            values.add(index2,result3);
                                            throw new RuntimeException("The values are not the same");
                                        }
                                    }
                                    return result;
                                }

                                @Override
                                public final int indexOf(final Object o) {
                                    final int[] result = new int[1];
                                    result[0] = -1;
                                    Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                                        int index = -1;

                                        @Override
                                        public void perform(E pParameter) {
                                            synchronized (this) {
                                                index++;
                                            }
                                            if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
                                                synchronized (result) {
                                                    result[0] = index;
                                                }
                                            }
                                        }

                                        @Override
                                        public boolean follow() {
                                            return result[0] == -1;
                                        }
                                    };
                                    Parallel.For(this, operation);
                                    return result[0];
                                }

                                @Override
                                public final int lastIndexOf(final Object o) {
                                    final int[] result = new int[1];
                                    result[0] = -1;
                                    Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                                        int index = -1;

                                        @Override
                                        public void perform(E pParameter) {
                                            synchronized (this) {
                                                index++;
                                            }
                                            if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
                                    Parallel.For(this, operation);
                                    return result[0];
                                }

                                @NotNull
                                @Override
                                public final java.util.ListIterator<E> listIterator() {
                                    return currentCollection.listIterator();
                                }

                                @NotNull
                                @Override
                                public final java.util.ListIterator<E> listIterator(final int index) {
                                    return currentCollection.listIterator(index);
                                }

                                @NotNull
                                @Override
                                public final List<E> subList(final int fromIndex, final int toIndex) {
                                    return currentCollection.subList(fromIndex, toIndex);
                                }

                                @Override
                                public final int size() {
                                    return currentCollection.size();
                                }

                                @Override
                                public final boolean isEmpty() {
                                    return currentCollection.isEmpty();
                                }

                                @Override
                                public final boolean contains(final Object o) {
                                    final boolean[] result = new boolean[1];
                                    Parallel.For(this, new Parallel.Operation<E>() {
                                        @Override
                                        public void perform(E pParameter) {
                                            if ((o == null && pParameter == null) || (pParameter != null && pParameter.equals(o)))
                                            synchronized (result) {
                                            }
                                        }

                                        @Override
                                        public boolean follow() {
                                            return !result[0];
                                        }
                                    });
                                    return result[0];
                                }

                                @NotNull
                                @Override
                                public final Iterator<E> iterator() {
                                    return currentCollection.iterator();
                                }

                                @NotNull
                                @Override
                                public final Object[] toArray() {
                                    return currentCollection.toArray();
                                }

                                @NotNull
                                @Override
                                public final <T1> T1[] toArray(@NotNull final T1[] a) {
                                    return currentCollection.toArray(a);
                                }

                                @Override
                                public final boolean add(final E e) {
                                    if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The collection is full");
                                    boolean result = currentCollection.add(e);
                                    if (result) {
                                        result = values.addAtEnd(e);
                                        if (!result)
                                            if (!currentCollection.remove(currentCollection.size()-1).equals(e)) {
                                                throw new RuntimeException("The values are not the same");
                                            }
                                    }
                                    return result;
                                }

                                @SuppressWarnings("unchecked")
                                @Override
                                public final boolean remove(final Object o) {
                                    boolean result;
                                    if (result = currentCollection.remove(o)) {
                                        if ((((values.mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) != (((values.mSize-1) / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE)) ) {
                                            result = values.remove((E) o);
                                            if (!result) throw new RuntimeException("The value has not been removed");
                                            currentCollection = new ArrayList<>(values.collectionOf(values.mIndex));
                                        }
                                    }
                                    return result;
                                }

                                @SuppressWarnings("SuspiciousMethodCalls")
                                @Override
                                public final boolean containsAll(@NotNull Collection<?> c) {
                                    final boolean[] result = new boolean[1];
                                    result[0] = !c.isEmpty();
                                    final Collection<E> collection = this;
                                    Parallel.For(c, new Parallel.Operation<Object>() {
                                        @Override
                                        public void perform(Object pParameter) {
                                            synchronized (result) {
                                                result[0] = collection.contains(pParameter);
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
                                public final boolean addAll(@NotNull Collection<? extends E> c) {
                                    final boolean[] result = new boolean[1];
                                    result[0] = !c.isEmpty();
                                    final Collection<E> collection = this;
                                    Parallel.For(c, new Parallel.Operation<E>() {
                                        @Override
                                        public void perform(E pParameter) {
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

                                @SuppressWarnings("SuspiciousMethodCalls")
                                @Override
                                public final boolean removeAll(@NotNull Collection<?> c) {
                                    final boolean[] result = new boolean[1];
                                    result[0] = !c.isEmpty();
                                    final Collection<E> collection = this;
                                    Parallel.For(c, new Parallel.Operation<Object>() {
                                        @Override
                                        public void perform(Object pParameter) {
                                            synchronized (result) {
                                                result[0] = collection.remove(pParameter);
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
                                public final boolean retainAll(@NotNull final Collection<?> c) {
                                    final boolean[] result = new boolean[1];
                                    result[0] = !c.isEmpty();
                                    final Collection<E> removes = new ArrayList<>(c.size());
                                    Parallel.For(this, new Parallel.Operation<E>() {
                                        @Override
                                        public void perform(E pParameter) {
                                            if (!c.contains(pParameter)) {
                                                synchronized (result) {
                                                    result[0] = removes.add(pParameter);
                                                }
                                            }
                                        }

                                        @Override
                                        public boolean follow() {
                                            return result[0];
                                        }
                                    });
                                    return result[0] && this.removeAll(removes);
                                }

                                @Override
                                public final void clear() {
                                    if (!this.isEmpty()) {
                                        if (!values.isEmpty()) {
                                            {
                                                final ArrayList<E> arrayList = new ArrayList<>(currentCollection);
                                                currentCollection.clear();
                                                final java.util.Map<Long, E> rollback = new TreeMap<>();
                                                try {
                                                    Parallel.For(arrayList, new Parallel.Operation<E>() {
                                                        final long startIndex = values.mIndex;
                                                        long index = 0;
                                                        @Override
                                                        public void perform(E pParameter) {
                                                            final E delete;
                                                            synchronized (values) {
                                                                delete = values.remove((startIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE);
                                                            }
                                                            synchronized (rollback) {
                                                                rollback.put(((startIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, delete);
                                                            }
                                                            if ((delete == null && pParameter != null) || (delete != null && !delete.equals(pParameter))) {
                                                                throw new RuntimeException("The values are not the same");
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
                                                } catch (Throwable t) {
                                                    Parallel.For(rollback.keySet(), new Parallel.Operation<Long>() {
                                                        @Override
                                                        public void perform(Long pParameter) {
                                                            synchronized (values) {
                                                                values.add(pParameter, rollback.get(pParameter));
                                                            }
                                                        }

                                                        @Override
                                                        public boolean follow() {
                                                            return true;
                                                        }
                                                    });
                                                    t.printStackTrace();
                                                    throw t;
                                                }
                                            }
                                            if (((values.mIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) < (((values.mSize-1) / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE)) {
                                                currentCollection = new ArrayList<>(values.collectionOf(values.mIndex));
                                                final Runtime runtime = Runtime.getRuntime();
                                                if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                                                    System.gc();
                                            }
                                        } else {
                                            currentCollection.clear();
                                        }
                                    }
                                }
                            };
                        }

                        @Override
                        public void remove() {
                            if (values.mIndex < MIN_INDEX) throw new IllegalStateException(String.valueOf(values.mIndex));
                            final boolean[] error = new boolean[1];
                            Parallel.For(new ArrayList<>(values.collectionOf(values.mIndex)), new Parallel.Operation<E>() {
                                @Override
                                public void perform(E pParameter) {
                                    synchronized (error) {
                                        error[0] = !values.remove(pParameter);
                                    }
                                }

                                @Override
                                public boolean follow() {
                                    return !error[0];
                                }
                            });
                            if (error[0]) throw new RuntimeException("The value has not been removed");
                        }

                    };
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    @NotNull
    public final List<E> collectionOf(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
        return new List<E>() {

            //Performance array list
            private ArrayList<E> currentCollection = new ArrayList<>(values[arrayIndex].get(listIndex));

            @Override
            public final int size() {
                return currentCollection.size();
            }

            @Override
            public final boolean isEmpty() {
                return currentCollection.isEmpty();
            }

            @Override
            public final boolean contains(final Object o) {
                final boolean[] result = new boolean[1];
                Parallel.For(this, new Parallel.Operation<E>() {
                    @Override
                    public void perform(E pParameter) {
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

            @NotNull
            @Override
            public final Iterator<E> iterator() {
                return currentCollection.iterator();
            }

            @NotNull
            @Override
            public final Object[] toArray() {
                return currentCollection.toArray();
            }

            @NotNull
            @Override
            public final <T1> T1[] toArray(@NotNull T1[] a) {
                return currentCollection.toArray(a);
            }

            @Override
            public final boolean add(E e) {
                if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
                boolean result = currentCollection.add(e);
                if (result) {
                    result = LongListIterator.this.addAtEnd(e);
                }
                return result;
            }

            @SuppressWarnings("unchecked")
            @Override
            public final boolean remove(final Object o) {
                final int index = this.indexOf(o);
                boolean result = index != -1;
                if (result) {
                    final E data = LongListIterator.this.get(((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                    if (result = ((o == null && data == null) || (data != null && data.equals(o)))) {
                        result = currentCollection.remove(data);
                        if (result) {
                            final E data2 = LongListIterator.this.remove(((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                            if ((result = ((o == null && data2 == null) || (data2 != null && data2.equals(o)))) &&
                                    ((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index != (((mSize-1)/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index) {
                                final int arrayIndex2 = (int) (pIndex / ((long) MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
                                final int listIndex2 = (arrayIndex2 == 1)
                                        ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                                        : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
                                currentCollection = new ArrayList<>(values[arrayIndex2].get(listIndex2));
                                final Runtime runtime = Runtime.getRuntime();
                                if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f) System.gc();
                            } else if (!result) {
                                //Rollback
                                LongListIterator.this.add(((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, data2);
                                currentCollection.add(index, (E) o);
                                result = false;
                            }
                        }
                    }
                }
                return result;
            }

            @SuppressWarnings("SuspiciousMethodCalls")
            @Override
            public final boolean containsAll(@NotNull Collection<?> c) {
                final boolean[] result = new boolean[1];
                result[0] = !c.isEmpty();
                final Collection<E> collection = this;
                Parallel.For(c, new Parallel.Operation<Object>() {
                    @Override
                    public void perform(Object pParameter) {
                        synchronized (result) {
                            result[0] = collection.contains(pParameter);
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
            public final boolean addAll(@NotNull Collection<? extends E> c) {
                final boolean[] result = new boolean[1];
                result[0] = !c.isEmpty();
                final Collection<E> collection = this;
                Parallel.For(c, new Parallel.Operation<E>() {
                    @Override
                    public void perform(E pParameter) {
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
            public final boolean addAll(final int index, @NotNull Collection<? extends E> c) {
                if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                final boolean[] result = new boolean[1];
                final int[] index2 = new int[1];
                index2[0] = index;
                result[0] = !c.isEmpty();
                final List<E> list = this;
                Parallel.For(c, new Parallel.Operation<E>() {
                    @Override
                    public void perform(E pParameter) {
                        synchronized (list) {
                            list.add(index2[0],pParameter);
                        }
                        synchronized (index2) {
                            index2[0]++;
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
            public final boolean removeAll(@NotNull final Collection<?> c) {
                final boolean[] result = new boolean[1];
                final Collection<E> collection = this;
                result[0] = !c.isEmpty();
                Parallel.For(c, new Parallel.Operation<Object>() {
                    @Override
                    public void perform(Object pParameter) {
                        synchronized (result) {
                            //No sync for collection for performance
                            result[0] = collection.remove(pParameter);
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
            public boolean retainAll(@NotNull final Collection<?> c) {
                final boolean[] result = new boolean[1];
                result[0] = !c.isEmpty();
                final Collection<E> removes = new ArrayList<>(c.size());
                Parallel.For(this, new Parallel.Operation<E>() {
                    @Override
                    public void perform(E pParameter) {
                        if (!c.contains(pParameter)) {
                            synchronized (result) {
                                //No sync no order necessary
                                result[0] = removes.add(pParameter);
                            }
                        }
                    }

                    @Override
                    public boolean follow() {
                        return result[0];
                    }
                });
                return result[0] && this.removeAll(removes);
            }

            @Override
            public final void clear() {
                if (!this.isEmpty()) {
                    if (!LongListIterator.this.isEmpty()) {
                        //For System.gc() {}
                        {
                            final ArrayList<E> arrayList = new ArrayList<>(currentCollection);
                            currentCollection.clear();
                            final java.util.Map<Long, E> rollback = new TreeMap<>();
                            try {
                                Parallel.For(arrayList, new Parallel.Operation<E>() {
                                    final long startIndex = pIndex;
                                    long index = 0;
                                    @Override
                                    public void perform(E pParameter) {
                                        //No sync necessary
                                        final E delete = LongListIterator.this.remove((startIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE);
                                        //No sync necessary
                                        rollback.put(((startIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, delete);
                                        if ((delete == null && pParameter != null) || (delete != null && !delete.equals(pParameter))) {
                                            throw new RuntimeException("The values are not the same");
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
                            } catch (Throwable t) {
                                //Rollback
                                Parallel.For(rollback.keySet(), new Parallel.Operation<Long>() {
                                    @Override
                                    public void perform(Long pParameter) {
                                        synchronized (LongListIterator.this) {
                                            LongListIterator.this.add(pParameter, rollback.get(pParameter));
                                        }
                                    }

                                    @Override
                                    public boolean follow() {
                                        return true;
                                    }
                                });
                                t.printStackTrace();
                                throw t;
                            }
                        }
                        if (((pIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) < (((LongListIterator.this.mSize-1) / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE)) {
                            currentCollection = new ArrayList<>(LongListIterator.this.collectionOf(pIndex));
                            final Runtime runtime = Runtime.getRuntime();
                            if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f) System.gc();
                        }
                    } else {
                        currentCollection.clear();
                    }
                }
            }

            @Override
            public final E get(int index) {
                return currentCollection.get(index);
            }

            @Override
            public final E set(int index, E element) {
                if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                final E result2 = currentCollection.set(index, element);
                final E result = LongListIterator.this.get(((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                if ((result == null && result2 == null) || (result != null && result.equals(result2))) {
                    final E result3 = LongListIterator.this.set(((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, element);
                    if ((result == null && result3 != null) || (result != null && !result.equals(result3))) {
                        //Rollback
                        LongListIterator.this.set(((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, result3);
                        throw new RuntimeException("The values are not the same");
                    }
                    return result;
                } else {
                    throw new RuntimeException("The values are not the same");
                }
            }

            @Override
            public final void add(int index, E element) {
                if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                currentCollection.add(index, element);
                LongListIterator.this.add(((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, element);
            }

            @Override
            public final E remove(final int index) {
                if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                final E result = LongListIterator.this.get(((pIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                final E result2 = currentCollection.remove(index);
                if ((result == null && result2 != null) || (result != null && !result.equals(result2))) {
                    throw new RuntimeException("The values are not the same");
                }
                final E result3 = LongListIterator.this.remove(((pIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                if ((result == null && result3 != null) || (result != null && !result.equals(result3))) {
                    //Rollback
                    LongListIterator.this.add(((pIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, result3);
                    throw new RuntimeException("The values are not the same");
                }
                if (((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index != (((mSize-1)/MAX_COLLECTION_SIZE)* MAX_COLLECTION_SIZE) + index) {
                    final int arrayIndex2 = (int) (pIndex / ((long) MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
                    final int listIndex2 = (arrayIndex2 == 1)
                            ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                            : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
                    currentCollection = new ArrayList<>(values[arrayIndex2].get(listIndex2));
                    final Runtime runtime = Runtime.getRuntime();
                    if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7) System.gc();
                }
                return result;
            }

            @Override
            public final int indexOf(final Object o) {
                final int[] result = new int[1];
                result[0] = -1;
                Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                    int index = -1;

                    @Override
                    public void perform(E pParameter) {
                        synchronized (this) {
                            index++;
                        }
                        if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
                            synchronized (result) {
                                result[0] = index;
                            }
                        }
                    }

                    @Override
                    public boolean follow() {
                        return result[0] == -1;
                    }
                };
                Parallel.For(this, operation);
                return result[0];
            }

            @Override
            public int lastIndexOf(final Object o) {
                final int[] result = new int[1];
                result[0] = -1;
                Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                    int index = -1;

                    @Override
                    public void perform(E pParameter) {
                        synchronized (this) {
                            index++;
                        }
                        if ((o == pParameter) || (pParameter != null && o.equals(pParameter))) {
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
                Parallel.For(this, operation);
                return result[0];
            }

            @NotNull
            @Override
            public final java.util.ListIterator<E> listIterator() {
                return currentCollection.listIterator();
            }

            @NotNull
            @Override
            public final java.util.ListIterator<E> listIterator(int index) {
                return currentCollection.listIterator(index);
            }

            @NotNull
            @Override
            public final List<E> subList(int fromIndex, int toIndex) {
                return currentCollection.subList(fromIndex, toIndex);
            }
        };
    }

    @NotNull
    @Override
    public final Long count(@Nullable final E pEntity) {
        final long[] result = new long[1];
        final Throwable[] throwable = new Throwable[1];
        for (List<E> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {
                @Override
                public void perform(E pParameter) {
                    try{
                        if ((pEntity == null && pParameter == null) || (pEntity != null && pEntity.equals(pParameter))) {
                            synchronized (result) {
                                result[0]++;
                            }
                        }
                    } catch (Throwable t) {
                        synchronized (throwable) {
                            throwable[0] = t;
                        }
                        throw t;
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }
        if (throwable[0] != null) throw new RuntimeException(throwable[0]);
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
        values[arrayIndex].get(listIndex).remove(this.collectionIndexOf(mIndex));
        mIndex--;
    }

    @Override
    public final void set(@Nullable E e) {
        if (mIndex == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (mIndex / ((long) MAX_COLLECTION_SIZE + 1));
        values[arrayIndex].get(listIndex).set(this.collectionIndexOf(mIndex), e);
    }

    @Override
    public final void add(@Nullable E pData) {
        if (mSize == Long.MAX_VALUE) throw new IllegalStateException("LongListIterator is full");
        long index;
        if (mIndex == NULL_INDEX)
            index = MIN_INDEX;
        else
            index = mIndex;
        final int arrayIndex = (int) (index / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((index % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (index / ((long) MAX_COLLECTION_SIZE + 1));
        if (values[arrayIndex].isEmpty() || (listIndex > 0 && values[arrayIndex].get(listIndex - 1).size() == Integer.MAX_VALUE)) {
            values[arrayIndex].add(new LinkedList<E>() {

                @Override
                public boolean contains(final Object o) {
                    final boolean[] result = new boolean[1];
                    Parallel.For(this, new Parallel.Operation<Object>() {
                        @Override
                        public void perform(Object pParameter) {
                            if ((o == null && pParameter == null) || (o != null && o.equals(pParameter))) {
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
            });
        }
        values[arrayIndex].get(values[arrayIndex].size() - 1).add(collectionIndexOf(index),pData);
        mSize++;
    }

    @Override
    public final boolean addAtEnd(@Nullable E pData) {
        if (mSize == Long.MAX_VALUE) throw new IllegalStateException("LongListIterator is full");
        final int arrayIndex = (int) (mSize / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((mSize % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (mSize / ((long) MAX_COLLECTION_SIZE + 1));
        if (values[arrayIndex].isEmpty() || (listIndex > 0 && values[arrayIndex].get(listIndex - 1).size() == Integer.MAX_VALUE)) {
            values[arrayIndex].add(new LinkedList<E>() {

                @Override
                public boolean contains(final Object o) {
                    final boolean[] result = new boolean[1];
                    Parallel.For(this, new Parallel.Operation<Object>() {
                        @Override
                        public void perform(Object pParameter) {
                            if ((o == null && pParameter == null) || (o != null && o.equals(pParameter))) {
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
            });
        }
        boolean added = values[arrayIndex].get(values[arrayIndex].size() - 1).add(pData);
        if (added) {
            mSize++;
        }
        return added;

    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean remove(@Nullable final E pData) {
        final boolean[] result = new boolean[1];
        final long[] index = new long[1];
        final Throwable[] throwable = new Throwable[1];
        index[0] = NULL_INDEX;
        final LinkedList<E>[] target = new LinkedList[1];
        final LinkedList<LinkedList<E>>[] followTarget = new LinkedList[2];
        followTarget[0] = new LinkedList<>();
        followTarget[1] = new LinkedList<>();
        for (final LinkedList<LinkedList<E>> lla : values) {
            Parallel.For(lla, new Parallel.Operation<LinkedList<E>>() {
                @Override
                public void perform(LinkedList<E> pParameter) {
                    try {
                        if (target[0] == null) {
                            Parallel.For(pParameter, new Parallel.Operation<E>() {
                                @Override
                                public void perform(final E pParameter) {
                                    try {
                                        synchronized (index) {
                                            index[0]++;
                                        }
                                        if (pData == pParameter || (pData != null && pData.equals(pParameter))) {
                                            synchronized (result) {
                                                result[0] = true;
                                            }
                                        }
                                    } catch (Throwable t) {
                                        synchronized (throwable) {
                                            throwable[0] = t;
                                        }
                                        throw t;
                                    }
                                }

                                @Override
                                public boolean follow() {
                                    return !result[0];
                                }
                            });
                            if (throwable[0] != null) throw new RuntimeException(throwable[0]);
                        }
                        if (result[0]) {
                            if (target[0] == null) {
                                synchronized (target) {
                                    target[0] = pParameter;
                                }
                            } else if (followTarget[0].size() == Integer.MAX_VALUE) {
                                synchronized (followTarget) {
                                    if (!followTarget[1].add(pParameter)) {
                                        synchronized (result) {
                                            result[0] = false;
                                        }
                                        throw new RuntimeException("The value has not been added");
                                    }
                                }
                            } else {
                                synchronized (followTarget) {
                                    if (!followTarget[0].add(pParameter)) {
                                        synchronized (result) {
                                            result[0] = false;
                                        }
                                        throw new RuntimeException("The value has not been added");
                                    }
                                }
                            }
                        }
                    } catch (Throwable t) {
                        synchronized (throwable) {
                            throwable[0] = t;
                        }
                        throw t;
                    }
                }

                @Override
                public boolean follow() {
                    return throwable[0] == null;
                }
            });
        }
        if (throwable[0] != null) {
            throwable[0].printStackTrace();
            throw new RuntimeException(throwable[0]);
        } else if (result[0] && target[0] != null) {
            result[0] = target[0].remove(pData);
            if (result[0]) {
                final LinkedList<E>[] previous = new LinkedList[1];
                previous[0] = target[0];
                for (LinkedList<LinkedList<E>> ll : followTarget) {
                    Parallel.For(ll, new Parallel.Operation<LinkedList<E>>() {
                        @Override
                        public void perform(LinkedList<E> pParameter) {
                            E data = pParameter.getFirst();
                            pParameter.remove(data);
                            synchronized (previous) {
                                previous[0].add(data);
                                previous[0] = pParameter;
                            }
                        }

                        @Override
                        public boolean follow() {
                            return true;
                        }
                    });
                }
                if (index[0] != NULL_INDEX && index[0] <= mIndex) {
                    mIndex--;
                }
                mSize--;
            }
        }
        return result[0];
    }

    @Override
    public final <E2 extends E> boolean retainAll(@NotNull CollectionIterator<E2, Long> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = !pCollectionIterator.isEmpty();
        final LongListIterator<E> retain = new LongListIterator<E>();
        for (Collection<E2> collection : pCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E2>() {

                @Override
                public void perform(E2 pParameter) {
                    synchronized (result) {
                        result[0] = retain.addAtEnd(pParameter);
                    }

                }

                @Override
                public boolean follow() {
                    return result[0];
                }
            });
        }

        final LongListIterator<E> delete = new LongListIterator<>();
        for (List<E> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {
                @Override
                public void perform(E pParameter) {
                    if (!retain.contains(pParameter)) {
                        synchronized (result) {
                            result[0] =  delete.addAtEnd(pParameter);
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return result[0];
                }
            });
        }

        for (List<E> collection : delete.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {
                @Override
                public void perform(E pParameter) {
                    synchronized (result) {
                        result[0] = LongListIterator.this.remove(pParameter);
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
        values[0].clear();
        values[1].clear();
        mIndex = NULL_INDEX;
        mSize = 0L;
    }

    @Override
    public final <E2 extends E> void addAll(@NotNull final Long pIndex, @NotNull final CollectionIterator<E2, Long> pCollectionIterator) {
        if (pIndex < MIN_INDEX || pIndex > mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        Parallel.Operation<E2> operation = new Parallel.Operation<E2>() {
            long index = pIndex;

            @Override
            public void perform(E2 pParameter) {
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
        for (Collection<E2> collection : pCollectionIterator.allCollections()) {
            Parallel.For(collection, operation);
        }
    }

    @Override
    public final E remove(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        @SuppressWarnings("unchecked")
        final E[] result = (E[]) new Data[1];
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            long index = MIN_INDEX;
            @Override
            public void perform(E pParameter) {
                if (index == pIndex) {
                    synchronized (result) {
                        result[0] = pParameter;
                    }
                    final int arrayIndex = (int) (index / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
                    final int listIndex = (arrayIndex == 1)
                            ? (int) ((index % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                            : (int) (index / ((long) MAX_COLLECTION_SIZE + 1));
                    synchronized (LongListIterator.this) {
                        E delete = values[arrayIndex].get(listIndex).remove(collectionIndexOf(index));
                        if ((delete == null && pParameter != null) || (delete != null && !delete.equals(pParameter))) {
                            values[arrayIndex].get(listIndex).add(collectionIndexOf(index),delete);
                            throw new RuntimeException("The value delete at index is not the same");
                        }
                        mSize--;
                    }
                }
                synchronized (this) {
                    index++;
                }
            }

            @Override
            public boolean follow() {
                return pIndex >= index;
            }
        };
        for (List<E> collection : this.allCollections()) {
            Parallel.For(collection,operation);
        }
        return result[0];
    }

    @Override
    @Nullable
    public final E get(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
        //if (values[arrayIndex].get(listIndex) == null) return null;
        return values[arrayIndex].get(listIndex).get(collectionIndexOf(pIndex));
    }

    @Override
    @Nullable
    public final E set(@NotNull final Long pIndex, @Nullable final E pData) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
        //if (values[arrayIndex].get(listIndex) == null) return null;
        return values[arrayIndex].get(listIndex).set(collectionIndexOf(pIndex), pData);
    }

    /**
     *
     * @param pIndex index
     * @param pData data
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size())
     */
    @Override
    public final void add(@NotNull final Long pIndex, @Nullable final E pData) {
        if (pIndex < MIN_INDEX || pIndex > mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
        if (values[arrayIndex].get(listIndex) != null) {
            values[arrayIndex].get(listIndex).add(collectionIndexOf(pIndex), pData);
        } else {
            values[arrayIndex].add(listIndex, new LinkedList<E>() {
                {
                    if (!add(pData)) throw new RuntimeException("The value has not been added");
                }

                @Override
                public boolean contains(final Object o) {
                    final boolean[] result = new boolean[1];
                    Parallel.For(this, new Parallel.Operation<Object>() {
                        @Override
                        public void perform(Object pParameter) {
                            if ((o == null && pParameter == null) || (o != null && o.equals(pParameter))) {
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
            });
        }
        if (pIndex <= this.mIndex) this.mIndex++;
    }

    @Override
    @NotNull
    public final Long indexOf(@Nullable final E pData) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            long index = NULL_INDEX;

            @Override
            public void perform(E pParameter) {
                if ((pData == pParameter) || (pData != null && pData.equals(pParameter))) {
                    synchronized (this) {
                        index++;
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
                return result[0] == NULL_INDEX;
            }
        };
        for (Collection<E> collection : this.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result[0];
    }

    @Override
    @NotNull
    public final Long lastIndexOf(@Nullable final E pData) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            long index = NULL_INDEX;

            @Override
            public void perform(E pParameter) {
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
        for (List<E> collection : this.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result[0];
    }

    @NotNull
    @Override
    public final java.util.ListIterator<E> listIterator() {
        return ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object.cloneObject(this);
    }

    @NotNull
    @Override
    public final java.util.ListIterator<E> listIterator(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final ListIterator<E, Long> result = new LongListIterator<>();
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            long index = NULL_INDEX;

            @Override
            public void perform(E pParameter) {
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
        for (List<E> collection : this.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result;
    }

    @NotNull
    @Override
    public final ListIterator<E, Long> subListIterator(@NotNull final Long pIndex1, @NotNull final Long pIndex2) {
        if (pIndex1 >= pIndex2 || pIndex1 < MIN_INDEX || pIndex2 >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex1) + " to " + String.valueOf(pIndex2) + " (excluded)");
        final ListIterator<E, Long> result = new LongListIterator<>();
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            long index = NULL_INDEX;

            @Override
            public void perform(E pParameter) {
                synchronized (this) {
                    index++;
                }
                if (index >= pIndex1 && index < pIndex2) {
                    synchronized (result) {
                        if (!result.addAtEnd(pParameter)) throw new RuntimeException("The value has not been added");
                    }
                }
            }

            @Override
            public boolean follow() {
                return index < pIndex2;
            }
        };
        for (Collection<E> collection : this.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result;
    }
}
