package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.ContainsException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.CollectionIterator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.KeySetIterator;

/**
 * Created by Marc-Antoine on 2017-11-28.
 */

public final class DataIntegerMap<T extends Data<Integer>> extends DataMap<Integer,T> {

    public class EntryIntegerSetIterator extends ca.qc.bergeron.marcantoine.crammeur.librairy.utils.EntrySetIterator<T,Integer> {

        protected final HashSet<Entry<Integer,T>> values;
        protected transient volatile int mIndex = NULL_INDEX;

        private EntryIntegerSetIterator(HashSet<Entry<Integer,T>> pHashSet) {
            values = pHashSet;
        }

        public EntryIntegerSetIterator() {
            values = new HashSet<>();
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            mIndex = NULL_INDEX;
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
         * @return
         */
        @Deprecated
        @Override
        public final int collectionIndexOf(@NotNull Integer pIndex) {
            if (pIndex < MIN_INDEX || pIndex >= values.size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
            return pIndex;
        }

        /**
         * Use currentCollection
         * @return
         */
        @Deprecated
        @NotNull
        @Override
        public final Iterable<Collection<Entry<Integer, T>>> allCollections() {
            return new Iterable<Collection<Entry<Integer, T>>>() {
                @NotNull
                @Override
                public Iterator<Collection<Entry<Integer, T>>> iterator() {
                    return new Iterator<Collection<Entry<Integer, T>>>() {

                        private final HashSet<Entry<Integer,T>> values = EntryIntegerSetIterator.this.values;
                        private transient volatile int mIndex = NULL_INDEX;
                        private transient volatile int mSize = 1;

                        @Override
                        public boolean hasNext() {
                            return mIndex + 1 < mSize;
                        }

                        @Override
                        public Collection<Entry<Integer, T>> next() {
                            mIndex++;
                            return values;
                        }
                    };
                }
            };
        }

        @Override
        public final boolean remove(@Nullable Entry<Integer, T> pEntry) {
            return values.remove(pEntry);
        }

        @Override
        public <E2 extends T> boolean retainAll(@NotNull EntryCollectionIterator<E2, Integer> pEntryCollectionIterator) {
            return false;
        }

        @Override
        public final void clear() {
            values.clear();
        }

        @NotNull
        @Override
        public final Set<Entry<Integer, T>> currentCollection() {
            return values;
        }

        /**
         * Use currentCollection
         * @param pIndex
         * @return
         */
        @Deprecated
        @NotNull
        @Override
        public final Set<Entry<Integer, T>> collectionOf(@NotNull Integer pIndex) {
            if (pIndex < MIN_INDEX && pIndex >= values.size()) throw new IndexOutOfBoundsException(String.valueOf(pIndex));
            return values;
        }

        @NotNull
        @Override
        public final Integer count(@Nullable final Entry<Integer, T> pEntity) {
            final int[] result = new int[1];
            Parallel.For(values, new Parallel.Operation<Entry<Integer, T>>() {
                @Override
                public void perform(Entry<Integer, T> pParameter) {
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
        public final void add(@Nullable Entry<Integer, T> pEntity) {
            if (values.contains(pEntity)) throw new ContainsException("the value is already present");
            values.add(pEntity);
        }

        @Override
        public final int nextIndex() {
            if (mIndex + 1 >= values.size()) return values.size();
            return mIndex + 1;
        }

        @Override
        public final boolean hasNext() {
            return mIndex + 1 < values.size();
        }

        @Nullable
        @Override
        public Entry<Integer, T> next() {
            return null;
        }

        @Override
        public final boolean hasPrevious() {
            return mIndex - 1 >= MIN_INDEX;
        }

        @Override
        public final int previousIndex() {
            if (mIndex - 1 < MIN_INDEX) return NULL_INDEX;
            return mIndex- 1;
        }

        @Nullable
        @Override
        public Entry<Integer, T> previous() {
            return null;
        }

        @Override
        public final void set(@Nullable Entry<Integer, T> pEntity) {
            if (!values.add(pEntity))
                if (!values.contains(pEntity)) throw new RuntimeException("The value has not been set");
        }

        @NotNull
        @Override
        public final Iterator<Entry<Integer, T>> iterator() {
            return new EntryIntegerSetIterator(values);
        }
    }

    protected final java.util.Map<Integer, T> values = new HashMap<>();

    @Override
    public Integer size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean containsKey(Integer pKey) {
        return values.containsKey(pKey);
    }

    @Override
    public boolean containsValue(T pValue) {
        return values.containsValue(pValue);
    }

    @Override
    public T get(Integer pKey) {
        return values.get(pKey);
    }

    @Override
    public T put(Integer pKey, T pValue) {
        return values.put(pKey,pValue);
    }

    @Override
    public T remove(Integer pKey) {
        return values.remove(pKey);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public KeySetIterator<Integer> keySet() {
        final KeySetIterator<Integer> result = new KeyIntegerSetIterator();
        Parallel.For(values.keySet(), new Parallel.Operation<Integer>() {
            @Override
            public void perform(Integer pParameter) {
                synchronized (result) {
                    result.add(pParameter);
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        });
        return result;
    }

    @Override
    public CollectionIterator<T, Integer> values() {
        final CollectionIterator<T, Integer> result = new DataIntegerListIterator<>();
        Parallel.For(values.values(), new Parallel.Operation<T>() {
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
        return result;
    }

    @Override
    public EntrySetIterator<T, Integer> entrySet() {
        final EntryIntegerSetIterator result = new EntryIntegerSetIterator();
        Parallel.For(values.keySet(), new Parallel.Operation<Integer>() {
            @Override
            public void perform(final Integer pParameter) {
                synchronized (result) {
                    result.add(new Entry<Integer, T>() {

                        final Integer key = pParameter;

                        @Override
                        public Integer getKey() {
                            return key;
                        }

                        @Override
                        public T getValue() {
                            return values.get(key);
                        }

                        @Override
                        public T setValue(T pValue) {
                            return values.put(key, pValue);
                        }
                    });
                }
            }

            @Override
            public boolean follow() {
                return true;
            }
        });
        return result;
    }
}
