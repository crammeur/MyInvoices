package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import ca.qc.bergeron.marcantoine.crammeur.librairy.events.ChangeListener;
import ca.qc.bergeron.marcantoine.crammeur.librairy.events.SizeListener;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Iterator;

/**
 * Created by Marc-Antoine on 2017-09-19.
 */

public class LongListIterator<E> extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.ListIterator<E, Long> {

    protected final class PartialList implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> {

        ParallelArrayList<E> values;
        transient final int firstListIndex;
        transient final int secondListIndex;
        transient final long mCollectionIndex;
        transient boolean mLock;

        private transient final ChangeListener<Long,E> mChangeListener;

        private PartialList() {
            mCollectionIndex = (getIndex() == NULL_INDEX)? NULL_INDEX: getIndex()/MAX_COLLECTION_SIZE;
            firstListIndex = (int) (mCollectionIndex / MAX_COLLECTION_SIZE);
            secondListIndex = (int) (((mCollectionIndex * MAX_COLLECTION_SIZE) % ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE)) / MAX_COLLECTION_SIZE);
            values = (mCollectionIndex == NULL_INDEX)? null: new ParallelArrayList<>(LongListIterator.this.values.get(firstListIndex).get(secondListIndex));
            mChangeListener = (mCollectionIndex == NULL_INDEX)
                    ? null
                    : new ChangeListener<Long, E>() {
                        @Override
                        public final void create(Long pIndex, E pElement) {
                            if (mLock && LongListIterator.this.size() / MAX_COLLECTION_SIZE >= mCollectionIndex) mLock = false;
                            if (pIndex / MAX_COLLECTION_SIZE == mCollectionIndex) {
                                if (values.size() == Integer.MAX_VALUE)
                                    values.remove(Integer.MAX_VALUE - 1);
                                values.add(indexInCollectionOf(pIndex), pElement);
                            }
                        }

                        @Override
                        public final void update(Long pIndex, E pElement, E pPreviousElement) {
                            if (pIndex / MAX_COLLECTION_SIZE == mCollectionIndex) {
                                final E data = values.set(indexInCollectionOf(pIndex), pElement);
                                if (pPreviousElement != data) {
                                    values.set(indexInCollectionOf(pIndex), data);
                                    throw new RuntimeException("The values are not the same");
                                }
                            }
                        }

                        @Override
                        public final void delete(Long pIndex, E pElement) {
                            if (pIndex / MAX_COLLECTION_SIZE == mCollectionIndex) {
                                final E data = values.remove(indexInCollectionOf(pIndex));
                                if (pElement != data) {
                                    values.add(indexInCollectionOf(pIndex), data);
                                    throw new RuntimeException("The values are not the same");
                                }
                            } else if (!mLock && mCollectionIndex * MAX_COLLECTION_SIZE > LongListIterator.this.size()) {
                                mLock = true;
                            }
                            if (!LongListIterator.this.isEmpty() && (LongListIterator.this.size() - 1) / MAX_COLLECTION_SIZE > mCollectionIndex && pIndex / MAX_COLLECTION_SIZE <= mCollectionIndex) {
                                values = new ParallelArrayList<>(LongListIterator.this.values.get(firstListIndex).get(secondListIndex));
                            }
                        }

                        @Override
                        public void clear() {
                            values.clear();
                            if (mCollectionIndex * MAX_COLLECTION_SIZE > LongListIterator.this.size()) mLock = true;
                        }
                    };
            if (mChangeListener != null)
                if (!mChangeListeners.add(mChangeListener)) throw new RuntimeException("ChangeListener has not been added");
            mLock = mCollectionIndex == NULL_INDEX;
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            values = null;
            if (mChangeListener != null)
                if (!mChangeListeners.remove(mChangeListener)) throw new RuntimeException("ChangeListener has not been removed");
        }

