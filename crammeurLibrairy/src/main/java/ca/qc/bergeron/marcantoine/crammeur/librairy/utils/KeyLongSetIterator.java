package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.ContainsException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

public final class KeyLongSetIterator extends KeySetIterator<Long> {

    protected final LinkedList<TreeSet<Long>>[] values = new LinkedList[2];
    protected transient volatile long mIndex = NULL_INDEX;
    protected transient volatile Long mHashCode = null;
    protected transient volatile boolean mNullKey = false;
    protected transient volatile long mSize = 0;

    private KeyLongSetIterator(LinkedList<TreeSet<Long>> pHashSetOne, LinkedList<TreeSet<Long>> pHashSetTwo, long pSize) {
        values[0] = pHashSetOne;
        values[1] = pHashSetTwo;
        mSize = pSize;
    }

    public KeyLongSetIterator() {
        values[0] = new LinkedList<TreeSet<Long>>() {

            @Override
            public boolean contains(@NotNull final Object o) {
                final boolean[] result = new boolean[1];
                Parallel.For(this, new Parallel.Operation<TreeSet<Long>>() {
                    @Override
                    public void perform(TreeSet<Long> pParameter) {
                        if (pParameter.equals(o)) {
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

            @Override
            public boolean remove(@NotNull final Object o) {
                final boolean[] result = new boolean[1];
                final Iterator<TreeSet<Long>> iterator = this.iterator();
                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        while (iterator.hasNext() && !result[0]) {
                            synchronized (iterator) {
                                if (iterator.next().equals(o)) {
                                    iterator.remove();
                                    synchronized (result) {
                                        result[0] = true;
                                    }
                                }
                            }
                        }
                    }
                };
                for (int thread=0; thread<Runtime.getRuntime().availableProcessors()*2; thread++) {
                    executorService.execute(runnable);
                }
                executorService.shutdown();
                while (!executorService.isTerminated()) {
                    try {
                        Thread.sleep(0,1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                return result[0];
            }
        };
        values[1] = new LinkedList<TreeSet<Long>>() {
            @Override
            public boolean contains(@NotNull final Object o) {
                final boolean[] result = new boolean[1];
                Parallel.For(this, new Parallel.Operation<TreeSet<Long>>() {
                    @Override
                    public void perform(TreeSet<Long> pParameter) {
                        if (pParameter.equals(o)) {
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

            @Override
            public boolean remove(@NotNull final Object o) {
                final boolean[] result = new boolean[1];
                final Iterator<TreeSet<Long>> iterator = this.iterator();
                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        while (iterator.hasNext() && !result[0]) {
                            synchronized (iterator) {
                                if (iterator.next().equals(o)) {
                                    synchronized (iterator) {
                                        iterator.remove();
                                    }
                                    synchronized (result) {
                                        result[0] = true;
                                    }
                                }
                            }
                        }
                    }
                };
                for (int thread=0; thread<Runtime.getRuntime().availableProcessors()*2; thread++) {
                    executorService.execute(runnable);
                }
                executorService.shutdown();
                while (!executorService.isTerminated()) {
                    try {
                        Thread.sleep(0,1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                return result[0];
            }
        };
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        values[0] = null;
        values[1] = null;
        mIndex = NULL_INDEX;
        mHashCode = null;
        mNullKey = false;
        mSize = 0;
    }

    @Override
    public final boolean remove(@Nullable final Long pKey) {
        final boolean[] result = new boolean[1];
        if (pKey == null && mNullKey) {
            mNullKey = false;
            result[0] = true;
        } else {
            final TreeSet<Long>[] last = new TreeSet[1];
            Parallel.Operation<TreeSet<Long>> operation = new Parallel.Operation<TreeSet<Long>>() {
                TreeSet<Long> previous = null;
                @Override
                public void perform(TreeSet<Long> pParameter) {
                    if (pParameter.contains(pKey)) {
                        synchronized (result) {
                            result[0] = pParameter.remove(pKey);
                        }
                        if (result[0]) {
                            synchronized (KeyLongSetIterator.this) {
                                mSize--;
                            }
                        }
                        synchronized (this) {
                            previous = pParameter;
                        }
                    } else if (result[0] && previous != null) {
                        Set<Long> remove = new HashSet<>();
                        for (Long l : pParameter) {
                            if (previous.size() != Integer.MAX_VALUE) {
                                if (!previous.add(l)) throw new RuntimeException("The value has not been added");
                                if (!remove.add(l)) throw new RuntimeException("The value has not been added");
                            } else {
                                break;
                            }
                        }
                        for (Long l : remove) {
                            if (!pParameter.remove(l)) throw new RuntimeException("The value has not been removed");
                        }
                    }
                    synchronized (last) {
                        last[0] = pParameter;
                    }
                }

                @Override
                public boolean follow() {
                    return !result[0];
                }
            };
            for (int arrayIndex=0;arrayIndex<values.length;arrayIndex++) {
                if (!values[arrayIndex].isEmpty()) {
                    Parallel.For(values[arrayIndex], operation);
                }
            }
            if (last[0] != null && last[0].isEmpty()) {
                if (values[0].contains(last[0])) {
                    if (!values[0].remove(last[0])) throw new RuntimeException("The last empty HashSet has not been removed");
                } else {
                    if (!values[1].remove(last[0])) throw new RuntimeException("The last empty HashSet has not been removed");
                }
            }
        }

        return result[0];
    }

    @Override
    public final <E extends Long> boolean retainAll(@NotNull CollectionIterator<E, Long> pKeyCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = true;
        final KeyLongSetIterator retain = new KeyLongSetIterator();
        for (Collection<E> collection : pKeyCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<E>() {

                @Override
                public void perform(E pParameter) {
                    retain.add(pParameter);
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }

        final KeyLongSetIterator delete = new KeyLongSetIterator();
        for (Collection<Long> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<Long>() {
                @Override
                public void perform(Long pParameter) {
                    if (!retain.contains(pParameter)) {
                        delete.add(pParameter);
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });
        }

        for (Collection<Long> collection : delete.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<Long>() {
                @Override
                public void perform(Long pParameter) {
                    synchronized (result) {
                        result[0] = KeyLongSetIterator.this.remove(pParameter);
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
        mHashCode = null;
        mNullKey = false;
        mSize = 0;
    }

    @NotNull
    @Override
    public final Long size() {
        return mSize;
    }

    @Override
    public final boolean isEmpty() {
        return mSize == 0;
    }

    @Override
    public final int currentCollectionIndex() {
        return collectionIndexOf(mIndex);
    }

    @Override
    public final int collectionIndexOf(@NotNull Long pIndex) {
        return (int) (pIndex % MAX_COLLECTION_SIZE);
    }

    @NotNull
    @Override
    public final Set<Long> currentCollection() {
        return collectionOf(mIndex);
    }

    @NotNull
    @Override
    public final Iterable<Collection<Long>> allCollections() {
        return new Iterable<Collection<Long>>() {
            @NotNull
            @Override
            public Iterator<Collection<Long>> iterator() {
                return new Iterator<Collection<Long>>() {

                    private LinkedList<TreeSet<Long>>[] values = KeyLongSetIterator.this.values;
                    private transient volatile long mIndex = NULL_INDEX;
                    private transient volatile long mSize = (long) values[0].size() + values[1].size() + ((mNullKey)?1:0);

                    @Override
                    public boolean hasNext() {
                        return mIndex + 1 < mSize;
                    }

                    private final Long[] keyMoved = new Long[1];
                    @Override
                    public Collection<Long> next() {
                        final Collection<Long>[] result = new Collection[1];
                        final int arrayIndex = (int) (++mIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
                        final long[] traveled = new long[1];
                        traveled[0] = (arrayIndex == 1)? MAX_COLLECTION_SIZE :0;
                        if (mNullKey) {
                            Parallel.For(values[arrayIndex], new Parallel.Operation<TreeSet<Long>>() {
                                @Override
                                public void perform(TreeSet<Long> pParameter) {
                                    if (pParameter.first().compareTo(null) > 0 && pParameter.last().compareTo(null) > 0) {
                                        synchronized (keyMoved) {
                                            keyMoved[0] = pParameter.last();
                                        }
                                        if (!pParameter.remove(pParameter.last()))
                                            throw new RuntimeException("The value has not been removed");
                                        if (!pParameter.add(null))
                                            throw new RuntimeException("The value has not been added");
                                    } else if (keyMoved[0] != null) {
                                        long key = keyMoved[0];
                                        synchronized (keyMoved) {
                                            keyMoved[0] = pParameter.last();
                                        }
                                        if (!pParameter.remove(pParameter.last()))
                                            throw new RuntimeException("The value has not been removed");
                                        if (!pParameter.add(key))
                                            throw new RuntimeException("The value has not been added");
                                    } else {

                                    }
                                    synchronized (traveled) {
                                        traveled[0]++;
                                    }
                                }

                                @Override
                                public boolean follow() {
                                    return mIndex >= traveled[0];
                                }
                            });
                        } else {
                            Parallel.For(values[arrayIndex], new Parallel.Operation<TreeSet<Long>>() {
                                @Override
                                public void perform(TreeSet<Long> pParameter) {
                                    if (mIndex == traveled[0]) {
                                        synchronized (result) {
                                            result[0] = pParameter;
                                        }
                                    }
                                    synchronized (traveled) {
                                        traveled[0]++;
                                    }
                                }

                                @Override
                                public boolean follow() {
                                    return mIndex >= traveled[0];
                                }
                            });
                        }

                        return result[0];
                    }

                    @Override
                    public void remove() {
                        if (mIndex == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
                        final int arrayIndex = (int) (mIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
                        final long[] traveled = new long[1];
                        traveled[0] = (arrayIndex == 1)? MAX_COLLECTION_SIZE :0;
                        final TreeSet<Long>[] remove = new TreeSet[1];
                        Parallel.For(values[arrayIndex], new Parallel.Operation<TreeSet<Long>>() {
                            @Override
                            public void perform(TreeSet<Long> pParameter) {
                                if (mIndex == traveled[0]) {
                                    synchronized (remove) {
                                        remove[0] = pParameter;
                                    }
                                }
                                synchronized (traveled) {
                                    traveled[0]++;
                                }
                            }

                            @Override
                            public boolean follow() {
                                return mIndex >= traveled[0];
                            }
                        });
                        if (remove[0] != null) {
                            if (!values[arrayIndex].remove(remove[0])) throw new RuntimeException("The set has not been removed");
                            while (values[0].size() < Integer.MAX_VALUE && !values[1].isEmpty()) {
                                TreeSet<Long> set = values[1].iterator().next();
                                values[0].add(set);
                                values[1].remove(set);
                            }
                            mIndex--;
                        }
                    }
                };
            }
        };
    }

    @NotNull
    @Override
    public final Set<Long> collectionOf(@NotNull Long pIndex) {
        if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
        final Set<Long> result = new LinkedHashSet<>();
        final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
        long size = (arrayIndex == 1)?(long) MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE :0;
        for (Set<Long> set : values[arrayIndex]) {
            size+=set.size();
            if (pIndex < size) {
                Parallel.For(set, new Parallel.Operation<Long>() {
                    @Override
                    public void perform(Long pParameter) {
                        synchronized (result) {
                            if (!result.add(pParameter)) throw new RuntimeException("The result has not been added");
                        }
                    }

                    @Override
                    public boolean follow() {
                        return true;
                    }
                });
            }
            if (result.size() == Integer.MAX_VALUE) break;
        }
        return result;
    }

    @NotNull
    @Override
    public final Long count(@Nullable final Long pEntity) {
        final long[] result = new long[1];
        for (Collection<Long> collection : this.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<Long>() {
                @Override
                public void perform(Long pParameter) {
                    if ((pEntity == null && pParameter == null) || (pEntity != null && pEntity.equals(pParameter))) {
                        synchronized (result) {
                            result[0]++;
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return result[0] == 0;
                }
            });
        }
        return result[0];
    }

    @Override
    public final void add(@Nullable final Long pEntity) {
        final boolean[] contain = new boolean[1];
        if (pEntity == null) {
            if (mNullKey) contain[0] = true;
            else mNullKey = true;
        } else {
            TreeSet<Long> myTreeSet = new TreeSet<Long>() {

                @Override
                public boolean add(@NotNull Long aLong) {
                    boolean result = super.add(aLong);
                    if (result) {
                        if (mHashCode == null) {
                            mHashCode = aLong;
                        } else {
                            mHashCode |= aLong;
                        }
                    }
                    return result;
                }

                @Override
                public boolean remove(@NotNull final Object o) {
                    boolean result = super.remove(o);
                    if (result) {
                        mHashCode = null;
                        Parallel.For(this, new Parallel.Operation<Long>() {
                            @Override
                            public void perform(Long pParameter) {
                                if (mHashCode == null) {
                                    synchronized (KeyLongSetIterator.this) {
                                        mHashCode = (Long)o;
                                    }
                                } else {
                                    synchronized (KeyLongSetIterator.this) {
                                        mHashCode |= (Long)o;
                                    }
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

                @Override
                public boolean contains(@NotNull final Object o) {
                    final boolean[] result = new boolean[1];
                    if (mHashCode != null && (mHashCode & (Long)o) == (Long)o) {
                        final TreeSet<Long> treeSet = this;
                        Parallel.For(treeSet, new Parallel.Operation<Long>() {
                            Long previous = null;
                            @Override
                            public void perform(Long pParameter) {
                                if (o.equals(pParameter)) {
                                    synchronized (result) {
                                        result[0] = true;
                                    }
                                }
                            }

                            @Override
                            public boolean follow() {
                                return ((previous == null) || (Long)o >= previous) && (((Long)o <= treeSet.first()) && (treeSet.first().equals(treeSet.last()) || ((Long)o <= treeSet.last()))) && !result[0];
                            }
                        });
                    }
                    return  result[0];
                }
            };

            if (this.isEmpty()) {
                if (!myTreeSet.add(pEntity)) throw new RuntimeException("The value has not been added");
                values[0].add(myTreeSet);
            } else {
                final TreeSet<Long>[] target = new TreeSet[1];
                final LinkedList<TreeSet<Long>>[] followTarget = new LinkedList[2];
                followTarget[0] = new LinkedList<>();
                followTarget[1] = new LinkedList<>();
                for (LinkedList<TreeSet<Long>> linkedList : values) {
                    Parallel.For(linkedList, new Parallel.Operation<TreeSet<Long>>() {
                        @Override
                        public void perform(TreeSet<Long> pParameter) {
                            if (pParameter.contains(pEntity)) {
                                synchronized (contain) {
                                    contain[0] = true;
                                }
                            } else if (pParameter.first() == null || (pParameter.first() != null && pParameter.first().compareTo(pEntity) < 0)) {
                                synchronized (target) {
                                    target[0] = pParameter;
                                }
                            } else if (target[0] != null) {
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
                        }

                        @Override
                        public boolean follow() {
                            return (target[0] == null || target[0].size() == Integer.MAX_VALUE) && !contain[0];
                        }
                    });
                }
                if (!contain[0]) {
                    if (target[0].size() == Integer.MAX_VALUE) {
                        if (followTarget[0].size() == 0) {
                            final int arrayIndex = (int) ((mSize-1) / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
                            if (!myTreeSet.add(pEntity))
                                throw new RuntimeException("The value has not been added");
                            if (!values[arrayIndex].add(myTreeSet))
                                throw new RuntimeException("The value has not been added");
                        } else {
                            final Long[] add = new Long[1];
                            final TreeSet<Long>[] lastTreeSet = new TreeSet[1];
                            for (LinkedList<TreeSet<Long>> linkedList : followTarget) {
                                Parallel.For(linkedList, new Parallel.Operation<TreeSet<Long>>() {
                                    @Override
                                    public void perform(TreeSet<Long> pParameter) {
                                        Long last = pParameter.last();
                                        if (!pParameter.remove(last))
                                            throw new RuntimeException("The value has not been removed");
                                        pParameter.add(add[0]);
                                        synchronized (add) {
                                            add[0] = last;
                                        }
                                        synchronized (lastTreeSet) {
                                            lastTreeSet[0] = pParameter;
                                        }
                                    }

                                    @Override
                                    public boolean follow() {
                                        return true;
                                    }
                                });
                            }
                            if (lastTreeSet[0] != null) {
                                if (lastTreeSet[0].size() == Integer.MAX_VALUE) {
                                    final int arrayIndex = (int) ((mSize-1) / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
                                    if (!myTreeSet.add(add[0]))
                                        throw new RuntimeException("The value has not been added");
                                    if (!values[arrayIndex].add(myTreeSet))
                                        throw new RuntimeException("The value has not been added");
                                } else {
                                    if (!lastTreeSet[0].add(add[0]))
                                        throw new RuntimeException("The value has not been added");
                                }
                            } else {
                                throw new RuntimeException();
                            }

                        }
                    } else {
                        if (!target[0].add(pEntity))
                            throw new RuntimeException("The value has not been added");
                    }
                }
            }
        }

        if (contain[0]) throw new ContainsException("The value is already present");
        mSize++;
    }

    protected final Long actual() {
        if (mIndex < MIN_INDEX && mIndex < mSize) {
            final int arrayIndex = (int) (mIndex / ((long) MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
            final Long[] result = new Long[1];
            final long[] index = new long[1];
            index[0] = NULL_INDEX;
            for (Collection<Long> collection : values[arrayIndex]) {
                Parallel.For(collection, new Parallel.Operation<Long>() {
                    @Override
                    public void perform(Long pParameter) {
                        synchronized (index) {
                            index[0]++;
                        }
                        if (index[0] == mIndex) {
                            synchronized (result) {
                                result[0] = pParameter;
                            }
                        }
                    }

                    @Override
                    public boolean follow() {
                        return index[0] < mIndex;
                    }
                });
            }
            return result[0];
        } else
            throw new IndexOutOfBoundsException(String.valueOf(mIndex));

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
    public final boolean hasNext() {
        return mIndex + 1 < mSize;
    }

    @Nullable
    @Override
    public final Long next() {
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

    @Override
    public final int previousIndex() {
        if (mIndex != NULL_INDEX && mIndex - 1 >= MIN_INDEX) {
            return collectionIndexOf(mIndex - 1);
        } else {
            return NULL_INDEX;
        }
    }

    @Nullable
    @Override
    public final Long previous() {
        if (hasPrevious()) {
            mIndex--;
            return actual();
        } else
            throw new NoSuchElementException();
    }

    @Override
    public final void set(@Nullable Long pEntity) {
        if (!this.contains(pEntity)) {
            this.add(pEntity);
        }
    }

    @NotNull
    @Override
    public final Iterator<Long> iterator() {
        return new KeyLongSetIterator(values[0],values[1],mSize);
    }
}
