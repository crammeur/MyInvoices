package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.ContainsException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.KeySetIterator;

/**
 * Created by Marc-Antoine on 2017-11-26.
 */

public final class DataLongMap<T extends Data<Long>> extends DataMap<Long,T> {

    public class EntryLongSetIterator extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.EntrySetIterator<T,Long> {

        protected final HashSet<HashSet<Entry<Long, T>>>[] values = new HashSet[2];
        protected transient volatile long mIndex = NULL_INDEX;
        protected transient volatile long mSize = 0;

        private EntryLongSetIterator(HashSet<HashSet<Entry<Long, T>>> pHashSetOne, HashSet<HashSet<Entry<Long, T>>> pHashSetTwo, long pSize) {
            values[0] = pHashSetOne;
            values[1] = pHashSetTwo;
            mSize = pSize;
        }

        public EntryLongSetIterator() {
            values[0] = new HashSet<>();
            values[1] = new HashSet<>();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            values[0] = null;
            values[1] = null;
            mIndex = NULL_INDEX;
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
            return this.collectionIndexOf(mIndex);
        }

        @Override
        public final int collectionIndexOf(@NotNull Long pIndex) {
            return (int) (pIndex % MAX_COLLECTION_SIZE);
        }

        @NotNull
        @Override
        public final Set<Entry<Long, T>> currentCollection() {
            return collectionOf(mIndex);
        }