        @Override
        public final boolean addAll(final int pIndex, @NotNull final Collection<? extends E> c) {
            if (mLock) throw new IllegalStateException("PartialList is locked");
            if (pIndex < 0 || pIndex > this.size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
            final boolean[] result = new boolean[1];
            result[0] = mCollectionIndex != NULL_INDEX && !c.isEmpty() && this.size() + c.size() > 0;
            if (result[0]) {
                final java.util.List<E> collection = this;
                Parallel.For(c, new Parallel.Operation<E>() {

                    private transient int mIndex = pIndex;

                    @Override
                    public void perform(E pParameter) {
                        synchronized (collection) {
                            collection.add(mIndex,pParameter);
                        }
                        synchronized (this) {
                            mIndex++;
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
        public final E get(final int index) {
            return values.get(index);
        }

        @Override
        public final E set(final int index, final E element) {
            if (mLock) throw new IllegalStateException("PartialList is locked");
            return LongListIterator.this.set(mCollectionIndex * MAX_COLLECTION_SIZE + index, element);
        }

        @Override
        public final void add(final int index, final E element) {
            if (mLock) throw new IllegalStateException("PartialList is locked");
            if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
            if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
            LongListIterator.this.add(mCollectionIndex * MAX_COLLECTION_SIZE + index,element);
        }

        @Override
        public final E remove(final int index) {
            if (mLock) throw new IllegalStateException("PartialList is locked");
            if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
            return LongListIterator.this.remove(mCollectionIndex * MAX_COLLECTION_SIZE + index);
        }

        @Override
        public final int indexOf(final Object o) {
            final int[] result = new int[1];
            result[0] = -1;
            if (mCollectionIndex != NULL_INDEX) {
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
            }
            return result[0];
        }

        @Override
        public final int lastIndexOf(final Object o) {
            final int[] result = new int[1];
            result[0] = -1;
            if (mCollectionIndex != NULL_INDEX) {
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
            }
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
            return new java.util.ListIterator<E>() {

                private transient int mIndex = index;

                @Override
                public final boolean hasNext() {
                    return mIndex + 1 < PartialList.this.size();
                }

                @Override
                public final E next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    return PartialList.this.get(++mIndex);
                }

                @Override
                public final boolean hasPrevious() {
                    return mIndex - 1 >= 0;
                }

                @Override
                public final E previous() {
                    if (!hasPrevious()) throw new NoSuchElementException();
                    return PartialList.this.get(--mIndex);
                }

                @Override
                public final int nextIndex() {
                    return mIndex + 1;
                }

                @Override
                public final int previousIndex() {
                    return (hasPrevious())?mIndex - 1:-1;
                }

                @Override
                public final void remove() {
                    if (mIndex == -1) throw new IllegalStateException("Index :" + String.valueOf(mIndex));
                    PartialList.this.remove(mIndex--);
                }

                @Override
                public final void set(E e) {
                    if (mIndex == -1) throw new IllegalStateException("Index :" + String.valueOf(mIndex));
                    PartialList.this.set(mIndex,e);
                }

                @Override
                public final void add(E e) {
                    if (PartialList.this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
                    PartialList.this.add(mIndex+1,e);
                }
            };
        }

        @NotNull
        @Override
        public final java.util.List<E> subList(final int fromIndex, final int toIndex) {
            if (mCollectionIndex == NULL_INDEX) throw new IllegalStateException("The list is null");
            return values.subList(fromIndex, toIndex);
        }

        @Override
        public final int size() {
            return (mCollectionIndex != NULL_INDEX)?values.size():0;
        }

        @Override
        public final boolean isEmpty() {
            return mCollectionIndex == NULL_INDEX || values.isEmpty();
        }

        @Override
        public final boolean contains(final Object o) {
            final boolean[] result = new boolean[1];
            if (mCollectionIndex != NULL_INDEX) {
                Parallel.For(this, new Parallel.Operation<E>() {
                    @Override
                    public void perform(E pParameter) {
                        if ((o == pParameter) || (pParameter != null && pParameter.equals(o)))
                            synchronized (result) {
                                result[0] = true;
                            }
                    }

                    @Override
                    public boolean follow() {
                        return !result[0];
                    }
                });
            }
            return result[0];
        }

        @NotNull
        @Override
        public final java.util.Iterator<E> iterator() {
            return new java.util.Iterator<E>() {

                private transient int mIndex = -1;

                @Override
                public boolean hasNext() {
                    return mIndex + 1 < PartialList.this.size();
                }

                @Override
                public E next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    return PartialList.this.get(++mIndex);
                }

                @Override
                public void remove() {
                    if (mIndex == -1) throw new IllegalStateException(String.valueOf(-1));
                    PartialList.this.remove(mIndex--);
                }
            };
        }

        @NotNull
        @Override
        public final Object[] toArray() {
            if (mCollectionIndex == NULL_INDEX) throw new UnsupportedOperationException("toArray");
            return values.toArray();
        }

        @NotNull
        @Override
        public final <T1> T1[] toArray(@NotNull final T1[] a) {
            if (mCollectionIndex == NULL_INDEX) throw new UnsupportedOperationException("toArray");
            return values.toArray(a);
        }

        @Override
        public final boolean add(final E e) {
            if (mCollectionIndex == NULL_INDEX) throw new UnsupportedOperationException("add");
            if (mLock) throw new IllegalStateException("PartialList is locked");
            if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
            return LongListIterator.this.addAtEnd(e);
        }

        @Override
        public final boolean remove(final Object o) {
            if (mLock) throw new IllegalStateException("PartialList is locked");
            int index = -1;
            if (mCollectionIndex != NULL_INDEX) {
                index = indexOf(o);
                if (index != -1) {
                    final E data = LongListIterator.this.remove(mCollectionIndex * MAX_COLLECTION_SIZE + index);
                    if ((data != o) && !(data != null && data.equals(o))) {
                        //Rollback
                        LongListIterator.this.add(mCollectionIndex * MAX_COLLECTION_SIZE + index, data);
                        throw new RuntimeException("The values are not the same");
                    }
                }
            }
            return index != -1;
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        @Override
        public final boolean containsAll(@NotNull final Collection<?> c) {
            final boolean[] result = new boolean[1];
            result[0] = mCollectionIndex != NULL_INDEX && !c.isEmpty() && this.size() >= c.size() ;
            if (result[0]) {
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
            }
            return result[0];
        }

        @Override
        public final boolean addAll(@NotNull final Collection<? extends E> c) {
            if (mLock) throw new IllegalStateException("PartialList is locked");
            final boolean[] result = new boolean[1];
            result[0] = mCollectionIndex != NULL_INDEX && !c.isEmpty() && this.size() + c.size() > 0;
            if (result[0]) {
                final Collection<E> collection = this;
                Parallel.For(c, new Parallel.Operation<E>() {
                    @Override
                    public void perform(E pParameter) {
                        synchronized (collection) {
                            if (!collection.add(pParameter))
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
            }
            return result[0];
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        @Override
        public final boolean removeAll(@NotNull final Collection<?> c) {
            if (mLock) throw new IllegalStateException("PartialList is locked");
            final boolean[] result = new boolean[1];
            result[0] = mCollectionIndex != NULL_INDEX && !c.isEmpty() && this.size() >= c.size();
            if (result[0]) {
                final Collection<E> collection = this;
                Parallel.For(c, new Parallel.Operation<Object>() {
                    @Override
                    public void perform(Object pParameter) {
                        if (!collection.remove(pParameter))
                            synchronized (result) {
                                result[0] = false;
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
        public final boolean retainAll(@NotNull final Collection<?> c) {
            if (mLock) throw new IllegalStateException("PartialList is locked");
            final boolean[] result = new boolean[1];
            result[0] = mCollectionIndex != NULL_INDEX && !c.isEmpty() && this.size() >= c.size();
            final Collection<E> removes = new ArrayList<>(c.size());
            if (result[0]) {
                Parallel.For(this, new Parallel.Operation<E>() {
                    @Override
                    public void perform(E pParameter) {
                        if (!c.contains(pParameter)) {
                            if (!removes.add(pParameter))
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
            }
            return result[0] && this.removeAll(removes);
        }

        @Override
        public final void clear() {
            if (mLock) throw new IllegalStateException("PartialList is locked");
            if (mCollectionIndex != NULL_INDEX && !this.isEmpty()) {
                final Runtime runtime = Runtime.getRuntime();
                if (!LongListIterator.this.isEmpty() && mCollectionIndex <= (LongListIterator.this.size() - 1) / MAX_COLLECTION_SIZE ) {
                    {
                        final ParallelLinkedList<E> parallelLinkedList = LongListIterator.this.values.get(firstListIndex).remove(secondListIndex);
                        if (!values.equals(new ParallelArrayList<>(parallelLinkedList))) {
                            LongListIterator.this.values.get(firstListIndex).add(secondListIndex,parallelLinkedList);
                            throw new RuntimeException("The values are not the same");
                        }
                        mSize -= parallelLinkedList.size();
                    }
                    if (runtime.freeMemory() < runtime.maxMemory() * 0.3f)
                        runtime.gc();

                    int nextFirstIndex = firstListIndex + 1;
                    while (nextFirstIndex < LongListIterator.this.values.size()) {
                        final ParallelLinkedList<E> parallelLinkedList1 = LongListIterator.this.values.get(nextFirstIndex).remove(0);
                        if (!LongListIterator.this.values.get(nextFirstIndex-1).add(parallelLinkedList1)) throw new RuntimeException("The list have not been added");
                        nextFirstIndex++;
                    }

                    if (getIndex() >= mCollectionIndex * MAX_COLLECTION_SIZE) {
                        setIndex((getIndex() - MAX_COLLECTION_SIZE >= MIN_INDEX)? getIndex() - MAX_COLLECTION_SIZE: NULL_INDEX);
                    }

                    if (!LongListIterator.this.isEmpty() && mCollectionIndex <= (LongListIterator.this.size() - 1) / MAX_COLLECTION_SIZE)
                        values = new ParallelArrayList<>(LongListIterator.this.values.get(firstListIndex).get(secondListIndex));
                    else
                        values.clear();
                } else {
                    values.clear();
                }

                if (runtime.freeMemory() < runtime.maxMemory() * 0.3f)
                    runtime.gc();
            }
        }

        @Override
        public final boolean isLocked() {
            return mLock;
        }
    }

    private final ParallelArrayList<ParallelArrayList<ParallelLinkedList<E>>> values = new ParallelArrayList<>();
    private transient long mIndex = NULL_INDEX;
    private transient long mSize = 0L;

    private transient final ParallelArrayList<ChangeListener<Long,E>> mChangeListeners = new ParallelArrayList<>();
    private transient PartialList mCurrentCollection = new PartialList();

    private transient final ParallelArrayList<SizeListener<Long>> mSizeListeners = new ParallelArrayList<>();

    public LongListIterator(final CollectionIterator<E,? extends Serializable> pCollectionIterator) {
        if (!Number.class.isAssignableFrom(pCollectionIterator.size().getClass()) && pCollectionIterator.size().toString().compareTo("\uFFFF\uFFFF\uFFFF\u7FFF") > 0) {
            throw new RuntimeException("The collection have too much elements");
        }
        final Parallel.Operation<Collection<E>> operation = new Parallel.Operation<Collection<E>>() {
            @Override
            public void perform(Collection<E> pParameter) {
                final int firstIndex = (int) (mSize / ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
                final ParallelLinkedList<E> parallelLinkedList = new ParallelLinkedList<>(pParameter);
                synchronized (LongListIterator.this) {
                    if (values.size() - 1 < firstIndex)
                        values.add(new ParallelArrayList<ParallelLinkedList<E>>());
                    values.get(firstIndex).add(parallelLinkedList);
                    mSize+=parallelLinkedList.size();
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
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mIndex = NULL_INDEX;
        mSize = 0L;
        mChangeListeners.clear();
        mCurrentCollection = null;
        mSizeListeners.clear();
        final Runtime runtime = Runtime.getRuntime();
        if (runtime.freeMemory() < runtime.maxMemory() * 0.3f)
            runtime.gc();
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
        if (mIndex / MAX_COLLECTION_SIZE != mCurrentCollection.mCollectionIndex || (mIndex == NULL_INDEX && mCurrentCollection.mCollectionIndex != NULL_INDEX)) {
            mCurrentCollection = new PartialList();
        }
    }

    @Override
    public final boolean hasActual() {
        return getIndex() >= MIN_INDEX && getIndex() < this.size() && getIndex() != NULL_INDEX;
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
    public final boolean isEmpty() {
        return this.size() == 0;
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
    public final boolean hasNext() {
        return getIndex() + 1 < this.size() && getIndex() + 1 >= MIN_INDEX;
    }

    @Nullable
    @Override
    public final E next() throws NoSuchElementException {
        if (getIndex() == NULL_INDEX)
            this.setIndex((long)MIN_INDEX);
        else if (getIndex() + 1 < this.size() && getIndex() + 1 >= MIN_INDEX)
            this.setIndex(this.getIndex()+1);
        else
            throw new NoSuchElementException();
        return get();
    }

    @Nullable
    public final E get() throws IndexOutOfBoundsException {
        return getCollection().get(indexInCollection());
    }

    @Override
    public final boolean hasPrevious() {
        return getIndex() - 1 >= MIN_INDEX;
    }

    @Nullable
    @Override
    public final E previous() throws NoSuchElementException {
        if (getIndex() - 1 >= MIN_INDEX)
            this.setIndex(this.getIndex()-1);
        else
            throw new NoSuchElementException();

        return get();
    }

    @Override
    public final boolean hasNextCollection() {
        return (getIndex() + (MAX_COLLECTION_SIZE - indexInCollection()) < this.size() && getIndex() + (MAX_COLLECTION_SIZE - indexInCollection()) >= MIN_INDEX) || (getIndex() == NULL_INDEX && this.size() != 0);
    }

    @Override
    @NotNull
    public final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> nextCollection() throws NoSuchElementException {
        if (getIndex() == NULL_INDEX)
            this.setIndex((long)MIN_INDEX);
        else if (getIndex() + (MAX_COLLECTION_SIZE - indexInCollection()) < this.size() && getIndex() + (MAX_COLLECTION_SIZE - indexInCollection()) >= MIN_INDEX)
            this.setIndex(getIndex() + (MAX_COLLECTION_SIZE - indexInCollection()));
        else
            throw new NoSuchElementException();

        return getCollection();
    }

    @Override
    public ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> getCollection() throws IndexOutOfBoundsException {
        if (getIndex() == NULL_INDEX) throw new IndexOutOfBoundsException(String.valueOf(NULL_INDEX));
        if (getIndex() / MAX_COLLECTION_SIZE != getCollectionIndex()) {
            mCurrentCollection = new PartialList();
            final Runtime runtime = Runtime.getRuntime();
            if (Runtime.getRuntime().freeMemory() < Runtime.getRuntime().maxMemory() * 0.3)
                runtime.gc();
        }
        return mCurrentCollection;
    }

    @Override
    public final boolean hasPreviousCollection() {
        return getIndex() - indexInCollection() - MAX_COLLECTION_SIZE >= MIN_INDEX;
    }

    @NotNull
    @Override
    public final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> previousCollection() throws NoSuchElementException {
        if (hasPreviousCollection())
            this.setIndex(getIndex() - indexInCollection() - MAX_COLLECTION_SIZE);
        else
            throw new NoSuchElementException();

        return getCollection();
    }

    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public final Iterable<ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E>> allCollections() {
        return new Iterable<ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E>>() {
            @NotNull
            @Override
            public final Iterator<ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E>> iterator() {
                return LongListIterator.this.<ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E>>collectionsIterator();
            }
        };
    }

    //TODO
    @Override
    @NotNull
    public final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> collectionOf(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int firstIndex = (int) (pIndex / ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
        final int secondIndex = (int) ((pIndex % ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE)) / MAX_COLLECTION_SIZE);
        return new ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E>() {

            //Performance array list
            private ParallelArrayList<E> values = new ParallelArrayList<>(LongListIterator.this.values.get(firstIndex).get(secondIndex));
            private transient final long mCollectionIndex = pIndex / MAX_COLLECTION_SIZE;
            private transient boolean mLock = false;

            private transient final ChangeListener<Long,E> changeListener = new ChangeListener<Long, E>() {
                @Override
                public void create(Long pIndex2, E pElement) {
                    if (mLock && (LongListIterator.this.size() - 1) / MAX_COLLECTION_SIZE <= mCollectionIndex) mLock = false;
                    if (mCollectionIndex == pIndex2 /MAX_COLLECTION_SIZE) {
                        if (values.size() == Integer.MAX_VALUE)
                            values.remove(Integer.MAX_VALUE - 1);
                        values.add(indexInCollectionOf(pIndex2), pElement);
                    }
                }

                @Override
                public final void update(Long pIndex2, E pElement, E pPreviousElement) {
                    if (mCollectionIndex == pIndex2 / MAX_COLLECTION_SIZE) {
                        final E data = values.set(indexInCollectionOf(pIndex2), pElement);
                        if (pPreviousElement != data) {
                            values.set(indexInCollectionOf(pIndex2), pElement);
                            throw new RuntimeException("The values are not the same");
                        }
                    }
                }

                @Override
                public void delete(Long pIndex2, E pElement) {
                    if (mCollectionIndex == pIndex2 / MAX_COLLECTION_SIZE) {
                        final E data = values.remove(indexInCollectionOf(pIndex2));
                        if (pElement != data) {
                            values.add(indexInCollectionOf(pIndex2), data);
                            throw new RuntimeException("The values are not the same");
                        }
                        if (!LongListIterator.this.isEmpty() && pIndex2 / MAX_COLLECTION_SIZE < mCollectionIndex) {
                            values = new ParallelArrayList<>(LongListIterator.this.values.get(firstIndex).get(secondIndex));
                        }
                    } else if (!mLock && mCollectionIndex > (LongListIterator.this.size() - 1) / MAX_COLLECTION_SIZE) {
                        mLock = true;
                    }
                }

                @Override
                public void clear() {
                    values.clear();
                    if (!mLock && mCollectionIndex > (LongListIterator.this.size() - 1) / MAX_COLLECTION_SIZE) mLock = true;
                }
            };

            {
                mChangeListeners.add(changeListener);
            }

            @Override
            protected final void finalize() throws Throwable {
                super.finalize();
                mChangeListeners.remove(changeListener);
            }

            @Override
            public final boolean isLocked() {
                return mLock;
            }

            @Override
            public final int size() {
                return values.size();
            }

            @Override
            public final boolean isEmpty() {
                return values.isEmpty();
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
                        if (hasNext())
                            return list.get(++mIndex);
                        else
                            throw new NoSuchElementException();
                    }

                    @Override
                    public void remove() {
                        if (mIndex == -1) throw new IllegalStateException("Index :" + String.valueOf(mIndex));
                        list.remove(mIndex--);
                    }
                };
            }

            @NotNull
            @Override
            public final Object[] toArray() {
                return values.toArray();
            }

            @NotNull
            @Override
            public final <T1> T1[] toArray(@NotNull T1[] a) {
                return values.toArray(a);
            }

            @Override
            public final boolean add(E e) {
                if (mLock) throw new IllegalStateException("PartialList is locked");
                return this.size() != Integer.MAX_VALUE && LongListIterator.this.addAtEnd(e);
            }

            @SuppressWarnings("unchecked")
            @Override
            public final boolean remove(final Object o) {
                if (mLock) throw new IllegalStateException("PartialList is locked");
                if (!this.isEmpty()) {
                    final int index = this.indexOf(o);
                    final boolean result = index != -1;
                    if (result) {
                        final E data = values.get(index);
                        final E data2 = LongListIterator.this.remove((mCollectionIndex * MAX_COLLECTION_SIZE) + index);
                        if (data != data2) {
                            //Rollback
                            LongListIterator.this.add((mCollectionIndex * MAX_COLLECTION_SIZE) + index, data2);
                            throw new RuntimeException("The values are not the same");
                        }
                    }
                    return result;
                } else {
                    return false;
                }
            }

            @SuppressWarnings("SuspiciousMethodCalls")
            @Override
            public final boolean containsAll(@NotNull Collection<?> c) {
                final boolean[] result = new boolean[1];
                result[0] = !c.isEmpty() && this.size() >= c.size();
                if (result[0]) {
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
                }
                return result[0];
            }

            @Override
            public final boolean addAll(@NotNull Collection<? extends E> c) {
                if (mLock) throw new IllegalStateException("PartialList is locked");
                final boolean[] result = new boolean[1];
                result[0] = !c.isEmpty() && this.size() + c.size() > 0;
                if (result[0]) {
                    final Collection<E> collection = this;
                    Parallel.For(c, new Parallel.Operation<E>() {
                        @Override
                        public void perform(E pParameter) {
                            synchronized (collection) {
                                if (!collection.add(pParameter))
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
                }
                return result[0];
            }

            @Override
            public final boolean addAll(final int index, @NotNull Collection<? extends E> c) {
                if (mLock) throw new IllegalStateException("PartialList is locked");
                if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                final boolean result;
                if (result = (!c.isEmpty() && this.size() + c.size() > 0)) {
                    Parallel.For(c, new Parallel.Operation<E>() {

                        private transient long mIndex = (mCollectionIndex * MAX_COLLECTION_SIZE) + index;

                        @Override
                        public void perform(E pParameter) {
                            synchronized (LongListIterator.this) {
                                LongListIterator.this.add(mIndex,pParameter);
                            }
                            synchronized (this) {
                                mIndex++;
                            }
                        }

                        @Override
                        public boolean follow() {
                            return true;
                        }
                    });
                }
                return result;
            }

            @SuppressWarnings("SuspiciousMethodCalls")
            @Override
            public final boolean removeAll(@NotNull final Collection<?> c) {
                if (mLock) throw new IllegalStateException("PartialList is locked");
                final boolean[] result = new boolean[1];
                result[0] = !c.isEmpty() && this.size() >= c.size();
                if (result[0]) {
                    final Collection<E> collection = this;
                    Parallel.For(c, new Parallel.Operation<Object>() {
                        @Override
                        public void perform(Object pParameter) {
                            if (!collection.remove(pParameter))
                                synchronized (result) {
                                    result[0] = false;
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
            public boolean retainAll(@NotNull final Collection<?> c) {
                if (mLock) throw new IllegalStateException("PartialList is locked");
                final boolean[] result = new boolean[1];
                result[0] = !c.isEmpty() && c.size() <= this.size();
                final Collection<E> removes = new ArrayList<>(c.size());
                if (result[0]) {
                    Parallel.For(this, new Parallel.Operation<E>() {
                        @Override
                        public void perform(E pParameter) {
                            if (!c.contains(pParameter)) {
                                if (!removes.add(pParameter))
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
                }
                return result[0] && this.removeAll(removes);
            }

            @Override
            public final void clear() {
                if (mLock) throw new IllegalStateException("PartialList is locked");
                if (!this.isEmpty()) {
                    final Runtime runtime = Runtime.getRuntime();
                    if (!LongListIterator.this.isEmpty()) {
                        // {} for runtime.gc
                        {
                            final ParallelLinkedList<E> parallelLinkedList = LongListIterator.this.values.get(firstIndex).remove(secondIndex);
                            if (!values.equals(new ParallelArrayList<>(parallelLinkedList))) {
                                LongListIterator.this.values.get(firstIndex).add(secondIndex,parallelLinkedList);
                                throw new RuntimeException("The values are not the same");
                            }
                            mSize -= parallelLinkedList.size();
                        }
                        if (runtime.freeMemory() < runtime.maxMemory() * 0.3f)
                            runtime.gc();
                        int nextFirstIndex = firstIndex + 1;
                        while (nextFirstIndex < LongListIterator.this.values.size()) {
                            final ParallelLinkedList<E> parallelLinkedList1 = LongListIterator.this.values.get(nextFirstIndex).remove(0);
                            if (!LongListIterator.this.values.get(nextFirstIndex-1).add(parallelLinkedList1)) throw new RuntimeException("The list has not been added");
                            nextFirstIndex++;
                        }
                        if (getIndex() >= pIndex) {
                            setIndex((getIndex() - MAX_COLLECTION_SIZE >= MIN_INDEX)? getIndex() - MAX_COLLECTION_SIZE: NULL_INDEX);
                        }
                        if (!LongListIterator.this.isEmpty() && mCollectionIndex <= (LongListIterator.this.size() - 1) / MAX_COLLECTION_SIZE)
                            values = new ParallelArrayList<>(LongListIterator.this.values.get(firstIndex).get(secondIndex));
                    } else {
                        values.clear();
                    }

                    if (runtime.freeMemory() < runtime.maxMemory() * 0.3f)
                        runtime.gc();
                }
            }

            @Override
            public final E get(int index) {
                return values.get(index);
            }

            @Override
            public final E set(int index, E element) {
                if (mLock) throw new IllegalStateException("PartialList is locked");
                if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                final E result = values.set(index, element);
                final E result2 = LongListIterator.this.set((mCollectionIndex * MAX_COLLECTION_SIZE) + index, element);
                if (!(result == result2) && !(result2 != null && result2.equals(result))) {
                    //Rollback
                    LongListIterator.this.set((mCollectionIndex * MAX_COLLECTION_SIZE) + index, result2);
                    throw new RuntimeException("The values are not the same");
                }
                return result2;
            }

            @Override
            public final void add(int index, E element) {
                if (mLock) throw new IllegalStateException("PartialList is locked");
                if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                values.add(index, element);
                LongListIterator.this.add((mCollectionIndex * MAX_COLLECTION_SIZE) + index, element);
            }

            @Override
            public final E remove(final int index) {
                if (mLock) throw new IllegalStateException("PartialList is locked");
                if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                final E result = values.remove(index);
                final E result2 = LongListIterator.this.remove((mCollectionIndex * MAX_COLLECTION_SIZE) + index);
                if (!(result == result2) && !(result2 != null && result2.equals(result))) {
                    //Rollback
                    LongListIterator.this.add((mCollectionIndex * MAX_COLLECTION_SIZE) + index, result2);
                    throw new RuntimeException("The values are not the same");
                }
                if (mCollectionIndex * MAX_COLLECTION_SIZE < ((LongListIterator.this.size()-1)/MAX_COLLECTION_SIZE)* MAX_COLLECTION_SIZE) {
                    values = new ParallelArrayList<>(LongListIterator.this.values.get(firstIndex).get(secondIndex));
                    final Runtime runtime = Runtime.getRuntime();
                    if (runtime.freeMemory() < runtime.maxMemory() * 0.3f)
                        runtime.gc();
                }
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
                        if (!hasNext()) throw new NoSuchElementException();
                        return list.get(++mIndex);
                    }

                    @Override
                    public boolean hasPrevious() {
                        return mIndex - 1 >= 0;
                    }

                    @Override
                    public E previous() {
                        if (!hasPrevious()) throw new NoSuchElementException();
                        return list.get(--mIndex);
                    }

                    @Override
                    public int nextIndex() {
                        return mIndex + 1;
                    }

                    @Override
                    public int previousIndex() {
                        return (mIndex == -1)? -1: mIndex - 1;
                    }

                    @Override
                    public void remove() {
                        if (mIndex == -1) throw new IllegalStateException("Index :" + String.valueOf(mIndex));
                        list.remove(mIndex--);
                    }

                    @Override
                    public void set(E e) {
                        list.set(mIndex,e);
                    }

                    @Override
                    public void add(E e) {
                        if (list.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
                        list.add(mIndex+1,e);
                    }
                };
            }

            @NotNull
            @Override
            public final java.util.List<E> subList(int fromIndex, int toIndex) {
                return values.subList(fromIndex, toIndex);
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
    public final int nextIndex() {
        if (getIndex() + 1 != Long.MAX_VALUE && getIndex() + 1 < this.size()) {
            return indexInCollectionOf(getIndex() + 1);
        } else {
            if (this.size() % ((long)MAX_COLLECTION_SIZE + 1) == 0)
                return Integer.MAX_VALUE;
            else
                return (int) (this.size() % ((long)MAX_COLLECTION_SIZE + 1));
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
    public final void remove() {
        if (getIndex() == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
        this.remove(getIndex());
    }

    @Override
    public void set(@Nullable final E e) {
        if (getIndex() == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
        final int firstIndex = (int) (getIndex() / ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
        final int secondIndex = (int) ((mIndex % ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE)) / MAX_COLLECTION_SIZE);
        final E data = values.get(firstIndex).get(secondIndex).set(this.indexInCollection(), e);

        Parallel.For(mChangeListeners, new Parallel.Operation<ChangeListener<Long,E>>() {
            @Override
            public void perform(ChangeListener<Long, E> pParameter) {
                pParameter.update(getIndex(), e, data);
            }

            @Override
            public boolean follow() {
                return true;
            }
        });
    }

    @Override
    public void add(@Nullable final E pData) {
        if (mSize == Long.MAX_VALUE) throw new IllegalStateException("The collection is full");
        final long index;
        if (getIndex() == NULL_INDEX)
            index = MIN_INDEX;
        else
            index = getIndex() + 1;

        final int firstIndex = (int) (getIndex() / ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
        final int secondIndex = (int) ((mIndex % ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE)) / MAX_COLLECTION_SIZE);
        if (index == mSize) {
            this.addAtEnd(pData);
        } else {
            if (values.get(firstIndex).size() - 1 > secondIndex || firstIndex < values.size() - 1) {
                E lastData = values.get(firstIndex).get(secondIndex).removeLast();
                values.get(firstIndex).get(secondIndex).add(indexInCollectionOf(index),pData);
                int firstIndex1 = (secondIndex == values.get(firstIndex).size() - 1 && firstIndex < values.size() - 1)? firstIndex + 1: firstIndex;
                int secondIndex1 = (firstIndex1 == firstIndex)? secondIndex + 1: 0;
                boolean run;
                do {
                    final int lasFirstIndex = firstIndex1;
                    final int lastSecondIndex = secondIndex1;
                    final E data = values.get(lasFirstIndex).get(lastSecondIndex).removeLast();
                    values.get(lasFirstIndex).get(lastSecondIndex).addFirst(lastData);
                    lastData = data;
                    if (secondIndex1 + 1 < values.get(lasFirstIndex).size()) {
                        secondIndex1++;
                    } else {
                        firstIndex1++;
                        secondIndex1 = 0;
                    }
                    if (!(run = (firstIndex1 < values.size() && secondIndex1 < values.get(firstIndex).size()))) {
                        values.get(lasFirstIndex).get(lastSecondIndex).addLast(data);
                    }
                } while (run);
            } else {
                values.get(firstIndex).get(secondIndex).add(indexInCollectionOf(index),pData);
            }

            mSize++;

            Parallel.For(mChangeListeners, new Parallel.Operation<ChangeListener<Long,E>>() {
                @Override
                public void perform(ChangeListener<Long, E> pParameter) {
                    pParameter.create(index, pData);
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });

            Parallel.For(mSizeListeners, new Parallel.Operation<SizeListener<Long>>() {
                @Override
                public void perform(SizeListener<Long> pParameter) {
                    pParameter.change(index, 1L);
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }
    }

    @Override
    public boolean addAtEnd(@Nullable final E pData) {
        if (mSize == Long.MAX_VALUE) throw new IllegalStateException("The list is full");
        final int firstIndex = (int) (mSize / ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
        final ParallelLinkedList<E> list;
        if (mSize % MAX_COLLECTION_SIZE == 0) {
            if (values.isEmpty() || (firstIndex + 1 >= values.size() && values.get(firstIndex).size() == Integer.MAX_VALUE)) {
                final ParallelArrayList<ParallelLinkedList<E>> linkedListParallelArrayList = new ParallelArrayList<>();
                linkedListParallelArrayList.add(list = new ParallelLinkedList<>());
                values.add(linkedListParallelArrayList);
            } else {
                values.get(firstIndex).add(list = new ParallelLinkedList<>());
            }
        } else {
            list = values.get(firstIndex).get(values.get(firstIndex).size() - 1);
        }
        final boolean result;
        if (result = list.add(pData)) {
            mSize++;
            Parallel.For(mChangeListeners, new Parallel.Operation<ChangeListener<Long,E>>() {
                @Override
                public void perform(ChangeListener<Long, E> pParameter) {
                    pParameter.create(mSize - 1, pData);
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }
        return result;
    }

    //TODO
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(@Nullable final E pData) {
        final boolean[] result = new boolean[1];
        result[0] = !this.isEmpty();
        if (result[0]) {
            final long[] index = new long[1];
            index[0] = MIN_INDEX;
            final LinkedList<E>[] target = new LinkedList[1];
            final ParallelArrayList<ParallelArrayList<ParallelLinkedList<E>>> followTarget = new ParallelArrayList<>();

            for (final Collection<ParallelLinkedList<E>> lla : values) {
                Parallel.For(lla, new Parallel.Operation<ParallelLinkedList<E>>() {
                    @Override
                    public void perform(final ParallelLinkedList<E> pParameter) {
                        if (target[0] == null) {
                            Parallel.For(pParameter, new Parallel.Operation<E>() {
                                @Override
                                public void perform(final E pParameter) {
                                    if (pData == pParameter || (pParameter != null && pParameter.equals(pData))) {
                                        synchronized (result) {
                                            result[0] = true;
                                        }
                                    } else {
                                        synchronized (index) {
                                            index[0]++;
                                        }
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
                            if (followTarget.isEmpty() || followTarget.get(followTarget.size()-1).size() == Integer.MAX_VALUE) {
                                if (!followTarget.add(new ParallelArrayList<ParallelLinkedList<E>>() {{if (!add(pParameter)) throw new RuntimeException("The values are not be added");}})) {
                                    throw new RuntimeException("The values are not be added");
                                }
                            } else {
                                followTarget.get(followTarget.size()-1).add(pParameter);
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

                    Parallel.For(mChangeListeners, new Parallel.Operation<ChangeListener<Long,E>>() {
                        @Override
                        public void perform(ChangeListener<Long, E> pParameter) {
                            pParameter.delete(index[0], pData);
                        }

                        @Override
                        public boolean follow() {
                            return true;
                        }
                    });

                    Parallel.For(mSizeListeners, new Parallel.Operation<SizeListener>() {
                        @Override
                        public void perform(SizeListener pParameter) {
                            pParameter.change(index[0], -1L);
                        }

                        @Override
                        public boolean follow() {
                            return true;
                        }
                    });
                }
            }
        }
        return result[0];
    }

    @Override
    public final <E2 extends E> boolean retainAll(@NotNull CollectionIterator<E2, Long> pCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = !pCollectionIterator.isEmpty();
        if (result[0]) {
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
        }
        return result[0];
    }

    @Override
    public void clear() {
        final long size = this.size();

        values.clear();
        this.setIndex((long)NULL_INDEX);
        mSize = 0L;
        mCurrentCollection = new PartialList();

        Parallel.For(mChangeListeners, new Parallel.Operation<ChangeListener<Long,E>>() {
            @Override
            public void perform(ChangeListener<Long, E> pParameter) {
                pParameter.clear();
            }

            @Override
            public boolean follow() {
                return true;
            }
        });
        Parallel.For(mSizeListeners, new Parallel.Operation<SizeListener<Long>>() {
            @Override
            public void perform(SizeListener<Long> pParameter) {
                pParameter.change(-1L, -size);
            }

            @Override
            public boolean follow() {
                return true;
            }
        });

        final Runtime runtime = Runtime.getRuntime();
        if (runtime.freeMemory() < runtime.maxMemory() * 0.3f)
            runtime.gc();
    }

    @Override
    public <E2 extends E> void addAll(@NotNull final Long pIndex, @NotNull final CollectionIterator<E2, Long> pCollectionIterator) {
        if (Long.MAX_VALUE - size() < pCollectionIterator.size()) throw new IllegalStateException("The list has too much values");
        if (pIndex < MIN_INDEX || pIndex > size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        Parallel.Operation<E2> operation = new Parallel.Operation<E2>() {

            private transient long mIndex = pIndex;

            @Override
            public void perform(E2 pParameter) {
                synchronized (LongListIterator.this) {
                    LongListIterator.this.add(mIndex, pParameter);
                }
                synchronized (this) {
                    mIndex++;
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
    public E remove(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final E[] result = (E[]) new Data[1];
        final LinkedList<E>[] target = new LinkedList[1];
        final int firstIndex = (int) (pIndex / ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
        final int secondIndex = (int) ((mIndex % ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE)) / MAX_COLLECTION_SIZE);
        target[0] = values.get(firstIndex).get(secondIndex);
        if (pIndex / MAX_COLLECTION_SIZE == (mSize - 1) / MAX_COLLECTION_SIZE) {
            result[0] = target[0].remove(indexInCollectionOf(pIndex));
        } else {
            final ArrayList<ArrayList<LinkedList<E>>> followTarget = new ParallelArrayList<>();
            for (int firstIndex1 = firstIndex; firstIndex1<values.size(); firstIndex1++) {
                if (firstIndex1 == firstIndex) {
                    Parallel.For(values.get(firstIndex1), new Parallel.Operation<LinkedList<E>>() {
                        int index = 0;
                        @Override
                        public void perform(final LinkedList<E> pParameter) {
                            if (index > secondIndex) {
                                if (followTarget.isEmpty() || followTarget.get(followTarget.size()-1).size() == Integer.MAX_VALUE) {
                                    synchronized (followTarget) {
                                        followTarget.add(new ParallelArrayList<LinkedList<E>>() {
                                            {
                                                add(pParameter);
                                            }
                                        });
                                    }
                                } else {
                                    synchronized (followTarget) {
                                        followTarget.get(followTarget.size()-1).add(pParameter);
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
                    Parallel.For(values.get(firstIndex1), new Parallel.Operation<LinkedList<E>>() {
                        @Override
                        public void perform(final LinkedList<E> pParameter) {
                            if (followTarget.get(followTarget.size()-1).size() == Integer.MAX_VALUE) {
                                synchronized (followTarget) {
                                    followTarget.add(new ParallelArrayList<LinkedList<E>>() {
                                        {
                                            add(pParameter);
                                        }
                                    });
                                }
                            } else {
                                synchronized (followTarget) {
                                    followTarget.get(followTarget.size()-1).add(pParameter);
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
            for (ArrayList<LinkedList<E>> ll : followTarget) {
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
        else if (pIndex <= getIndex())
            if (getIndex() == MIN_INDEX)
                this.setIndex((long)NULL_INDEX);
            else
                this.setIndex(getIndex()-1);

        Parallel.For(mChangeListeners, new Parallel.Operation<ChangeListener<Long, E>>() {
            @Override
            public void perform(ChangeListener<Long, E> pParameter) {
                pParameter.delete(pIndex, result[0]);
            }

            @Override
            public boolean follow() {
                return true;
            }
        });

        Parallel.For(mSizeListeners, new Parallel.Operation<SizeListener>() {
            @Override
            public void perform(SizeListener pParameter) {
                pParameter.change(pIndex, -1L);
            }

            @Override
            public boolean follow() {
                return true;
            }
        });

        return result[0];
    }

    @Override
    @Nullable
    public E get(@NotNull final Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        if (pIndex / MAX_COLLECTION_SIZE == getCollectionIndex()) {
            return mCurrentCollection.values.get(indexInCollectionOf(pIndex));
        } else {
            final int firstIndex = (int) (pIndex / ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
            final int secondIndex = (int) ((mIndex % ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE)) / MAX_COLLECTION_SIZE);
            return values.get(firstIndex).get(secondIndex).get(indexInCollectionOf(pIndex));
        }
    }

    @Override
    @Nullable
    public E set(@NotNull final Long pIndex, @Nullable final E pData) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final int firstIndex = (int) (pIndex / ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
        final int secondIndex = (int) ((mIndex % ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE)) / MAX_COLLECTION_SIZE);
        final E result = values.get(firstIndex).get(secondIndex).set(indexInCollectionOf(pIndex), pData);

        Parallel.For(mChangeListeners, new Parallel.Operation<ChangeListener<Long,E>>() {
            @Override
            public void perform(ChangeListener<Long, E> pParameter) {
                pParameter.update(pIndex, pData, result);
            }

            @Override
            public boolean follow() {
                return true;
            }
        });

        return result;
    }

    //TODO
    /**
     *
     * @param pIndex mIndex
     * @param pData data
     * @throws IndexOutOfBoundsException if the mIndex is out of range (mIndex < 0 || mIndex > size())
     * @throws IllegalStateException if the list is full
     */
    @Override
    public void add(@NotNull final Long pIndex, @Nullable final E pData) throws IllegalStateException, IndexOutOfBoundsException {
        if (mSize == Long.MAX_VALUE) throw new IllegalStateException("The list is full");
        if (pIndex < MIN_INDEX || pIndex > mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        if (pIndex == mSize) {
            if (!this.addAtEnd(pData)) throw new RuntimeException("The value has not been added");
        } else {
            final int firstIndex = (int) (pIndex / ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
            final int secondIndex = (int) ((mIndex % ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE)) / MAX_COLLECTION_SIZE);
            if (values.get(firstIndex).size()-1 < secondIndex) {
                values.get(firstIndex).add(new ParallelLinkedList<E>() {
                    {
                        if (!add(pData)) throw new RuntimeException("The value has not been added");
                    }
                });
            } else {
                if (values.get(firstIndex).get(secondIndex).size() == Integer.MAX_VALUE) {
                    E lastData = values.get(firstIndex).get(secondIndex).removeLast();
                    values.get(firstIndex).get(secondIndex).add(indexInCollectionOf(pIndex),pData);
                    int firstIndex1 = (secondIndex < Integer.MAX_VALUE - 1)? firstIndex: firstIndex+1;
                    int secondIndex1 = (firstIndex == firstIndex1)? secondIndex+1: 0;
                    ParallelLinkedList<E> collection = null;
                    do {
                        if (values.get(firstIndex1).size() - (secondIndex1 + 1) > 0 || firstIndex1 < values.size()) {
                            final E data = values.get(firstIndex1).get(secondIndex1).removeLast();
                            values.get(firstIndex1).get(secondIndex1).add(0,lastData);
                            lastData = data;
                            if (secondIndex1 == Integer.MAX_VALUE - 1) {
                                secondIndex1 = 0;
                                firstIndex1++;
                            }
                            else
                                secondIndex1++;
                        } else if (values.get(firstIndex1).get(secondIndex1).size() != Integer.MAX_VALUE) {
                            collection = values.get(firstIndex1).get(secondIndex1);
                            collection.add(0,lastData);
                            lastData = null;
                        } else {
                            final E data2 = lastData;
                            collection = new ParallelLinkedList<E>() {
                                {
                                    if (!add(data2)) throw new RuntimeException("The value has not been added");
                                }
                            };
                            final ParallelArrayList<ParallelLinkedList<E>> linkedListArrayList = new ParallelArrayList<>();
                            linkedListArrayList.add(collection);
                            values.add(linkedListArrayList);
                            lastData = null;
                        }
                    } while (collection == null);

                } else {
                    values.get(firstIndex).get(secondIndex).add(indexInCollectionOf(pIndex),pData);
                }
            }

            mSize++;

            if (pIndex <= mIndex)
                mIndex++;

            Parallel.For(mChangeListeners, new Parallel.Operation<ChangeListener<Long,E>>() {
                @Override
                public void perform(ChangeListener<Long, E> pParameter) {
                    pParameter.create(pIndex, pData);
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });

            Parallel.For(mSizeListeners, new Parallel.Operation<SizeListener<Long>>() {
                @Override
                public void perform(SizeListener<Long> pParameter) {
                    pParameter.change(pIndex, 1L);
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
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
                if ((pData == pParameter) || (pParameter != null && pParameter.equals(pData))) {
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
        if (pFromIndex >= pToIndex || pFromIndex < MIN_INDEX || pToIndex >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(pFromIndex) + " to " + String.valueOf(pToIndex) + " (excluded)");
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
            if (!operation.follow()) break;
        }
        return result;
    }

    @NotNull
    @Override
    public final Iterator<E> iterator() {
        return new Iterator<E>() {

            private transient long mIndex = NULL_INDEX;

            final class PartialList implements java.util.List<E> {

                private ParallelArrayList<E> values;
                private transient final long mCollectionIndex;

                private PartialList() {
                    final int firstIndex = (int) (mIndex / ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
                    final int secondIndex = (int) ((mIndex % ((long)MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE)) / MAX_COLLECTION_SIZE);
                    values = (mIndex == NULL_INDEX)? null: new ParallelArrayList<>(LongListIterator.this.values.get(firstIndex).get(secondIndex));
                    mCollectionIndex = (mIndex == NULL_INDEX)? NULL_INDEX: mIndex/MAX_COLLECTION_SIZE;
                }

                @Override
                protected void finalize() throws Throwable {
                    super.finalize();
                    values = null;
                }

                @Override
                public final boolean addAll(final int pIndex, @NotNull final Collection<? extends E> c) {
                    if (this.size() + c.size() < 0) throw new IllegalStateException("The collection has too much elements");
                    if (pIndex < 0 || pIndex > this.size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    if (result[0]) {
                        final java.util.List<E> list = this;
                        Parallel.For(c, new Parallel.Operation<E>() {

                            private transient int mIndex = pIndex;

                            @Override
                            public void perform(E pParameter) {
                                synchronized (list) {
                                    list.add(mIndex,pParameter);
                                }
                                synchronized (this) {
                                    mIndex++;
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
                public final E get(final int index) {
                    return values.get(index);
                }

                @Override
                public final E set(final int index, final E element) {
                    final E result = values.set(index, element);
                    final E result2 = LongListIterator.this.set(((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, element);
                    if (!(result == result2) && !(result2 != null && result2.equals(result))) {
                        //Rollback
                        LongListIterator.this.set(((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, result2);
                        throw new RuntimeException("The values are not the same");
                    }
                    return result2;

                }

                @Override
                public final void add(final int index, final E element) {
                    if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
                    if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                    values.add(index,element);
                    LongListIterator.this.add((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE + index, element);
                }

                @Override
                public final E remove(final int index) {
                    if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                    final E result = values.remove(index);
                    final E result2 = LongListIterator.this.remove(((mIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                    if (!(result == result2) && !(result2 != null && result2.equals(result))) {
                        //Rollback
                        LongListIterator.this.add(((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, result2);
                        throw new RuntimeException("The values are not the same");
                    }
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
                    return this.listIterator(-1);
                }

                @NotNull
                @Override
                public final java.util.ListIterator<E> listIterator(final int index) {
                    final PartialList partialList = this;
                    return new java.util.ListIterator<E>() {

                        private transient int mIndex = index;

                        @Override
                        public boolean hasNext() {
                            return mIndex + 1 < partialList.size();
                        }

                        @Override
                        public E next() {
                            if (hasNext())
                                return values.get(++mIndex);
                            else
                                throw new NoSuchElementException();
                        }

                        @Override
                        public boolean hasPrevious() {
                            return mIndex - 1 >= 0;
                        }

                        @Override
                        public E previous() {
                            if (hasPrevious())
                                return values.get(--mIndex);
                            else
                                throw new NoSuchElementException();
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
                            mCurrentCollection.remove(mIndex--);
                        }

                        @Override
                        public void set(E e) {
                            mCurrentCollection.set(mIndex,e);
                        }

                        @Override
                        public void add(E e) {
                            mCurrentCollection.add(mIndex+1,e);
                        }
                    };
                }

                @NotNull
                @Override
                public final java.util.List<E> subList(final int fromIndex, final int toIndex) {
                    return values.subList(fromIndex, toIndex);
                }

                @Override
                public final int size() {
                    return values.size();
                }

                @Override
                public final boolean isEmpty() {
                    return values.isEmpty();
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
                    final PartialList partialList = this;
                    return new java.util.Iterator<E>() {

                        private transient int mIndex = -1;

                        @Override
                        public boolean hasNext() {
                            return mIndex + 1 < partialList.size();
                        }

                        @Override
                        public E next() {
                            return values.get(++mIndex);
                        }

                        @Override
                        public void remove() {
                            partialList.remove(mIndex--);
                        }
                    };
                }

                @NotNull
                @Override
                public final Object[] toArray() {
                    return values.toArray();
                }

                @NotNull
                @Override
                public final <T1> T1[] toArray(@NotNull final T1[] a) {
                    return values.toArray(a);
                }

                @Override
                public final boolean add(final E e) {
                    if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
                    return values.add(e) && LongListIterator.this.addAtEnd(e);
                }

                @Override
                public final boolean remove(final Object o) {
                    int index = indexOf(o);
                    if (index != -1) {
                        final E data = values.remove(index);
                        final E data2 = LongListIterator.this.remove(((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index);
                        if (!(data == data2) && !(data2 != null && data2.equals(data))) {
                            //Rollback
                            LongListIterator.this.add(((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) + index, data2);
                            throw new RuntimeException("The values are not the same");
                        }
                    }
                    return index != -1;
                }

                @SuppressWarnings("SuspiciousMethodCalls")
                @Override
                public final boolean containsAll(@NotNull final Collection<?> c) {
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    if (result[0]) {
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
                    }
                    return result[0];
                }

                @Override
                public final boolean addAll(@NotNull final Collection<? extends E> c) {
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    if (result[0]) {
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
                    }
                    return result[0];
                }

                @SuppressWarnings("SuspiciousMethodCalls")
                @Override
                public final boolean removeAll(@NotNull final Collection<?> c) {
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    if (result[0]) {
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
                    }
                    return result[0];
                }

                @Override
                public final boolean retainAll(@NotNull final Collection<?> c) {
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    final Collection<E> removes = new ArrayList<>(c.size());
                    if (result[0]) {
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
                    }
                    return result[0] && this.removeAll(removes);
                }

                @Override
                public final void clear() {
                    if (!this.isEmpty()) {
                        if (!LongListIterator.this.isEmpty()) {
                            {
                                final ArrayList<E> arrayList = new ArrayList<>(values);
                                values.clear();
                                final java.util.Map<Long, E> rollback = new TreeMap<>();
                                try {
                                    Parallel.For(arrayList, new Parallel.Operation<E>() {
                                        final long collectionIndex = mIndex / MAX_COLLECTION_SIZE;
                                        long index = 0;
                                        @Override
                                        public void perform(E pParameter) {
                                            final E delete = LongListIterator.this.remove(collectionIndex * MAX_COLLECTION_SIZE);
                                            synchronized (rollback) {
                                                rollback.put((collectionIndex * MAX_COLLECTION_SIZE) + index, delete);
                                            }
                                            if (!(delete == pParameter) && !(pParameter != null && pParameter.equals(delete))) {
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
                            if (((mIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) < (((LongListIterator.this.size()-1) / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE)) {
                                values = new ParallelArrayList<>(LongListIterator.this.collectionOf(mIndex));
                                final Runtime runtime = Runtime.getRuntime();
                                if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                                    runtime.gc();
                            }
                        } else {
                            values.clear();
                        }
                    }
                }

            }

            private transient PartialList mCurrentCollection = new PartialList();

            private transient final SizeListener<Long> sizeListener = new SizeListener<Long>() {
                @Override
                public void change(Long pIndex, Long pDelta) {
                    if (pIndex <= mIndex) {
                        mIndex = (LongListIterator.this.size() == 0)? NULL_INDEX: mIndex + pDelta;
                    } else if (mIndex >= LongListIterator.this.size()) {
                        mIndex = (LongListIterator.this.size() == 0)? NULL_INDEX: LongListIterator.this.size() - 1;
                    }
                    if ((mIndex / MAX_COLLECTION_SIZE != mCurrentCollection.mCollectionIndex && mIndex != NULL_INDEX && mCurrentCollection.mCollectionIndex != NULL_INDEX) || (mIndex == NULL_INDEX && mCurrentCollection.mCollectionIndex != NULL_INDEX)) {
                        mCurrentCollection = new PartialList();
                    }
                }
            };

            {
                if (!mSizeListeners.add(sizeListener)) throw new RuntimeException("SizeListener has not be added");
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                mIndex = NULL_INDEX;
                mCurrentCollection = null;
                if (!mSizeListeners.remove(sizeListener)) throw new RuntimeException("SizeListener has not be removed");
            }

            @Nullable
            @Override
            public E next() throws NoSuchElementException {
                if (mIndex == NULL_INDEX)
                    mIndex = MIN_INDEX;
                else if (mIndex + 1 < mSize)
                    mIndex++;
                else
                    throw new NoSuchElementException();
                if (mIndex / MAX_COLLECTION_SIZE != mCurrentCollection.mCollectionIndex) {
                    mCurrentCollection = new PartialList();
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
                return mIndex != NULL_INDEX && mIndex - 1 >= MIN_INDEX ;
            }

            @Nullable
            @Override
            public E previous() throws NoSuchElementException {
                if (mIndex == NULL_INDEX || mIndex - 1 < MIN_INDEX)
                    throw new NoSuchElementException();
                else
                    mIndex--;
                if (mIndex / MAX_COLLECTION_SIZE != mCurrentCollection.mCollectionIndex) {
                    mCurrentCollection = new PartialList();
                }
                return get();
            }

            @Override
            public void add(@Nullable E pEntity) {
                mCurrentCollection.add(indexInCollectionOf(mIndex+1),pEntity);
            }

            @Override
            public void set(@Nullable E pEntity) {
                mCurrentCollection.set(indexInCollectionOf(mIndex), pEntity);
            }

            @Override
            public void remove() {
                mCurrentCollection.remove(indexInCollectionOf(mIndex--));
            }

            @Override
            public boolean hasNext() {
                return mIndex + 1 < size();
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Iterator<ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E>> collectionsIterator() {
        return collectionsIterator((long)NULL_INDEX);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Iterator<ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E>> collectionsIterator(@NotNull final Long pIndex) {
        if ((pIndex != NULL_INDEX && pIndex < MIN_INDEX) || pIndex >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        return new Iterator<ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E>>() {

            final class PartialList implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> {

                private ParallelArrayList<E> values;
                private transient final long mCollectionIndex;

                private PartialList() {
                    values = (mIndex != NULL_INDEX)?new ParallelArrayList<>(collectionOf(mIndex)):null;
                    mCollectionIndex = (mIndex != NULL_INDEX)?mIndex/MAX_COLLECTION_SIZE:NULL_INDEX;
                }

                @Override
                protected void finalize() throws Throwable {
                    super.finalize();
                    values = null;
                }

                @Override
                public final boolean addAll(final int pIndex, @NotNull final Collection<? extends E> c) {
                    if (pIndex < 0 || pIndex > this.size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
                    if (this.size() + c.size() < 0) throw new IllegalStateException("The collection have too much elements");
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    if (result[0]) {
                        final java.util.List<E> list = this;
                        Parallel.For(c, new Parallel.Operation<E>() {

                            private transient int index = pIndex;

                            @Override
                            public void perform(E pParameter) {
                                synchronized (list) {
                                    list.add(index,pParameter);
                                }
                                synchronized (this) {
                                    index++;
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
                public final E get(final int index) {
                    return values.get(index);
                }

                @Override
                public final E set(final int index, final E element) {
                    final E result = values.set(index,element);
                    final E result2 = LongListIterator.this.set((mCollectionIndex * MAX_COLLECTION_SIZE) + index, element);
                    if (!(result == result2) && !(result2 != null && result2.equals(result))) {
                        //Rollback
                        LongListIterator.this.set((mCollectionIndex * MAX_COLLECTION_SIZE) + index, result2);
                        throw new RuntimeException("The values are not the same");
                    }
                    return result2;
                }

                @Override
                public final void add(final int index, final E element) {
                    if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The list is full");
                    if (index < 0 || index > this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                    values.add(index,element);
                    LongListIterator.this.add(mCollectionIndex*MAX_COLLECTION_SIZE+index,element);
                    if (index <= indexInCollectionOf(mIndex))
                        mIndex++;
                }

                @Override
                public final E remove(final int index) {
                    if (index < 0 || index >= this.size()) throw new IndexOutOfBoundsException(String.valueOf(index));
                    final E result = values.remove(index);
                    final long index2 = (mCollectionIndex * MAX_COLLECTION_SIZE) + index;
                    final E result2 = LongListIterator.this.remove(index2);
                    if ((result != result2) && (result2 != null && !result2.equals(result))) {
                        //Rollback
                        LongListIterator.this.add(index2,result2);
                        throw new RuntimeException("The values are not the same");
                    }
                    if (index <= indexInCollectionOf(mIndex))
                        mIndex--;
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
                    final PartialList partialList = this;
                    return new java.util.ListIterator<E>() {

                        private transient int mIndex = index;

                        @Override
                        public boolean hasNext() {
                            return mIndex + 1 < partialList.size();
                        }

                        @Override
                        public E next() {
                            if (hasNext())
                                return values.get(++mIndex);
                            else
                                throw new NoSuchElementException();
                        }

                        @Override
                        public boolean hasPrevious() {
                            return mIndex - 1 >= 0;
                        }

                        @Override
                        public E previous() {
                            if (hasPrevious())
                                return values.get(--mIndex);
                            else
                                throw new NoSuchElementException();
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
                            partialList.remove(mIndex--);
                        }

                        @Override
                        public void set(E e) {
                            partialList.set(mIndex,e);
                        }

                        @Override
                        public void add(E e) {
                            partialList.add(mIndex+1,e);
                        }
                    };
                }

                @NotNull
                @Override
                public final java.util.List<E> subList(final int fromIndex, final int toIndex) {
                    return values.subList(fromIndex, toIndex);
                }

                @Override
                public final int size() {
                    return values.size();
                }

                @Override
                public final boolean isEmpty() {
                    return values.isEmpty();
                }

                @Override
                public final boolean contains(final Object o) {
                    final boolean[] result = new boolean[1];
                    Parallel.For(this, new Parallel.Operation<E>() {
                        @Override
                        public void perform(E pParameter) {
                            if ((o == pParameter) || (pParameter != null && pParameter.equals(o)))
                                synchronized (result) {
                                    result[0] = true;
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
                    final PartialList partialList = this;
                    return new java.util.Iterator<E>() {

                        private transient int mIndex = -1;

                        @Override
                        public boolean hasNext() {
                            return mIndex + 1 < partialList.size();
                        }

                        @Override
                        public E next() {
                            return values.get(++mIndex);
                        }

                        @Override
                        public void remove() {
                            partialList.remove(mIndex--);
                        }
                    };
                }

                @NotNull
                @Override
                public final Object[] toArray() {
                    return values.toArray();
                }


                @NotNull
                @Override
                public final <T1> T1[] toArray(@NotNull final T1[] a) {
                    return values.toArray(a);
                }

                @Override
                public final boolean add(final E e) {
                    if (this.size() == Integer.MAX_VALUE) throw new IllegalStateException("The collection is full");
                    boolean result;
                    if (result = values.add(e)) {
                        result = LongListIterator.this.addAtEnd(e);
                        if (!result)
                            values.remove(values.size()-1);
                    }
                    return result;
                }

                @Override
                public final boolean remove(final Object o) {
                    int index = indexOf(o);
                    boolean result;
                    if (result = index != -1) {
                        final E data = values.remove(index);
                        final E data2 = LongListIterator.this.remove((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE + index);
                        if ((data != data2) && (data2 != null && !data2.equals(data))) {
                            //Rollback
                            LongListIterator.this.add((mIndex/MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE + index, data2);
                            throw new RuntimeException("The values are not the same");
                        }
                        if (index <= indexInCollectionOf(mIndex))
                            mIndex--;
                        if (mIndex / MAX_COLLECTION_SIZE != mCollectionIndex || (mIndex == NULL_INDEX && mCollectionIndex != NULL_INDEX)) {
                            mCurrentCollection = new PartialList();
                            final Runtime runtime = Runtime.getRuntime();
                            if (runtime.freeMemory() < runtime.maxMemory() * 0.3f)
                                runtime.gc();
                        }
                    }
                    return result;
                }

                @SuppressWarnings("SuspiciousMethodCalls")
                @Override
                public final boolean containsAll(@NotNull final Collection<?> c) {
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    if (result[0]) {
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
                    }
                    return result[0];
                }

                @Override
                public final boolean addAll(@NotNull final Collection<? extends E> c) {
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    if (result[0]) {
                        final List<E> list = this;
                        Parallel.For(c, new Parallel.Operation<E>() {
                            @Override
                            public void perform(E pParameter) {
                                synchronized (list) {
                                    if (!list.add(pParameter))
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
                    }
                    return result[0];
                }

                @SuppressWarnings("SuspiciousMethodCalls")
                @Override
                public final boolean removeAll(@NotNull final Collection<?> c) {
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    if (result[0]) {
                        final Collection<E> collection = this;
                        Parallel.For(c, new Parallel.Operation<Object>() {
                            @Override
                            public void perform(Object pParameter) {
                                if (!collection.remove(pParameter))
                                    synchronized (result) {
                                        result[0] = false;
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
                public final boolean retainAll(@NotNull final Collection<?> c) {
                    final boolean[] result = new boolean[1];
                    result[0] = !c.isEmpty();
                    final Collection<E> removes = new ArrayList<>(c.size());
                    if (result[0]) {
                        Parallel.For(this, new Parallel.Operation<E>() {
                            @Override
                            public void perform(E pParameter) {
                                if (!c.contains(pParameter)) {
                                    if (!removes.add(pParameter))
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
                    }
                    return result[0] && this.removeAll(removes);
                }

                @Override
                public final void clear() {
                    if (!this.isEmpty()) {
                        if (!LongListIterator.this.isEmpty()) {
                            {
                                final ParallelArrayList<E> arrayList = new ParallelArrayList<>(values);
                                values.clear();
                                final java.util.Map<Long, E> rollback = new TreeMap<>();
                                try {
                                    Parallel.For(arrayList, new Parallel.Operation<E>() {
                                        int index = 0;
                                        final long collectionIndex = mIndex / MAX_COLLECTION_SIZE;
                                        @Override
                                        public void perform(E pParameter) {
                                            final E delete;
                                            synchronized (LongListIterator.this) {
                                                delete = LongListIterator.this.remove(collectionIndex * MAX_COLLECTION_SIZE);
                                            }
                                            synchronized (rollback) {
                                                rollback.put((collectionIndex * MAX_COLLECTION_SIZE) + index, delete);
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
                            if (((mIndex / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE) < (((LongListIterator.this.size()-1) / MAX_COLLECTION_SIZE) * MAX_COLLECTION_SIZE)) {
                                values = new ParallelArrayList<>(LongListIterator.this.collectionOf(mIndex));
                                final Runtime runtime = Runtime.getRuntime();
                                if (runtime.maxMemory() - runtime.freeMemory() > runtime.maxMemory() * 0.7f)
                                    runtime.gc();
                            }
                        } else {
                            values.clear();
                        }
                    }
                }

                @Override
                public boolean isLocked() {
                    return false;
                }
            }

            private transient long mIndex = pIndex;
            private transient PartialList mCurrentCollection = new PartialList();

            private transient final SizeListener<Long> sizeListener = new SizeListener<Long>() {
                @Override
                public void change(Long pIndex, Long pDelta) {
                    if (pIndex <= mIndex) {
                        mIndex = (LongListIterator.this.size() == 0)? NULL_INDEX: mIndex + pDelta;
                    } else if (mIndex >= LongListIterator.this.size()) {
                        mIndex = (LongListIterator.this.size() == 0)? NULL_INDEX: LongListIterator.this.size() - 1;
                    }
                    if ((mIndex != NULL_INDEX && mIndex / MAX_COLLECTION_SIZE != mCurrentCollection.mCollectionIndex) || (mIndex == NULL_INDEX && mCurrentCollection.mCollectionIndex != NULL_INDEX)) {
                        mCurrentCollection = new PartialList();
                    }
                }
            };

            {
                if (!mSizeListeners.add(sizeListener)) throw new RuntimeException("SizeListener has not be added");
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                if (!mSizeListeners.remove(sizeListener)) throw new RuntimeException("SizeListener has not be removed");
            }

            @Override
            public final boolean hasNext() {
                return (mIndex == NULL_INDEX)
                        ?size() > 0
                        :mIndex + (MAX_COLLECTION_SIZE - indexInCollectionOf(mIndex)) < size() && mIndex + (MAX_COLLECTION_SIZE - indexInCollectionOf(mIndex)) >= MIN_INDEX;
            }

            @Override
            public final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> next() throws NoSuchElementException {
                if (mIndex == NULL_INDEX)
                    mIndex = MIN_INDEX;
                else if (mIndex + (MAX_COLLECTION_SIZE - indexInCollectionOf(mIndex)) < size() && mIndex + (MAX_COLLECTION_SIZE - indexInCollectionOf(mIndex)) >= MIN_INDEX)
                    mIndex += MAX_COLLECTION_SIZE - indexInCollectionOf(mIndex);
                else
                    throw new NoSuchElementException();
                return get();
            }

            @Nullable
            @Override
            public final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> get() throws IndexOutOfBoundsException {
                if (mIndex / MAX_COLLECTION_SIZE != mCurrentCollection.mCollectionIndex) {
                    mCurrentCollection = new PartialList();
                    final Runtime runtime = Runtime.getRuntime();
                    if (runtime.freeMemory() < runtime.maxMemory() * 0.3f)
                        runtime.gc();
                }
                return mCurrentCollection;
            }

            @Override
            public final boolean hasPrevious() {
                return MIN_INDEX <= mIndex - indexInCollectionOf(mIndex) - MAX_COLLECTION_SIZE;
            }

            @Nullable
            @Override
            public final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> previous() throws NoSuchElementException {
                if (mIndex - indexInCollectionOf(mIndex) - MAX_COLLECTION_SIZE >= MIN_INDEX)
                    mIndex = mIndex - indexInCollectionOf(mIndex) - MAX_COLLECTION_SIZE;
                else
                    throw new NoSuchElementException();
                return get();
            }

            @Override
            public final void add(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> pEntity) {
                if (mCurrentCollection.size() + pEntity.size() < 0) throw new IllegalStateException("The list have too much elements");
                mCurrentCollection.addAll(indexInCollectionOf(mIndex), pEntity);
            }

            @Override
            public final void set(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.ListIterator.PartialList<E> pEntity) {
                if (pEntity.size() > mCurrentCollection.size()) throw new IllegalStateException("The list have too much elements");
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
            public final void remove() {
                mCurrentCollection.clear();
            }

        };
    }
}
