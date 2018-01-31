package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import ca.qc.bergeron.marcantoine.crammeur.librairy.events.SizeListener;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Iterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public class LongListIterator<E> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator<E, Long> {


    //TODO
    private final class ListIterator implements java.util.List<E> {

        transient ParallelArrayList<E> currentCollection;
        transient final long mCollectionIndex;

        private ListIterator() {
            currentCollection = (mIndex != NULL_INDEX)?new ParallelArrayList<>(collectionOf(mIndex)):null;
            mCollectionIndex = (mIndex == NULL_INDEX)?NULL_INDEX:mIndex/MAX_COLLECTION_SIZE;
        }


        @Override
        public final boolean addAll(final int pIndex, @NotNull final Collection<? extends E> c) {
            if (pIndex < 0 || pIndex > this.size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
            final boolean[] result = new boolean[1];
            result[0] = !c.isEmpty();
            final java.util.List<E> collection = this;
            Parallel.For(c, new Parallel.Operation<E>() {
                @Override
                public void perform(E pParameter) {
                    synchronized (collection) {
                        collection.add(pIndex,pParameter);
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
            return LongListIterator.this.set(((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, element);

        }

        @Override
        public final void add(final int index, final E element) {
            if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
            if (currentCollection.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
            LongListIterator.this.add((mIndex/MAX_COLLECTION_SIZE)*MAX_COLLECTION_SIZE+index,element);
        }

        @Override
        public final E remove(final int index) {
            if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
            final long index2 = ((mIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index;
            return LongListIterator.this.remove(index2);
        }

        @Override
        public final int indexOf(final Object o) {
            final int[] result = new int[1];
            result[0] = -1;
            Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                int index = 0;

                @Override
                public void perform(E pParameter) {
                    if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
            };
            Parallel.For(this, operation);
            return result[0];
        }

        @Override
        public final int lastIndexOf(final Object o) {
            final int[] result = new int[1];
            result[0] = -1;
            Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                int index = 0;

                @Override
                public void perform(E pParameter) {
                    if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
            };
            Parallel.For(this, operation);
            return result[0];
        }

        @NotNull
        @Override
        public final java.util.ListIterator<E> listIterator() {
            return this.listIterator(-1);
        }

        @NotNull
        @Override
        public final java.util.ListIterator<E> listIterator(final int index) {
            return new java.util.ListIterator<E>() {

                private transient int mIndex = index;

                @Override
                public boolean hasNext() {
                    return mIndex + 1 < mSize - (mCollectionIndex * MAX_COLLECTION_SIZE) && mIndex + 1 != Integer.MAX_VALUE;
                }

                @Override
                public E next() {
                    return ListIterator.this.get(++mIndex);
                }

                @Override
                public boolean hasPrevious() {
                    return mIndex - 1 >= 0;
                }

                @Override
                public E previous() {
                    return ListIterator.this.get(--mIndex);
                }

                @Override
                public int nextIndex() {
                    return mIndex + 1;
                }

                @Override
                public int previousIndex() {
                    return mIndex - 1;
                }

                @Override
                public void remove() {
                    ListIterator.this.remove(mIndex--);
                }

                @Override
                public void set(E e) {
                    ListIterator.this.set(mIndex,e);
                }

                @Override
                public void add(E e) {
                    ListIterator.this.add(mIndex,e);
                }
            };
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
                    if ((o == pParameter) || (pParameter != null && pParameter.equals(o)))
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
            return new java.util.Iterator<E>() {

                private transient int index = -1;

                @Override
                public boolean hasNext() {
                    return index + 1 < mSize - (mCollectionIndex * MAX_COLLECTION_SIZE);
                }

                @Override
                public E next() {
                    return ListIterator.this.get(++index);
                }

                @Override
                public void remove() {
                    ListIterator.this.remove(index);
                }
            };
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
            if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
            return LongListIterator.this.addAtEnd(e);
        }

        @SuppressWarnings("unchecked")
        @Override
        public final boolean remove(final Object o) {
            int index = indexOf(o);
            return index != -1 && o.equals(LongListIterator.this.remove((mCollectionIndex * MAX_COLLECTION_SIZE) + index));
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
                                    if ((delete == null && pParameter != null) || (pParameter != null && !pParameter.equals(delete))) {
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
                        currentCollection = new ParallelArrayList<>(LongListIterator.this.collectionOf(mIndex));
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

    @SuppressWarnings("unchecked")
    private final ParallelArrayList<ParallelLinkedList<E>>[] values = new ParallelArrayList[2];
    private transient long mIndex = NULL_INDEX;
    private transient long mSize = 0L;
    private transient ListIterator mCurrentCollection = new ListIterator();

    private transient final ParallelArrayList<SizeListener> sizeListeners = new ParallelArrayList<>();

    public LongListIterator(final CollectionIterator<E,? extends Serializable> pCollectionIterator) {
        values[0] = new ParallelArrayList<>();
        values[1] = new ParallelArrayList<>();
        final Parallel.Operation<Collection<E>> operation = new Parallel.Operation<Collection<E>>() {
            @Override
            public void perform(Collection<E> pParameter) {
                final int arrayIndex = (int) (mSize / (((long) MAX_COLLECTION_SIZE + 1) * ((long)MAX_COLLECTION_SIZE + 1)));
                final ParallelLinkedList<E> parallelLinkedList = new ParallelLinkedList<>(pParameter);
                synchronized (LongListIterator.this) {
                    values[arrayIndex].add(parallelLinkedList);
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
        values[0] = new ParallelArrayList<>();
        values[1] = new ParallelArrayList<>();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        values[0] = null;
        values[1] = null;
        mIndex = NULL_INDEX;
        mSize = 0L;
        mCurrentCollection = null;
    }

    @NotNull
    @Override
    public Long getIndex() {
        return mIndex;
    }

    @Override
    public void setIndex(@NotNull Long pIndex) {
        if ((pIndex != NULL_INDEX && pIndex < MIN_INDEX) || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        mIndex = pIndex;
        if (mIndex / MAX_COLLECTION_SIZE != getCollectionIndex()) {
            mCurrentCollection = new ListIterator();
        }
    }

    protected Long getCollectionIndex() {
        return mCurrentCollection.mCollectionIndex;
    }

    @Override
    @NotNull
    public Long size() {
        return mSize;
    }

    @Override
    public final int collectionSize() {
        return this.collectionSizeOf(getIndex());
    }

    @Override
    public boolean isEmpty() {
        return mSize == 0;
    }

    @Override
    public final int indexInCollection() {
        return this.indexInCollectionOf(getIndex());
    }

    @Override
    public final int indexInCollectionOf(@NotNull Long pIndex) {
        return (int) (pIndex % MAX_COLLECTION_SIZE);
    }

    @Override
    public boolean hasNext() {
        return getIndex() + 1 < mSize && getIndex() + 1 >= MIN_INDEX;
    }

    @Nullable
    @Override
    public E next() throws NoSuchElementException {
        if (getIndex() == NULL_INDEX)
            this.setIndex((long)MIN_INDEX);
        else if (getIndex() + 1 < mSize && getIndex() + 1 >= MIN_INDEX)
            this.setIndex(this.getIndex()+1);
        else
            throw new NoSuchElementException();
        return get();
    }

    @Nullable
    public E get() throws IndexOutOfBoundsException {
        return getCollection().get(indexInCollection());
    }

    @Override
    public boolean hasPrevious() {
        return (getIndex() != NULL_INDEX) && getIndex() - 1 >= MIN_INDEX;
    }

    @Nullable
    @Override
    public E previous() throws NoSuchElementException {
        if (getIndex() - 1 >= MIN_INDEX)
            this.setIndex(this.getIndex()-1);
        else
            throw new NoSuchElementException();

        return get();
    }

    @Override
    public boolean hasNextCollection() {
        return (getIndex() + (MAX_COLLECTION_SIZE - indexInCollection()) < mSize && getIndex() + (MAX_COLLECTION_SIZE - indexInCollection()) >= MIN_INDEX) || (getIndex() == NULL_INDEX && mSize != 0);
    }

    @Override
    @NotNull
    public java.util.List<E> nextCollection() throws NoSuchElementException {
        if (getIndex() == NULL_INDEX)
            this.setIndex((long)MIN_INDEX);
        else if (getIndex() + (MAX_COLLECTION_SIZE - indexInCollection()) < mSize && getIndex() + (MAX_COLLECTION_SIZE - indexInCollection()) >= MIN_INDEX)
            this.setIndex(this.getIndex()+(MAX_COLLECTION_SIZE - indexInCollection()));
        else
            throw new NoSuchElementException();

        return getCollection();
    }

    @Override
    public java.util.List<E> getCollection() throws IndexOutOfBoundsException {
        if (getIndex() / MAX_COLLECTION_SIZE != getCollectionIndex()) {
            mCurrentCollection = new ListIterator();
        }
        return mCurrentCollection;
    }

    @Override
    public boolean hasPreviousCollection() {
        return getIndex() - (MAX_COLLECTION_SIZE - (MAX_COLLECTION_SIZE - indexInCollection())) >= MIN_INDEX;
    }

    @NotNull
    @Override
    public java.util.List<E> previousCollection() throws NoSuchElementException {
        if (getIndex() - (MAX_COLLECTION_SIZE - (MAX_COLLECTION_SIZE - indexInCollection())) >= MIN_INDEX)
            this.setIndex(getIndex() - (MAX_COLLECTION_SIZE - (MAX_COLLECTION_SIZE - indexInCollection())));
        else
            throw new NoSuchElementException();

        return getCollection();
    }

    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public final Iterable<java.util.List<E>> allCollections() {
        return new Iterable<java.util.List<E>>() {
            @NotNull
            @Override
            public final Iterator<java.util.List<E>> iterator() {
                return LongListIterator.this.<java.util.List<E>>collectionsIterator();
            }
        };
    }

    //TODO
    @Override
    @NotNull
    public java.util.List<E> collectionOf(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
        return new java.util.List<E>() {

            //Performance array list
            private ParallelArrayList<E> currentCollection = new ParallelArrayList<>(values[arrayIndex].get(listIndex));

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
                        if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
                final java.util.List<E> list = this;
                return new java.util.Iterator<E>() {

                    private transient int mIndex = -1;

                    @Override
                    public boolean hasNext() {
                        return mIndex + 1 < list.size();
                    }

                    @Override
                    public E next() {
                        return list.get(++mIndex);
                    }

                    @Override
                    public void remove() {
                        list.remove(mIndex);
                    }
                };
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
                return currentCollection.add(e) && LongListIterator.this.addAtEnd(e);
            }

            @SuppressWarnings("unchecked")
            @Override
            public final boolean remove(final Object o) {
                final int index = this.indexOf(o);
                boolean result = index != -1;
                if (result) {
                    final E data = LongListIterator.this.get(((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                    if (result = ((o == data) || (data != null && data.equals(o)))) {
                        result = currentCollection.remove(data);
                        if (result) {
                            final E data2 = LongListIterator.this.remove(((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                            if ((result = ((o == data2) || (data2 != null && data2.equals(o)))) &&
                                    ((pIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index != (((mSize-1)/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index) {
                                final int arrayIndex2 = (int) (pIndex / ((long) MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
                                final int listIndex2 = (arrayIndex2 == 1)
                                        ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                                        : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
                                currentCollection = new ParallelArrayList<>(values[arrayIndex2].get(listIndex2));
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
                        if (!collection.contains(pParameter))
                            synchronized (result) {
                                result[0] = false;
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
                if (this.size() + c.size() < 0) throw new IllegalStateException("The list has too much elements");
                if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                final java.util.ListIterator<E> list = this.listIterator(index);
                Parallel.For(c, new Parallel.Operation<E>() {
                    @Override
                    public void perform(E pParameter) {
                        synchronized (list) {
                            list.add(pParameter);
                        }
                    }

                    @Override
                    public boolean follow() {
                        return true;
                    }
                });
                mSize += c.size();
                return !c.isEmpty();
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
                            currentCollection = new ParallelArrayList<>(LongListIterator.this.collectionOf(pIndex));
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
                    currentCollection = new ParallelArrayList<>(values[arrayIndex2].get(listIndex2));
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
                    int index = 0;

                    @Override
                    public void perform(E pParameter) {
                        if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
                };
                Parallel.For(this, operation);
                return result[0];
            }

            @Override
            public int lastIndexOf(final Object o) {
                final int[] result = new int[1];
                result[0] = -1;
                Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                    int index = 0;

                    @Override
                    public void perform(E pParameter) {
                        if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
                };
                Parallel.For(this, operation);
                return result[0];
            }

            @NotNull
            @Override
            public final java.util.ListIterator<E> listIterator() {
                return this.listIterator(-1);
            }

            @NotNull
            @Override
            public final java.util.ListIterator<E> listIterator(final int index) {
                if ((index != -1 && index < 0) || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                final java.util.List<E> list = this;
                return new java.util.ListIterator<E>() {

                    private transient int mIndex = index;

                    @Override
                    public boolean hasNext() {
                        return mIndex + 1 < list.size();
                    }

                    @Override
                    public E next() {
                        return list.get(++mIndex);
                    }

                    @Override
                    public boolean hasPrevious() {
                        return mIndex - 1 >= 0;
                    }

                    @Override
                    public E previous() {
                        return list.get(--mIndex);
                    }

                    @Override
                    public int nextIndex() {
                        return mIndex + 1;
                    }

                    @Override
                    public int previousIndex() {
                        return mIndex - 1;
                    }

                    @Override
                    public void remove() {
                        list.remove(mIndex);
                    }

                    @Override
                    public void set(E e) {
                        list.set(mIndex,e);
                    }

                    @Override
                    public void add(E e) {
                        list.add(mIndex+1,e);
                    }
                };
            }

            @NotNull
            @Override
            public final java.util.List<E> subList(int fromIndex, int toIndex) {
                return currentCollection.subList(fromIndex, toIndex);
            }
        };
    }

    @NotNull
    @Override
    public Long count(@Nullable final E pEntity) {
        final long[] result = new long[1];
        for (java.util.List<E> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {
                @Override
                public void perform(E pParameter) {
                    if ((pEntity == pParameter) || (pParameter != null && pParameter.equals(pEntity))) {
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
    public int nextIndex() {
        if (getIndex() + 1 != Long.MAX_VALUE && getIndex() + 1 < mSize) {
            return indexInCollectionOf(getIndex() + 1);
        } else {
            if (mSize % ((long)MAX_COLLECTION_SIZE + 1) == 0)
                return Integer.MAX_VALUE;
            else
                return (int) (mSize % ((long)MAX_COLLECTION_SIZE + 1));
        }
    }

    @Override
    public final int previousIndex() {
        if (getIndex() != NULL_INDEX && getIndex() - 1 >= MIN_INDEX) {
            return indexInCollectionOf(getIndex() - 1);
        } else {
            return NULL_INDEX;
        }
    }

    @Override
    public void remove() {
        if (getIndex() == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
        this.remove(getIndex());
    }

    @Override
    public void set(@Nullable E e) {
        if (getIndex() == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
        final int arrayIndex = (int) (getIndex() / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((getIndex() % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (getIndex() / ((long) MAX_COLLECTION_SIZE + 1));
        values[arrayIndex].get(listIndex).set(this.indexInCollectionOf(getIndex()), e);
        mCurrentCollection.currentCollection.set(this.indexInCollectionOf(getIndex()),e);
    }

    //TODO
    @Override
    public final void add(@Nullable final E pData) {
        if (mSize == Long.MAX_VALUE) throw new IllegalStateException("The collection is full");
        final long index;
        if (getIndex() == NULL_INDEX)
            index = MIN_INDEX;
        else
            index = getIndex() + 1;

        final int arrayIndex = (int) (index / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((index % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (index / ((long) MAX_COLLECTION_SIZE + 1));
        if (index == mSize) {
            this.addAtEnd(pData);
        } else {
            if (values[arrayIndex].size() - 1 > listIndex || arrayIndex < values.length - 1) {
                int arrayIndex2 = arrayIndex;
                int listIndex2 = listIndex;
                E lastData = values[arrayIndex2].get(listIndex2).removeLast();
                values[arrayIndex2].get(listIndex2).add(indexInCollectionOf(index),pData);
                do {
                    if (listIndex2 < values[arrayIndex2].size() - 1) {
                        listIndex2++;
                    } else {
                        arrayIndex2++;
                        listIndex2 = 0;
                    }
                    final E data = values[arrayIndex2].get(listIndex2).removeLast();
                    values[arrayIndex2].get(listIndex2).addFirst(lastData);
                    lastData = data;
                } while (arrayIndex2 < values.length && listIndex2 < values[arrayIndex2].size());
            } else {
                values[arrayIndex].get(listIndex).add(indexInCollectionOf(index),pData);
            }
            if (mCurrentCollection.currentCollection != null && index / MAX_COLLECTION_SIZE == getCollectionIndex()) {
                if (mCurrentCollection.currentCollection.size() == Integer.MAX_VALUE) {
                    mCurrentCollection.currentCollection.remove(Integer.MAX_VALUE-1);
                }
                mCurrentCollection.currentCollection.add(indexInCollectionOf(index),pData);
            }
            mSize++;
        }
    }

    @Override
    public final boolean addAtEnd(@Nullable E pData) {
        if (mSize == Long.MAX_VALUE) throw new IllegalStateException("The list is full");
        final int arrayIndex = (int) (mSize / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final ParallelLinkedList<E> collection;
        if (mSize % MAX_COLLECTION_SIZE == 0) {
            if (values[arrayIndex].size() == Integer.MAX_VALUE) {
                values[arrayIndex+1].add(collection = new ParallelLinkedList<E>());
            } else {
                values[arrayIndex].add(collection =  new ParallelLinkedList<E>());
            }
        } else {
            collection = values[arrayIndex].get(values[arrayIndex].size() - 1);
        }
        boolean result = collection.add(pData);
        if (result) {
            if (++mSize / MAX_COLLECTION_SIZE == getCollectionIndex()) {
                mCurrentCollection.currentCollection.add(indexInCollectionOf(mSize - 1), pData);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean remove(@Nullable final E pData) {
        final boolean[] result = new boolean[1];
        final long[] index = new long[1];
        index[0] = MIN_INDEX;
        final LinkedList<E>[] target = new LinkedList[1];
        final ParallelArrayList<ParallelLinkedList<E>>[] followTarget = new ParallelArrayList[2];
        followTarget[0] = new ParallelArrayList<>();
        followTarget[1] = new ParallelArrayList<>();
        for (final Collection<ParallelLinkedList<E>> lla : values) {
            Parallel.For(new ParallelArrayList<>(lla), new Parallel.Operation<ParallelLinkedList<E>>() {
                @Override
                public void perform(ParallelLinkedList<E> pParameter) {
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
                for (ArrayList<ParallelLinkedList<E>> ll : followTarget) {
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
                if (--mSize == 0)
                    this.setIndex((long)NULL_INDEX);
                else if (index[0] <= getIndex()) {
                    this.setIndex(getIndex()-1);
                }
                for (SizeListener sizeListener : sizeListeners) {
                    sizeListener.change();
                }
                if (index[0] / MAX_COLLECTION_SIZE <= getCollectionIndex() && getIndex() / MAX_COLLECTION_SIZE == mCurrentCollection.mCollectionIndex) {
                    mCurrentCollection.currentCollection = new ParallelArrayList<>(collectionOf(getIndex()));
                }
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
        for (java.util.List<E> collection : this.allCollections()) {
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

        for (java.util.List<E> collection : delete.allCollections()) {
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
        this.setIndex((long)NULL_INDEX);
        mSize = 0L;
        mCurrentCollection = new ListIterator();
    }

    @Override
    public final <E2 extends E> void addAll(@NotNull final Long pIndex, @NotNull final CollectionIterator<E2, Long> pCollectionIterator) {
        if (Long.MAX_VALUE - mSize < pCollectionIterator.size()) throw new IllegalStateException("The list has too much values");
        if (pIndex < MIN_INDEX || pIndex > mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final java.util.ListIterator<E> iterator = this.collectionsIterator(pIndex).get().listIterator(indexInCollectionOf(pIndex));
        Parallel.Operation<E2> operation = new Parallel.Operation<E2>() {

            @Override
            public void perform(E2 pParameter) {
                synchronized (iterator) {
                    iterator.add(pParameter);
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
        mSize += pCollectionIterator.size();
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
            result[0] = target[0].remove(indexInCollectionOf(pIndex));
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
            result[0] = target[0].remove(indexInCollectionOf(pIndex));
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

        if (--mSize == 0)
            this.setIndex((long)NULL_INDEX);
        else if (pIndex <= getIndex()) {
            this.setIndex(getIndex()-1);
        }
        for (SizeListener sizeListener : sizeListeners) {
            sizeListener.change();
        }
        if (pIndex / MAX_COLLECTION_SIZE == getCollectionIndex() && mSize/MAX_COLLECTION_SIZE <= getCollectionIndex()) {
            if (result[0] != mCurrentCollection.currentCollection.remove(indexInCollectionOf(pIndex))) throw new RuntimeException("The values are not the same");
        } else if (pIndex / MAX_COLLECTION_SIZE <= getCollectionIndex()) {
            mCurrentCollection.currentCollection = new ParallelArrayList<>(collectionOf(getIndex()));
        }
        return result[0];
    }

    @Override
    @Nullable
    public final E get(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        if (pIndex / MAX_COLLECTION_SIZE == getCollectionIndex()) {
            return mCurrentCollection.get(indexInCollectionOf(pIndex));
        } else {
            final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
            final int listIndex = (arrayIndex == 1)
                    ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                    : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
            return values[arrayIndex].get(listIndex).get(indexInCollectionOf(pIndex));
        }
    }

    @Override
    @Nullable
    public final E set(@NotNull final Long pIndex, @Nullable final E pData) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        final int listIndex = (arrayIndex == 1)
                ? (int) ((pIndex % (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1))) / ((long) MAX_COLLECTION_SIZE + 1))
                : (int) (pIndex / ((long) MAX_COLLECTION_SIZE + 1));
        final ParallelLinkedList<E> collection = values[arrayIndex].get(listIndex);
        try {
            return collection.set(indexInCollectionOf(pIndex), pData);
        } finally {
            if (pIndex / MAX_COLLECTION_SIZE == getCollectionIndex()) {
                mCurrentCollection.set(indexInCollectionOf(pIndex), pData);
            }
        }
    }

    /**
     *
     * @param pIndex Index
     * @param pData data
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > size())
     * @throws IllegalStateException if the list is full
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
                values[arrayIndex].add(new ParallelLinkedList<E>() {
                    {
                        if (!add(pData)) throw new RuntimeException("The value has not been added");
                    }
                });
            } else {
                if (values[arrayIndex].get(listIndex).size() == Integer.MAX_VALUE) {
                    int arrayIndex2 = arrayIndex;
                    int listIndex2 = listIndex;
                    ParallelLinkedList<E> collection = null;
                    E lastData = null;
                    do {
                        if (values[arrayIndex2].size() - (listIndex2 + 1) > 0) {
                            final E data = values[arrayIndex2].get(listIndex2).removeLast();
                            if (arrayIndex2 == arrayIndex && listIndex2 == listIndex) {
                                values[arrayIndex2].get(listIndex2).add(indexInCollectionOf(pIndex),pData);
                            } else {
                                if (values[arrayIndex2].size() < listIndex2 + 1) {
                                    //For Parallel.For final value
                                    final E data2 = lastData;
                                    values[arrayIndex2].add(new ParallelLinkedList<E>() {
                                        {
                                            if (!add(data2)) throw new RuntimeException("The value has not been added");
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
                            collection = values[arrayIndex2].get(listIndex2);
                            collection.add(0,lastData);
                            lastData = null;
                        } else {
                            final E data2 = lastData;
                            values[arrayIndex2+1].add(collection = new ParallelLinkedList<E>() {
                                {
                                    if (!add(data2)) throw new RuntimeException("The value has not been added");
                                }
                            });
                            lastData = null;
                        }
                    } while (collection == null);

                } else {
                    values[arrayIndex].get(listIndex).add(indexInCollectionOf(pIndex),pData);
                }
            }
            if (pIndex / MAX_COLLECTION_SIZE == getCollectionIndex() && ++mSize / MAX_COLLECTION_SIZE == getCollectionIndex()) {
                mCurrentCollection.currentCollection.add(indexInCollectionOf(pIndex), pData);
            } else if (pIndex / MAX_COLLECTION_SIZE <= getCollectionIndex()) {
                mCurrentCollection.currentCollection = new ParallelArrayList<>(collectionOf(getIndex()));
            }
        }
    }

    @Override
    @NotNull
    public Long indexOf(@Nullable final E pData) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            long index = MIN_INDEX;

            @Override
            public void perform(E pParameter) {
                if ((pData == pParameter) || (pData != null && pData.equals(pParameter))) {
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
            if (result[0] != NULL_INDEX)
                break;
        }
        return result[0];
    }

    @Override
    @NotNull
    public Long lastIndexOf(@Nullable final E pData) {
        final long[] result = new long[1];
        result[0] = NULL_INDEX;
        Parallel.Operation<E> operation = new Parallel.Operation<E>() {
            long index = MIN_INDEX;

            @Override
            public void perform(E pParameter) {
                if ((pData == pParameter) || (pParameter != null && pParameter.equals(pData))) {
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
        };
        for (java.util.List<E> collection : this.allCollections()) {
            Parallel.For(collection, operation);
        }
        return result[0];
    }

    @NotNull
    @Override
    public final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator<E, Long> subListIterator(@NotNull final Long pFromIndex, @NotNull final Long pToIndex) {
        if (pFromIndex >= pToIndex || pFromIndex < MIN_INDEX || pToIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pFromIndex) + " to " + String.valueOf(pToIndex) + " (excluded)");
        final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator<E, Long> result = new LongListIterator<>();
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

            final class ListIterator implements java.util.List<E> {

                transient ParallelArrayList<E> currentCollection;
                transient long mCollectionIndex;

                private ListIterator() {
                    currentCollection = (mIndex != NULL_INDEX)?new ParallelArrayList<>(collectionOf(mIndex)):null;
                    mCollectionIndex = (mIndex == NULL_INDEX)?NULL_INDEX:mIndex/MAX_COLLECTION_SIZE;
                }


                @Override
                public final boolean addAll(final int pIndex, @NotNull final Collection<? extends E> c) {
                    if (pIndex < 0 || pIndex > this.size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    final java.util.List<E> collection = this;
                    Parallel.For(c, new Parallel.Operation<E>() {
                        @Override
                        public void perform(E pParameter) {
                            synchronized (collection) {
                                collection.add(pIndex,pParameter);
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
                    final E result = currentCollection.set(index, element);
                    final E result2 = LongListIterator.this.set(((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, element);
                    if ((result != result2) && (result2 != null && !result2.equals(result))) throw new RuntimeException("The values are not the same");
                    return result2;

                }

                @Override
                public final void add(final int index, final E element) {
                    if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
                    if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                    currentCollection.add(index,element);
                    LongListIterator.this.add((mIndex/MAX_COLLECTION_SIZE)*MAX_COLLECTION_SIZE+index,element);
                }

                @Override
                public final E remove(final int index) {
                    if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                    final E result = currentCollection.remove(index);
                    final E result2 = LongListIterator.this.remove(((mIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                    if ((result != result2) && (result2 != null && !result2.equals(result))) throw new RuntimeException("The values are not the same");
                    return result2;
                }

                @Override
                public final int indexOf(final Object o) {
                    final int[] result = new int[1];
                    result[0] = -1;
                    Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                        int index = 0;

                        @Override
                        public void perform(E pParameter) {
                            if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
                    };
                    Parallel.For(this, operation);
                    return result[0];
                }

                @Override
                public final int lastIndexOf(final Object o) {
                    final int[] result = new int[1];
                    result[0] = -1;
                    Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                        int index = 0;

                        @Override
                        public void perform(E pParameter) {
                            if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
                    };
                    Parallel.For(this, operation);
                    return result[0];
                }

                @NotNull
                @Override
                public final java.util.ListIterator<E> listIterator() {
                    return this.listIterator(0);
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
                            if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
                public final <T1> T1[] toArray(@NotNull final T1[] a) {
                    return currentCollection.toArray(a);
                }

                @Override
                public final boolean add(final E e) {
                    if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
                    return currentCollection.add(e) && LongListIterator.this.addAtEnd(e);
                }

                @SuppressWarnings("unchecked")
                @Override
                public final boolean remove(final Object o) {
                    return currentCollection.remove(o) && LongListIterator.this.remove((E) o);
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
                            if (!collection.contains(pParameter)) {
                                synchronized (result) {
                                    result[0] = false;
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
                public final boolean addAll(@NotNull final Collection<? extends E> c) {
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    final Collection<E> collection = this;
                    Parallel.For(c, new Parallel.Operation<E>() {
                        @Override
                        public void perform(E pParameter) {
                            synchronized (collection) {
                                if (!collection.add(pParameter)) {
                                    synchronized (result) {
                                        result[0] = false;
                                    }
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
                                if (!collection.remove(pParameter)) {
                                    synchronized (result) {
                                        result[0] = false;
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
                public final boolean retainAll(@NotNull final Collection<?> c) {
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    final Collection<E> removes = new ArrayList<>(c.size());
                    Parallel.For(this, new Parallel.Operation<E>() {
                        @Override
                        public void perform(E pParameter) {
                            if (!c.contains(pParameter) && !removes.add(pParameter)) {
                                synchronized (result) {
                                    result[0] = false;
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
                                            final E delete = LongListIterator.this.remove((startIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE);
                                            synchronized (rollback) {
                                                rollback.put(((startIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, delete);
                                            }
                                            if ((delete == null && pParameter != null) || (pParameter != null && !pParameter.equals(delete))) {
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
                            if (((mIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) < (((mSize-1) / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE)) {
                                currentCollection = new ParallelArrayList<>(LongListIterator.this.collectionOf(mIndex));
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
            private transient ListIterator mCurrentCollection = new ListIterator();

            @Nullable
            @Override
            public E next() throws NoSuchElementException {
                if (mIndex + 1 < mSize)
                    mIndex++;
                else
                    throw new NoSuchElementException();
                if (mIndex / MAX_COLLECTION_SIZE != mCurrentCollection.mCollectionIndex) {
                    mCurrentCollection = new ListIterator();
                }
                return get();
            }

            @Nullable
            @Override
            public E get() throws IndexOutOfBoundsException {
                if (mIndex < MIN_INDEX || mIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(mIndex));
                return mCurrentCollection.get(indexInCollectionOf(mIndex));
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
                if (mIndex / MAX_COLLECTION_SIZE != mCurrentCollection.mCollectionIndex) {
                    mCurrentCollection = new ListIterator();
                }
                return get();
            }

            @Override
            public void add(@Nullable E pEntity) {
                mCurrentCollection.add(indexInCollectionOf(mIndex),pEntity);
            }

            @Override
            public void set(@Nullable E pEntity) {
                mCurrentCollection.set(indexInCollectionOf(mIndex), pEntity);
            }

            @Override
            public void remove() {
                mCurrentCollection.remove(indexInCollectionOf(mIndex));
            }

            @Override
            public boolean hasNext() {
                return mIndex + 1 < mSize;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<java.util.List<E>> collectionsIterator() {
        return collectionsIterator((long)NULL_INDEX);
    }

    //TODO
    @SuppressWarnings("unchecked")
    @Override
    public Iterator<java.util.List<E>> collectionsIterator(@NotNull final Long pIndex) {
        if ((pIndex != NULL_INDEX && pIndex < MIN_INDEX) || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        return new Iterator<java.util.List<E>>() {

            private transient final Iterator<java.util.List<E>> iterator = this;
            private transient long mIndex = pIndex;

            final class ListIterator implements java.util.List<E> {

                {
                    sizeListeners.add(new SizeListener() {
                        @Override
                        public void change() {
                            try {
                                Field field = iterator.getClass().getDeclaredField("mIndex");
                                boolean b = field.isAccessible();
                                try {
                                    field.setAccessible(true);
                                    long index = field.getLong(iterator);
                                    if (index >= mSize) {
                                        field.set(iterator,mSize-1);
                                    }
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                } finally {
                                    field.setAccessible(b);
                                }
                            } catch (NoSuchFieldException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }

                transient ParallelArrayList<E> currentCollection;
                transient long mCollectionIndex;

                private ListIterator() {
                    currentCollection = (mIndex != NULL_INDEX)?new ParallelArrayList<>(collectionOf(mIndex)):null;
                    mCollectionIndex = (mIndex != NULL_INDEX)?mIndex/MAX_COLLECTION_SIZE:NULL_INDEX;
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
                        int index = 0;

                        @Override
                        public void perform(E pParameter) {
                            if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
                    };
                    Parallel.For(this, operation);
                    return result[0];
                }

                @Override
                public final int lastIndexOf(final Object o) {
                    final int[] result = new int[1];
                    result[0] = -1;
                    Parallel.Operation<E> operation = new Parallel.Operation<E>() {
                        int index = 0;

                        @Override
                        public void perform(E pParameter) {
                            if ((o == pParameter) || (pParameter != null && pParameter.equals(o))) {
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
                    };
                    Parallel.For(this, operation);
                    return result[0];
                }

                @NotNull
                @Override
                public final java.util.ListIterator<E> listIterator() {
                    return listIterator(-1);
                }

                @NotNull
                @Override
                public final java.util.ListIterator<E> listIterator(final int index) {
                    return new java.util.ListIterator<E>() {

                        private transient long mIndex2 = (mIndex / MAX_COLLECTION_SIZE) + index;

                        @Override
                        public boolean hasNext() {
                            return indexInCollectionOf(mIndex2) + 1 < Integer.MAX_VALUE;
                        }

                        @Override
                        public E next() {
                            if (hasNext())
                                return currentCollection.get(indexInCollectionOf(++mIndex2));
                            else
                                throw new NoSuchElementException();
                        }

                        @Override
                        public boolean hasPrevious() {
                            return indexInCollectionOf(mIndex2) - 1 >= 0;
                        }

                        @Override
                        public E previous() {
                            if (hasPrevious())
                                return currentCollection.get(indexInCollectionOf(--mIndex2));
                            else
                                throw new NoSuchElementException();
                        }

                        @Override
                        public int nextIndex() {
                            return indexInCollectionOf(mIndex2) + 1;
                        }

                        @Override
                        public int previousIndex() {
                            return indexInCollectionOf(mIndex2) - 1;
                        }

                        @Override
                        public void remove() {
                            mCurrentCollection.remove(indexInCollectionOf(mIndex2));
                        }

                        @Override
                        public void set(E e) {
                            mCurrentCollection.set(indexInCollectionOf(mIndex2),e);
                        }

                        @Override
                        public void add(E e) {
                            mCurrentCollection.add(e);
                        }
                    };
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
                            if ((o == pParameter) || (pParameter != null && pParameter.equals(o)))
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
                            currentCollection = new ParallelArrayList<>(collectionOf(mIndex));
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
                                            if ((delete == null && pParameter != null) || (pParameter != null && !pParameter.equals(delete))) {
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
                                currentCollection = new ParallelArrayList<>(LongListIterator.this.collectionOf(mIndex));
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

            private ListIterator mCurrentCollection = new ListIterator();

            @Override
            public final boolean hasNext() {
                return (mIndex == NULL_INDEX)
                        ?mSize > 0
                        :mIndex + MAX_COLLECTION_SIZE < mSize && mIndex + MAX_COLLECTION_SIZE > MIN_INDEX;
            }

            @Override
            public final java.util.List<E> next() throws NoSuchElementException {
                if (mIndex == NULL_INDEX)
                    mIndex = MIN_INDEX;
                else if (mIndex + (MAX_COLLECTION_SIZE - indexInCollectionOf(mIndex)) < mSize && mIndex + MAX_COLLECTION_SIZE >= MIN_INDEX)
                    mIndex += MAX_COLLECTION_SIZE - indexInCollectionOf(mIndex);
                else
                    throw new NoSuchElementException();
                return get();
            }

            @Nullable
            @Override
            public java.util.List<E> get() throws IndexOutOfBoundsException {
                if (mIndex / MAX_COLLECTION_SIZE != mCurrentCollection.mCollectionIndex) {
                    mCurrentCollection = new ListIterator();
                }
                return mCurrentCollection;
            }

            @Override
            public boolean hasPrevious() {
                return mIndex != NULL_INDEX && MIN_INDEX <= mIndex - MAX_COLLECTION_SIZE;
            }

            @Nullable
            @Override
            public java.util.List<E> previous() throws NoSuchElementException {
                if (mIndex - (MAX_COLLECTION_SIZE - indexInCollectionOf(mIndex)) >= MIN_INDEX)
                    mIndex -= MAX_COLLECTION_SIZE - indexInCollectionOf(mIndex);
                else
                    throw new NoSuchElementException();
                return get();
            }

            @Override
            public void add(@Nullable java.util.List<E> pEntity) {
                if (mCurrentCollection.size() + pEntity.size() < 0) throw new IllegalStateException("The list has too much elements");
                mCurrentCollection.addAll(indexInCollectionOf(mIndex), pEntity);
            }

            @Override
            public void set(@Nullable java.util.List<E> pEntity) {
                if (pEntity.size() != mCurrentCollection.size()) throw new IllegalStateException("The lists are not the same size");
                Parallel.For(pEntity, new Parallel.Operation<E>() {
                    private int index = 0;
                    @Override
                    public void perform(E pParameter) {
                        mCurrentCollection.set(index,pParameter);
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
            public void remove() {
                mCurrentCollection.clear();
            }

        };
    }
}