        @NotNull
        @Override
        public final Iterable<Collection<Entry<Long, T>>> allCollections() {
            return new Iterable<Collection<Entry<Long,T>>>() {
                @NotNull
                @Override
                public Iterator<Collection<Entry<Long,T>>> iterator() {
                    return new Iterator<Collection<Entry<Long,T>>>() {

                        private HashSet<java.util.Map<Long,T>>[] values = DataLongMap.this.values;
                        private transient volatile long mIndex = NULL_INDEX;
                        private transient volatile long mSize = (long) values[0].size() + values[1].size();

                        @Override
                        public boolean hasNext() {
                            return mIndex + 1 < mSize;
                        }

                        @Override
                        public Collection<Entry<Long,T>> next() {
                            final Collection<Entry<Long,T>>[] result = new Collection[1];
                            final int arrayIndex = (int) (++mIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
                            final long[] traveled = new long[1];
                            traveled[0] = (arrayIndex == 1)? MAX_COLLECTION_SIZE :0;
                            Parallel.For(values[arrayIndex], new Parallel.Operation<Map<Long,T>>() {
                                @Override
                                public void perform(final Map<Long,T> pParameter) {
                                    if (mIndex == traveled[0]) {
                                        final Collection<Entry<Long,T>> collection = new LinkedHashSet<>(pParameter.size());
                                        Parallel.For(pParameter.keySet(), new Parallel.Operation<Long>() {
                                            @Override
                                            public void perform(final Long pParameter2) {
                                                synchronized (collection) {
                                                    collection.add(new Entry<Long, T>() {

                                                        final Long key = pParameter2;

                                                        @Override
                                                        public Long getKey() {
                                                            return key;
                                                        }

                                                        @Override
                                                        public T getValue() {
                                                            return pParameter.get(key);
                                                        }

                                                        @Override
                                                        public T setValue(T pValue) {
                                                            return pParameter.put(key,pValue);
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public boolean follow() {
                                                return true;
                                            }
                                        });
                                        synchronized (result) {
                                            result[0] = collection;
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
                            return result[0];
                        }

                        @Override
                        public void remove() {
                            if (mIndex == NULL_INDEX) throw new IllegalStateException(String.valueOf(NULL_INDEX));
                            final int arrayIndex = (int) (++mIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
                            final long[] traveled = new long[1];
                            traveled[0] = (arrayIndex == 1)? MAX_COLLECTION_SIZE :0;
                            final Map<Long,T>[] remove = new Map[1];
                            Parallel.For(values[arrayIndex], new Parallel.Operation<Map<Long,T>>() {
                                @Override
                                public void perform(Map<Long,T> pParameter) {
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
                                    Map<Long,T> map = values[1].iterator().next();
                                    values[0].add(map);
                                    values[1].remove(map);
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
        public final Set<Entry<Long, T>> collectionOf(@NotNull Long pIndex) {
            if (pIndex < MIN_INDEX || pIndex >= mSize) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
            final Set<Entry<Long,T>> result = new LinkedHashSet<>();
            final int arrayIndex = (int) (pIndex / (((long) MAX_COLLECTION_SIZE + 1) * ((long) MAX_COLLECTION_SIZE + 1)));
            long size = (arrayIndex == 1)?(long) MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE :0;
            for (Set<Entry<Long,T>> set : values[arrayIndex]) {
                size+=set.size();
                if (pIndex < size) {
                    Parallel.For(set, new Parallel.Operation<Entry<Long,T>>() {
                        @Override
                        public void perform(Entry<Long,T> pParameter) {
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
        public final Long count(@Nullable final Entry<Long, T> pEntity) {
            final long[] result = new long[1];
            for (Collection<Entry<Long, T>> collection : this.allCollections()) {
                Parallel.For(collection, new Parallel.Operation<Entry<Long, T>>() {
                    @Override
                    public void perform(Entry<Long, T> pParameter) {
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
        public final void add(@Nullable final Entry<Long, T> pEntity) {
            if (this.isEmpty()) {
                values[0].add(new HashSet<Entry<Long,T>>(){{if (!add(pEntity)) throw new RuntimeException("The value has not been added");}});
                mSize++;
            } else {
                final boolean[] contain = new boolean[1];
                final long[] traveled = new long[1];
                for (int arrayIndex=0; arrayIndex<values.length; arrayIndex++) {
                    final int finalArrayIndex = arrayIndex;
                    Parallel.For(values[arrayIndex], new Parallel.Operation<HashSet<Entry<Long,T>>>() {
                        @Override
                        public void perform(HashSet<Entry<Long,T>> pParameter) {
                            if (pParameter.contains(pEntity)) {
                                synchronized (contain) {
                                    contain[0] = true;
                                }
                            }
                            synchronized (traveled) {
                                traveled[0]+=pParameter.size();
                            }
                            if (!contain[0] && traveled[0] == mSize) {
                                if (pParameter.size() == Integer.MAX_VALUE) {
                                    HashSet<Entry<Long,T>> set = new HashSet<>();
                                    if (set.add(pEntity)) {
                                        synchronized (values) {
                                            if (values[finalArrayIndex].add(set)) {
                                                synchronized (EntryLongSetIterator.this) {
                                                    EntryLongSetIterator.this.mSize++;
                                                }
                                            } else {
                                                throw new RuntimeException("The value has not been added");
                                            }
                                        }
                                    } else {
                                        throw new RuntimeException("The value has not been added");
                                    }
                                } else {
                                    if (pParameter.add(pEntity)) {
                                        synchronized (EntryLongSetIterator.this) {
                                            EntryLongSetIterator.this.mSize++;
                                        }
                                    } else {
                                        throw new RuntimeException("The value has not been added");
                                    }
                                }
                            }
                        }

                        @Override
                        public boolean follow() {
                            return !contain[0];
                        }
                    });
                    if (contain[0]) break;
                }
                if (contain[0]) throw new ContainsException("The value is already present");
            }
        }

        protected final Entry<Long, T> actual() {
            if (mIndex < MIN_INDEX && mIndex < mSize) {
                final int arrayIndex = (int) (mIndex / ((long) MAX_COLLECTION_SIZE * MAX_COLLECTION_SIZE));
                final Entry<Long,T>[] result = new Entry[1];
                final long[] index = new long[1];
                index[0] = NULL_INDEX;
                for (HashSet<Entry<Long,T>> hashSet : values[arrayIndex]) {
                    Parallel.For(hashSet, new Parallel.Operation<Entry<Long,T>>() {
                        @Override
                        public void perform(Entry<Long,T> pParameter) {
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
        public final Entry<Long, T> next() {
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
        public final Entry<Long, T> previous() {
            if (hasPrevious()) {
                mIndex--;
                return actual();
            } else
                throw new NoSuchElementException();
        }

        @Override
        public void set(@Nullable Entry<Long, T> pEntity) {
            if (!this.contains(pEntity)) {
                this.add(pEntity);
            }
        }

        @Override
        public final boolean remove(@Nullable final Entry<Long, T> pEntry) {
            final boolean[] result = new boolean[1];
            final HashSet<Entry<Long,T>>[] last = new HashSet[1];
            Parallel.Operation<HashSet<Entry<Long,T>>> operation = new Parallel.Operation<HashSet<Entry<Long,T>>>() {
                HashSet<Entry<Long,T>> previous = null;
                @Override
                public void perform(HashSet<Entry<Long,T>> pParameter) {
                    if (pParameter.contains(pEntry)) {
                        synchronized (result) {
                            result[0] = pParameter.remove(pEntry);
                        }
                        if (result[0]) {
                            synchronized (EntryLongSetIterator.this) {
                                mSize--;
                            }
                        }
                        synchronized (this) {
                            previous = pParameter;
                        }
                    } else if (result[0] && previous != null) {
                        Set<Entry<Long,T>> remove = new HashSet<>();
                        for (Entry<Long,T> l : pParameter) {
                            if (previous.size() != Integer.MAX_VALUE) {
                                if (!previous.add(l)) throw new RuntimeException("The value has not been added");
                                if (!remove.add(l)) throw new RuntimeException("The value has not been added");
                            } else {
                                break;
                            }
                        }
                        for (Entry<Long,T> l : remove) {
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
                Parallel.For(values[arrayIndex], operation);
            }
            if (last[0] != null && last[0].isEmpty()) {
                if (values[0].contains(last[0])) {
                    if (!values[0].remove(last[0])) throw new RuntimeException("The last empty HashSet has not been removed");
                } else {
                    if (!values[1].remove(last[0])) throw new RuntimeException("The last empty HashSet has not been removed");
                }
            }

            return result[0];
        }

        @Override
        public final <E2 extends T> boolean retainAll(@NotNull final EntryCollectionIterator<E2, Long> pEntryCollectionIterator) {
            final boolean[] result = new boolean[1];
            result[0] = true;
            final EntrySetIterator<T, Long> retain = new EntryLongSetIterator();
            for (Collection<Entry<Long,E2>> collection : pEntryCollectionIterator.allCollections()) {
                Parallel.For(collection, new Parallel.Operation<Entry<Long, E2>>() {

                    @Override
                    public void perform(final Entry<Long,E2> pParameter) {
                        retain.add(new Entry<Long, T>() {

                            final Long key = pParameter.getKey();

                            @Override
                            public Long getKey() {
                                return key;
                            }

                            @Override
                            public T getValue() {
                                return pParameter.getValue();
                            }

                            @Override
                            public T setValue(T pValue) {
                                Map<Field, java.lang.Object> map = new HashMap<>();
                                for (Field field : Object.getAllFields(pValue.getClass())) {
                                    boolean b = field.isAccessible();
                                    field.setAccessible(true);
                                    try {
                                        map.put(field,field.get(pValue));
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    } finally {
                                        field.setAccessible(b);
                                    }
                                }
                                try {
                                    E2 value = Object.changeObject(pParameter.getValue(),map);
                                    return pParameter.setValue(value);
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    }

                    @Override
                    public boolean follow() {
                        return true;
                    }
                });
            }

            final EntrySetIterator<T, Long> delete = new EntryLongSetIterator();
            for (Collection<Entry<Long,T>> collection : this.allCollections()) {
                Parallel.For(collection, new Parallel.Operation<Entry<Long,T>>() {
                    @Override
                    public void perform(Entry<Long,T> pParameter) {
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

            for (Collection<Entry<Long,T>> collection : delete.allCollections()) {
                Parallel.For(collection, new Parallel.Operation<Entry<Long,T>>() {
                    @Override
                    public void perform(Entry<Long,T> pParameter) {
                        synchronized (result) {
                            result[0] = EntryLongSetIterator.this.remove(pParameter);
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
            mSize = 0;
        }

        @NotNull
        @Override
        public Iterator<Entry<Long, T>> iterator() {
            return new EntryLongSetIterator(values[0], values[1], mSize);
        }
    }

    protected final HashSet<java.util.Map<Long,T>>[] values = new HashSet[2];
    private transient volatile long mSize = 0;

    public DataLongMap() {
        values[0] = new HashSet<>();
        values[1] = new HashSet<>();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        values[0] = null;
        values[1] = null;
        mSize = 0;
    }

    @Override
    public final Long size() {
        return mSize;
    }

    @Override
    public final boolean isEmpty() {
        return mSize == 0;
    }

    @Override
    public final boolean containsKey(final Long pKey) {
        final boolean[] result = new boolean[1];
        for (HashSet<java.util.Map<Long, T>> hashSet : values) {
            Parallel.For(hashSet, new Parallel.Operation<java.util.Map<Long, T>>() {
                @Override
                public void perform(java.util.Map<Long, T> pParameter) {
                    if (pParameter.keySet().contains(pKey)) {
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
            if (result[0]) break;
        }
        return result[0];
    }

    @Override
    public final boolean containsValue(final T pValue) {
        final boolean[] result = new boolean[1];
        for (HashSet<java.util.Map<Long, T>> hashSet : values) {
            Parallel.For(hashSet, new Parallel.Operation<java.util.Map<Long, T>>() {
                @Override
                public void perform(java.util.Map<Long, T> pParameter) {
                    if (pParameter.values().contains(pValue)) {
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
            if (result[0]) break;
        }
        return result[0];
    }

    @Override
    public T get(final Long pKey) {
        final T[] result = (T[]) new Data[1];
        final boolean[] follow = new boolean[1];
        follow[0] = true;
        for (HashSet<java.util.Map<Long, T>> hashSet : values) {
            Parallel.For(hashSet, new Parallel.Operation<java.util.Map<Long, T>>() {
                @Override
                public void perform(java.util.Map<Long, T> pParameter) {
                    if (pParameter.keySet().contains(pKey)) {
                        synchronized (result) {
                            result[0] = pParameter.get(pKey);
                        }
                        synchronized (follow) {
                            follow[0] = false;
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return follow[0];
                }
            });
            if (!follow[0]) break;
        }
        return result[0];
    }

    @Override
    public final T put(final Long pKey, final T pValue) {
        final T[] result = (T[]) new Data[1];
        final boolean[] added = new boolean[1];
        for (HashSet<java.util.Map<Long, T>> hashSet : values) {
            Parallel.For(hashSet, new Parallel.Operation<java.util.Map<Long, T>>() {
                @Override
                public void perform(java.util.Map<Long, T> pParameter) {
                    if (pParameter.containsKey(pKey)) {
                        synchronized (result) {
                            result[0] = pParameter.put(pKey,pValue);
                        }
                        synchronized (added) {
                            added[0] = true;
                        }
                    } else if (pParameter.size() < Integer.MAX_VALUE) {
                        synchronized (result) {
                            result[0] = pParameter.put(pKey,pValue);
                        }
                        synchronized (added) {
                            added[0] = true;
                        }
                    }
                }

                @Override
                public boolean follow() {
                    return !added[0];
                }
            });
            if (added[0]) break;
        }
        if (!added[0]) {
            java.util.Map<Long, T> map = new HashMap<>();
            result[0] = map.put(pKey,pValue);
            if (values[0].size() != Integer.MAX_VALUE) {
                 if (!values[0].add(map)) throw new RuntimeException("The value has not been added");
            } else {
                 if (!values[1].add(map)) throw new RuntimeException("The value has not been added");
            }
        }
        return result[0];
    }

    @Override
    public final T remove(final Long pKey) {
        final T[] result = (T[]) new Data[1];
        final long[] traveled = new long[1];
        Parallel.Operation<java.util.Map<Long, T>> operation = new Parallel.Operation<java.util.Map<Long, T>>() {
            java.util.Map<Long, T> previous = null;
            @Override
            public void perform(java.util.Map<Long, T> pParameter) {
                if (pParameter.containsKey(pKey)) {
                    synchronized (result) {
                        result[0] = pParameter.remove(pKey);
                    }
                    synchronized (DataLongMap.this) {
                        mSize--;
                    }
                    previous = pParameter;
                } else if (previous != null && previous.size() != Integer.MAX_VALUE) {
                    Long key = pParameter.keySet().iterator().next();
                    previous.put(key, pParameter.get(key));
                    pParameter.remove(key);
                    synchronized (traveled) {
                        traveled[0]++;
                    }
                }
                synchronized (traveled) {
                    traveled[0]+= pParameter.size();
                }
            }

            @Override
            public boolean follow() {
                return traveled[0] < mSize;
            }
        };
        for (HashSet<java.util.Map<Long, T>> hashSet : values) {
            Parallel.For(hashSet, operation);
        }
        return result[0];
    }

    @Override
    public final void clear() {
        values[0].clear();
        values[1].clear();
        mSize = 0;
    }

    @Override
    public final KeySetIterator<Long> keySet() {
        final KeySetIterator<Long> result = new KeyLongSetIterator();
        for (HashSet<java.util.Map<Long,T>> hashSet : values) {
            for (java.util.Map<Long,T> map : hashSet) {
                Parallel.For(map.keySet(), new Parallel.Operation<Long>() {
                    @Override
                    public void perform(Long pParameter) {
                        synchronized (result) {
                            result.add(pParameter);
                        }
                    }

                    @Override
                    public boolean follow() {
                        return true;
                    }
                });
            }
        }
        return result;
    }

    @NotNull
    @Override
    public final CollectionIterator<T, Long> values() {
        final CollectionIterator<T,Long> result = new DataLongListIterator<>();
        for (HashSet<java.util.Map<Long,T>> hashSet : values) {
            for (java.util.Map<Long, T> map : hashSet) {
                Parallel.For(map.values(), new Parallel.Operation<T>() {
                    @Override
                    public void perform(T pParameter) {
                        synchronized (result) {
                            result.add(pParameter);
                        }
                    }

                    @Override
                    public boolean follow() {
                        return true;
                    }
                });
            }
        }
        return result;
    }

    @Override
    public final EntrySetIterator<T, Long> entrySet() {
        final ca.qc.bergeron.marcantoine.crammeur.librairy.utils.EntrySetIterator<T,Long> result = new EntryLongSetIterator();
        for (HashSet<java.util.Map<Long,T>> hashSet : values) {
            for (java.util.Map<Long,T> map : hashSet) {
                Parallel.For(map.keySet(), new Parallel.Operation<Long>() {
                    @Override
                    public void perform(final Long pParameter) {
                        synchronized (result) {
                            result.add(new Entry<Long, T>() {

                                final Long key = pParameter;

                                @Override
                                public Long getKey() {
                                    return key;
                                }

                                @Override
                                public T getValue() {
                                    return DataLongMap.this.get(key);
                                }

                                @Override
                                public T setValue(T pValue) {
                                    return DataLongMap.this.put(key,pValue);
                                }
                            });
                        }
                    }

                    @Override
                    public boolean follow() {
                        return true;
                    }
                });
            }
        }
        return result;
    }
}
