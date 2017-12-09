package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import ca.qc.bergeron.marcantoine.crammeur.librairy.events.SizeListener;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Iterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public class LongListIterator<E> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator<E, Long> {

    @SuppressWarnings("unchecked")
    private final LinkedList<LinkedList<E>>[] values = new LinkedList[2];
    private transient volatile long mIndex = NULL_INDEX;
    private transient volatile long mSize = 0L;

    //TODO Parallel methods
    private transient final LinkedList<SizeListener> sizeListeners = new LinkedList<>();

    public LongListIterator(final CollectionIterator<E,? extends Serializable> pCollectionIterator) {
        values[0] = new LinkedList<>();
        values[1] = new LinkedList<>();
        final Parallel.Operation<Collection<E>> operation = new Parallel.Operation<Collection<E>>() {
            long index = 0;
            @Override
            public void perform(Collection<E> pParameter) {
                values[(int) (index++ / ((long) MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE))].add(new LinkedList<>(pParameter));
                synchronized (this) {
                    mSize+=pParameter.size();
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        };
        Parallel.For(pCollectionIterator.allCollections(), operation);
    }

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
        sizeListeners.clear();
    }

    @NotNull
    @Override
    public final Long getIndex() {
        return mIndex;
    }

    @Override
    public final void setIndex(@NotNull Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        mIndex = pIndex;
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
        return (int) (pIndex % ((long)MAX_COLLECTION_SIZE + 1));
    }

    @Override
    public final boolean hasNext() {
        return mIndex + 1 < mSize && mIndex + 1 >= MIN_INDEX;
    }
    @Nullable
    @Override
    public final E next() throws NoSuchElementException {
        if (mIndex == NULL_INDEX)
            mIndex = MIN_INDEX;
        else if (mIndex + 1 <= mSize && mIndex + 1 >= MIN_INDEX)
            mIndex++;
        else
            throw new NoSuchElementException();

        return get();
    }

    @Nullable
    public final E get() throws IndexOutOfBoundsException {
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
        if (mIndex - 1 >= MIN_INDEX)
            mIndex--;
        else
            throw new NoSuchElementException();

        return get();
    }

    @Override
    public final boolean hasNextCollection() {
        return (mIndex + MAX_COLLECTION_SIZE < mSize && mIndex + MAX_COLLECTION_SIZE >= MIN_INDEX) || (mIndex == NULL_INDEX && mSize != 0);
    }

    @Override
    @NotNull
    public final List<E> nextCollection() throws NoSuchElementException {
        if (mIndex == NULL_INDEX)
            mIndex = MIN_INDEX;
        else if (mIndex + MAX_COLLECTION_SIZE < mSize && mIndex + MAX_COLLECTION_SIZE >= MIN_INDEX)
            mIndex += MAX_COLLECTION_SIZE;
        else
            throw new NoSuchElementException();

        return getCollection();
    }

    @Override
    public final List<E> getCollection() throws IndexOutOfBoundsException {
        return collectionOf(mIndex);
    }

    @Override
    public boolean hasPreviousCollection() {
        return mIndex - MAX_COLLECTION_SIZE >= MIN_INDEX;
    }

    @NotNull
    @Override
    public final List<E> previousCollection() throws NoSuchElementException {
        if (mIndex - MAX_COLLECTION_SIZE >= MIN_INDEX)
            mIndex -= MAX_COLLECTION_SIZE;
        else
            throw new NoSuchElementException();

        return getCollection();
    }

    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public final Iterable<List<E>> allCollections() {
        return new Iterable<List<E>>() {
            @NotNull
            @Override
            public final Iterator<List<E>> iterator() {
                return LongListIterator.this.<List<E>>collectionIterator();
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
            public final java.util.Iterator<E> iterator() {
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
        this.remove(mIndex);
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
        final long index;
        if (mIndex == NULL_INDEX)
            index = MIN_INDEX;
        else
            index = mIndex;
        final int arrayIndex = (int) (index / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((index % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (index / ((long) MAX_COLLECTION_SIZE + 1));
        if (mSize % MAX_COLLECTION_SIZE == 0) {
            if (values[arrayIndex].size() == Integer.MAX_VALUE) {
                values[arrayIndex+1].add(new LinkedList<E>() {

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
            } else {
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
        }
        if (values[arrayIndex].get(listIndex).size() == Integer.MAX_VALUE) {
            long index2 = index;
            E data = pData;
            while (index2 < mSize) {
                data = this.set(index2, data);
                index2++;
            }
            if (!this.addAtEnd(data)) throw new RuntimeException("The value has not been added");
        } else {
            values[arrayIndex].get(values[arrayIndex].size() - 1).add(collectionIndexOf(index),pData);
        }
        mSize++;
    }

    @Override
    public final boolean addAtEnd(@Nullable E pData) {
        if (mSize == Long.MAX_VALUE) throw new IllegalStateException("LongListIterator is full");
        final int arrayIndex = (int) (mSize / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        if (mSize % MAX_COLLECTION_SIZE == 0) {
            if (values[arrayIndex].size() == Integer.MAX_VALUE) {
                values[arrayIndex+1].add(new LinkedList<E>() {

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
            } else {
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
        index[0] = MIN_INDEX;
        final LinkedList<E>[] target = new LinkedList[1];
        final LinkedList<LinkedList<E>>[] followTarget = new LinkedList[2];
        followTarget[0] = new LinkedList<>();
        followTarget[1] = new LinkedList<>();
        for (final LinkedList<LinkedList<E>> lla : values) {
            Parallel.For(new ArrayList<>(lla), new Parallel.Operation<LinkedList<E>>() {
                @Override
                public void perform(LinkedList<E> pParameter) {
                    if (target[0] == null) {
                        Parallel.For(pParameter, new Parallel.Operation<E>() {
                            @Override
                            public void perform(final E pParameter) {
                                if (pData == pParameter || (pData != null && pData.equals(pParameter))) {
                                    synchronized (result) {
                                        result[0] = true;
                                    }
                                }
                                synchronized (index) {
                                    index[0]++;
                                }
                            }

                            @Override
                            public boolean follow() {
                                return !result[0];
                            }
                        });
                    }
                    if (result[0] && target[0] == null) {
                        target[0] = pParameter;
                    } else if (result[0]) {
                        if (followTarget[0].size() == Integer.MAX_VALUE) {
                            if (!followTarget[1].add(pParameter)) {
                                throw new RuntimeException("The value has not been added");
                            }
                        } else {
                            if (!followTarget[0].add(pParameter)) {
                                throw new RuntimeException("The value has not been added");
                            }
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }
        if (result[0] && target[0] != null) {
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
                if (index[0] <= mIndex) {
                    mIndex--;
                }
                mSize--;
                if (mSize == 0) mIndex = NULL_INDEX;
            }
            for (SizeListener sizeListener : sizeListeners) {
                sizeListener.change();
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
        if (Long.MAX_VALUE - mSize < pCollectionIterator.size()) throw new IllegalStateException("The collection has too much values");
        if (pIndex < MIN_INDEX || pIndex > mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        Parallel.Operation<E2> operation = new Parallel.Operation<E2>() {
            long index = pIndex;

            @Override
            public void perform(E2 pParameter) {
                synchronized (LongListIterator.this) {
                    LongListIterator.this.add(index,pParameter);
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

    @SuppressWarnings("unchecked")
    @Override
    public final E remove(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final E[] result = (E[]) new Data[1];
        final LinkedList<E>[] target = new LinkedList[1];
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
        target[0] = values[arrayIndex].get(listIndex);
        if (pIndex / MAX_COLLECTION_SIZE == mSize / MAX_COLLECTION_SIZE) {
            result[0] = target[0].remove(collectionIndexOf(pIndex));
        } else {
            final LinkedList<LinkedList<E>>[] followTarget = new LinkedList[2];
            followTarget[0] = new LinkedList<>();
            followTarget[1] = new LinkedList<>();
            for (int arrayIndex2 = arrayIndex; arrayIndex2<values.length; arrayIndex2++) {
                if (arrayIndex2 == arrayIndex) {
                    Parallel.For(new ArrayList<>(values[arrayIndex2]), new Parallel.Operation<LinkedList<E>>() {
                        int index = 0;
                        @Override
                        public void perform(LinkedList<E> pParameter) {
                            if (index > listIndex) {
                                if (followTarget[0].size() == Integer.MAX_VALUE) {
                                    synchronized (followTarget){
                                        followTarget[1].add(pParameter);
                                    }
                                } else {
                                    synchronized (followTarget) {
                                        followTarget[0].add(pParameter);
                                    }
                                }
                            } else {
                                synchronized (this) {
                                    index++;
                                }
                            }
                        }

                        @Override
                        public boolean follow() {
                            return true;
                        }
                    });
                } else {
                    Parallel.For(new ArrayList<>(values[arrayIndex2]), new Parallel.Operation<LinkedList<E>>() {
                        @Override
                        public void perform(LinkedList<E> pParameter) {
                            if (followTarget[0].size() == Integer.MAX_VALUE) {
                                synchronized (followTarget) {
                                    followTarget[1].add(pParameter);
                                }
                            } else {
                                synchronized (followTarget) {
                                    followTarget[0].add(pParameter);
                                }
                            }
                        }

                        @Override
                        public boolean follow() {
                            return true;
                        }
                    });
                }
            }
            result[0] = target[0].remove(collectionIndexOf(pIndex));
            final LinkedList<E>[] previous = new LinkedList[1];
            previous[0] = target[0];
            for (LinkedList<LinkedList<E>> ll : followTarget) {
                Parallel.For(ll, new Parallel.Operation<LinkedList<E>>() {
                    @Override
                    public void perform(LinkedList<E> pParameter) {
                        E data = pParameter.getFirst();
                        pParameter.remove(0);
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
        }
        if (pIndex <= mIndex) {
            mIndex--;
        }
        mSize--;
        if (mSize == 0) mIndex = NULL_INDEX;
        for (SizeListener sizeListener : sizeListeners) {
            sizeListener.change();
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
     * @param pIndex Index
     * @param pData data
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size())
     */
    @Override
    public final void add(@NotNull final Long pIndex, @Nullable final E pData) throws IllegalStateException, IndexOutOfBoundsException {
        if (mSize == Long.MAX_VALUE) throw new IllegalStateException("The list is full");
        if (pIndex < MIN_INDEX || pIndex > mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        if (pIndex == mSize) {
            if (!this.addAtEnd(pData)) throw new RuntimeException("The value has not been added");
        } else {
            final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
            final int listIndex = (arrayIndex == 1)
                    ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                    : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
            if (values[arrayIndex].size()-1 < listIndex) {
                values[arrayIndex].add(new LinkedList<E>() {
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
            } else {
                if (values[arrayIndex].get(listIndex).size() == Integer.MAX_VALUE) {
                    int arrayIndex2 = arrayIndex;
                    int listIndex2 = listIndex;
                    E lastData = null;
                    do {
                        if (values[arrayIndex2].size() - (listIndex2 + 1) > 0) {
                            final E data = values[arrayIndex2].get(listIndex2).removeLast();
                            if (arrayIndex2 == arrayIndex && listIndex2 == listIndex) {
                                values[arrayIndex2].get(listIndex2).add(collectionIndexOf(pIndex),pData);
                            } else {
                                if (values[arrayIndex2].size() < listIndex2 + 1) {
                                    //For Parallel.For final value
                                    final E data2 = lastData;
                                    values[arrayIndex2].add(new LinkedList<E>() {
                                        {
                                            if (!add(data2)) throw new RuntimeException("The value has not been added");
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
                                } else {
                                    values[arrayIndex2].get(listIndex2).add(0,lastData);
                                }
                            }
                            lastData = data;
                            if (listIndex2 == Integer.MAX_VALUE) {
                                listIndex2 = 0;
                                arrayIndex2++;
                            }
                            else
                                listIndex2++;
                        } else if (values[arrayIndex2].get(listIndex2).size() != Integer.MAX_VALUE) {
                            values[arrayIndex2].get(listIndex2).add(0,lastData);
                            lastData = null;
                        } else {
                            final E data2 = lastData;
                            values[arrayIndex2+1].add(new LinkedList<E>() {
                                {
                                    if (!add(data2)) throw new RuntimeException("The value has not been added");
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
                            lastData = null;
                        }
                    } while (lastData != null);

                } else {
                    values[arrayIndex].get(listIndex).add(collectionIndexOf(pIndex),pData);
                }
            }
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
    public final ListIterator<E,Long> listIterator() {
        return new LongListIterator<>(this);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public final ListIterator<E,Long> listIterator(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize)
            throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        if (pIndex == MIN_INDEX) return listIterator();
        final LongListIterator<E> result;
        if (pIndex % MAX_COLLECTION_SIZE == 0) {
            final LinkedList<LinkedList<E>>[] clone = new LinkedList[2];
            clone[0] = new LinkedList<>();
            clone[1] = new LinkedList<>();
            final long[] index = new long[1];
            for (int arrayIndex = 0; arrayIndex<values.length; arrayIndex++) {
                final int finalArrayIndex = arrayIndex;
                Parallel.For(new ArrayList<>(values[arrayIndex]), new Parallel.Operation<LinkedList<E>>() {
                    @Override
                    public void perform(LinkedList<E> pParameter) {
                        synchronized (index) {
                            index[0]+=pParameter.size();
                        }
                        if (index[0] >= pIndex)
                            synchronized (clone) {
                                clone[finalArrayIndex].add(new LinkedList<>(pParameter));
                            }
                    }

                    @Override
                    public boolean follow() {
                        return true;
                    }
                });
            }
            java.util.Map<Field,Object> map = new HashMap<>();
            try {
                map.put(this.getClass().getDeclaredField("values"), clone);
                map.put(this.getClass().getDeclaredField("mSize"), mSize - pIndex);
                result = ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object.createObject(this.getClass(), map);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            result = new LongListIterator<>();
            final Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                long skipCount = 0;
                @Override
                public void perform(E pParameter) {
                    if (skipCount < pIndex) {
                        synchronized (this) {
                            skipCount++;
                        }
                    } else {
                        synchronized (result) {
                            if (!result.addAtEnd(pParameter)) throw new RuntimeException("The value has not been added");
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            };
            long traveled = 0;
            for (int arrayIndex = 0; arrayIndex<values.length; arrayIndex++) {
                for (LinkedList<E> linkedList : new ArrayList<>(values[arrayIndex])){
                    traveled+= linkedList.size();
                    if (traveled >= pIndex) {
                        Parallel.For(new ArrayList<>(linkedList), operation);
                    }
                }
            }
        }
        return result;
    }

    @NotNull
    @Override
    public final ListIterator<E, Long> subListIterator(@NotNull final Long pFromIndex, @NotNull final Long pToIndex) {
        if (pFromIndex >= pToIndex || pFromIndex < MIN_INDEX || pToIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pFromIndex) + " to " + String.valueOf(pToIndex) + " (excluded)");
        final ListIterator<E, Long> result = new LongListIterator<>();
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            long index = MIN_INDEX;

            @Override
            public void perform(E pParameter) {
                if (index >= pFromIndex && index < pToIndex) {
                    synchronized (result) {
                        if (!result.addAtEnd(pParameter)) throw new RuntimeException("The value has not been added");
                    }
                }
                synchronized (this) {
                    index++;
                }
            }

            @Override
            public boolean follow() {
                return index < pToIndex;
            }
        };
        for (Collection<E> collection : this.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result;
    }

    @NotNull
    @Override
    public final Iterator<E> iterator() {
        return new Iterator<E>() {

            private transient long mIndex = NULL_INDEX;

            @Nullable
            @Override
            public E next() throws NoSuchElementException {
                if (mIndex + 1 < mSize)
                    mSize++;
                else
                    throw new NoSuchElementException();
                return get();
            }

            @Nullable
            @Override
            public E get() throws IndexOutOfBoundsException {
                if (mIndex < MIN_INDEX || mIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(mIndex));
                final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
                final int listIndex = (arrayIndex == 1)
                        ? (int) ((mIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                        : (int) (mIndex / ((long) MAX_COLLECTION_SIZE + 1));
                //if (values[arrayIndex].get(listIndex) == null) return null;
                return values[arrayIndex].get(listIndex).get(collectionIndexOf(mIndex));
            }

            @Override
            public boolean hasPrevious() {
                return mIndex != NULL_INDEX && mIndex + 1 >= MIN_INDEX ;
            }

            @Nullable
            @Override
            public E previous() throws NoSuchElementException {
                if (mIndex == NULL_INDEX || mIndex - 1 < MIN_INDEX)
                    throw new NoSuchElementException();
                else
                    mIndex--;
                return get();
            }

            @Override
            public void add(@Nullable E pEntity) {
                LongListIterator.this.add(mIndex,pEntity);
            }

            @Override
            public void set(@Nullable E pEntity) {
                LongListIterator.this.set(mIndex,pEntity);
            }

            @Override
            public void remove() {
                LongListIterator.this.remove(mIndex);
            }

            @Override
            public boolean hasNext() {
                return mIndex + 1 < mSize;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<List<E>> collectionIterator() {
        return new Iterator<List<E>>() {

            final class List implements java.util.List<E> {

                {
                    final List list = this;
                    sizeListeners.add(new SizeListener() {

                        @Override
                        public void change() {
                            boolean changeNeeded;
                            if (changeNeeded = mSize == 0) {
                                list.mIndex = NULL_INDEX;
                            } else if (changeNeeded = list.mIndex >= mSize) {
                                list.mIndex = mSize - 1;
                            }
                            if (changeNeeded) {
                                list.currentCollection = new ArrayList<>(collectionOf(list.mIndex));
                                Runtime runtime = Runtime.getRuntime();
                                if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                                    System.gc();
                            }

                        }
                    });
                }

                private ArrayList<E> currentCollection;
                private transient long mIndex;

                private List(long pIndex) {
                    mIndex = pIndex;
                    currentCollection = new ArrayList<>(collectionOf(pIndex));
                }


                @Override
                public final boolean addAll(final int pIndex, @NotNull final Collection<? extends E> c) {
                    if (pIndex < 0 || pIndex > this.size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
                    final boolean[] result = new boolean[1];
                    final int[] index = new int[1];
                    index[0] = pIndex;
                    result[0] = !c.isEmpty();
                    final java.util.List<E> collection = this;
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
                    final E result = LongListIterator.this.get(((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                    if ((result == null && result2 == null) || (result != null && result.equals(result2))) {
                        final E result3 = LongListIterator.this.set(((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, element);
                        if ((result == null && result3 != null) || (result != null && !result.equals(result3))) {
                            //Rollback
                            LongListIterator.this.set(((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, result3);
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
                    if (currentCollection.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
                    currentCollection.add(index,element);
                    LongListIterator.this.add((mIndex/MAX_COLLECTION_SIZE)*MAX_COLLECTION_SIZE+index,element);
                }

                @Override
                public final E remove(final int index) {
                    if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                    final E result2 = currentCollection.remove(index);
                    final long index2 = ((mIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index;
                    final E result = LongListIterator.this.get(index2);
                    if ((result == null && result2 != null) || (result != null && !result.equals(result2))) {
                        throw new RuntimeException("The values are not the same");
                    } else {
                        final E result3 = LongListIterator.this.remove(index2);
                        if ((result == null && result3 != null) || (result != null && !result.equals(result3))) {
                            LongListIterator.this.add(index2,result3);
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
                public final java.util.List<E> subList(final int fromIndex, final int toIndex) {
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
                public final java.util.Iterator<E> iterator() {
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
                        result = LongListIterator.this.addAtEnd(e);
                        if (!result)
                            if (!currentCollection.remove(currentCollection.size()-1).equals(e)) {
                                throw new RuntimeException("The value has not been removed");
                            }
                    }
                    return result;
                }

                @SuppressWarnings("unchecked")
                @Override
                public final boolean remove(final Object o) {
                    boolean result;
                    if (result = currentCollection.remove(o)) {
                        result = LongListIterator.this.remove((E) o);
                        if (!result) throw new RuntimeException("The value has not been removed");
                        if ((((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) != (((mSize-1) / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE)) ) {
                            currentCollection = new ArrayList<>(collectionOf(mIndex));
                        }
                    }
                    return result;
                }

                @SuppressWarnings("SuspiciousMethodCalls")
                @Override
                public final boolean containsAll(@NotNull final Collection<?> c) {
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
                public final boolean addAll(@NotNull final Collection<? extends E> c) {
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
                public final boolean removeAll(@NotNull final Collection<?> c) {
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
                        if (!LongListIterator.this.isEmpty()) {
                            {
                                final ArrayList<E> arrayList = new ArrayList<>(currentCollection);
                                currentCollection.clear();
                                final java.util.Map<Long, E> rollback = new TreeMap<>();
                                try {
                                    Parallel.For(arrayList, new Parallel.Operation<E>() {
                                        final long startIndex = mIndex;
                                        long index = 0;
                                        @Override
                                        public void perform(E pParameter) {
                                            final E delete;
                                            synchronized (values) {
                                                delete = LongListIterator.this.remove((startIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE);
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
                            if (((mIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) < (((mSize-1) / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE)) {
                                currentCollection = new ArrayList<>(LongListIterator.this.collectionOf(mIndex));
                                final Runtime runtime = Runtime.getRuntime();
                                if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                                    System.gc();
                            }
                        } else {
                            currentCollection.clear();
                        }
                    }
                }

            }

            private transient long mIndex = NULL_INDEX;

            @Override
            public final boolean hasNext() {
                return (mIndex == NULL_INDEX)
                        ?mSize != 0
                        :mIndex + MAX_COLLECTION_SIZE < mSize && mIndex + MAX_COLLECTION_SIZE > MIN_INDEX;
            }

            @Override
            public final java.util.List<E> next() throws NoSuchElementException {
                if (mIndex == NULL_INDEX) {
                    mIndex = MIN_INDEX;
                } else if (mIndex + MAX_COLLECTION_SIZE < mSize && mIndex + MAX_COLLECTION_SIZE >= MIN_INDEX) {
                    mIndex += MAX_COLLECTION_SIZE;
                } else {
                    throw new NoSuchElementException();
                }
                try {
                    return new List(mIndex);
                } finally {
                    final Runtime runtime = Runtime.getRuntime();
                    if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                        System.gc();
                }
            }

            @Nullable
            @Override
            public java.util.List<E> get() throws IndexOutOfBoundsException {
                try {
                    return new List(mIndex);
                } finally {
                    final Runtime runtime = Runtime.getRuntime();
                    if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                        System.gc();
                }
            }

            @Override
            public boolean hasPrevious() {
                return mIndex != NULL_INDEX && MIN_INDEX <= mIndex - MAX_COLLECTION_SIZE;
            }

            @Nullable
            @Override
            public java.util.List<E> previous() throws NoSuchElementException {
                if (mIndex - MAX_COLLECTION_SIZE >= MIN_INDEX){
                    mIndex-=MAX_COLLECTION_SIZE;
                } else
                    throw new NoSuchElementException();
                try {
                    return new List(mIndex);
                } finally {
                    final Runtime runtime = Runtime.getRuntime();
                    if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                        System.gc();
                }
            }

            @Override
            public void add(@Nullable java.util.List<E> pEntity) {
                {
                    List list = new List(mIndex);
                    if (list.size() + pEntity.size() == Integer.MAX_VALUE || list.size() + pEntity.size() < MIN_INDEX) throw new IllegalStateException("The list is full");
                    list.addAll(pEntity);
                }
                final Runtime runtime = Runtime.getRuntime();
                if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                    System.gc();
            }

            @Override
            public void set(@Nullable java.util.List<E> pEntity) {
                {
                    final List list = new List(mIndex);
                    if (pEntity.size() != list.size()) throw new IllegalStateException("The lists are not the same size");
                    Parallel.For(pEntity, new Parallel.Operation<E>() {
                        private int index = 0;
                        @Override
                        public void perform(E pParameter) {
                            list.set(index,pParameter);
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
                final Runtime runtime = Runtime.getRuntime();
                if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                    System.gc();
            }

            @Override
            public void remove() {
                {
                    if (mIndex < MIN_INDEX) throw new IllegalStateException(String.valueOf(mIndex));
                    new List(mIndex).remove(collectionIndexOf(mIndex));
                }
                final Runtime runtime = Runtime.getRuntime();
                if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                    System.gc();
            }

        };
    }
}
