package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
        public <E2 extends Entry<Integer, T>> boolean removeAll(@NotNull CollectionIterator<E2, Integer> pCollectionIterator) {
            return false;
        }

        @Override
        public <E2 extends Entry<Integer, T>> boolean retainAll(@NotNull CollectionIterator<E2, Integer> pCollectionIterator) {
            return false;
        }

        @Override
        public <E2 extends T> boolean retainAll(@NotNull final EntryCollectionIterator<E2, Integer> pEntryCollectionIterator) {
            final boolean[] result = new boolean[1];
            result[0] = true;
            final EntrySetIterator<T,Integer> retain = new EntryIntegerSetIterator();
            for (Collection<Entry<Integer,E2>> collection : pEntryCollectionIterator.allCollections()) {
                Parallel.For(collection, new Parallel.Operation<Entry<Integer, E2>>() {

                    @Override
                    public void perform(final Entry<Integer,E2> pParameter) {
                        retain.add(new Entry<Integer, T>() {

                            final Integer key = pParameter.getKey();

                            @Override
                            public Integer getKey() {
                                return key;
                            }

                            @Override
                            public T getValue() {
                                return pParameter.getValue();
                            }

                            @Override
                            public T setValue(T pValue) {
                                java.util.Map<Field,Object> map = new HashMap<>();
                                Class<?> clazz = pValue.getClass();
                                Field[] fields;
                                do {
                                    fields = clazz.getDeclaredFields();
                                    for (Field field : fields) {
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
                                } while ((clazz = clazz.getSuperclass()) != null);
                                try {
                                    java.util.Map<Field,Object> map2 = new HashMap<>();
                                    Class<?> clazz2 = pParameter.getValue().getClass();
                                    Field[] fields2;
                                    do {
                                        fields2 = clazz2.getDeclaredFields();
                                        for (Field field : fields2) {
                                            boolean b = field.isAccessible();
                                            try {
                                                field.setAccessible(true);
                                                map2.put(field,field.get(pParameter.getValue()));
                                            } finally {
                                                field.setAccessible(b);
                                            }
                                        }
                                    } while ((clazz2 = clazz2.getSuperclass()) != null);
                                    E2 value = ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object.createObject((Class<E2>) pParameter.getValue().getClass(),map2);
                                    return pParameter.setValue(ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object.updateObject(value,map));
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                } catch (InvocationTargetException e) {
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

            final EntrySetIterator<T,Integer> delete = new EntryIntegerSetIterator();
            Parallel.For(this.currentCollection(), new Parallel.Operation<Entry<Integer, T>>() {
                @Override
                public void perform(Entry<Integer, T> pParameter) {
                    if (!retain.contains(pParameter)) {
                        delete.add(pParameter);
                    }
                }

                @Override
                public boolean follow() {
                    return true;
                }
            });

            Parallel.For(delete.currentCollection(), new Parallel.Operation<Entry<Integer, T>>() {
                @Override
                public void perform(Entry<Integer, T> pParameter) {
                    synchronized (result) {
                        result[0] = EntryIntegerSetIterator.this.remove(pParameter);
                        if (!result[0]) throw new RuntimeException();
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

        protected final Entry<Integer, T> actual() {
            if (mIndex != NULL_INDEX && mIndex < values.size()) {
                final Entry<Integer, T>[] result = new Entry[1];
                final int[] index = new int[1];
                index[0] = NULL_INDEX;
                Parallel.For(values, new Parallel.Operation<Entry<Integer, T>>() {
                    @Override
                    public void perform(Entry<Integer, T> pParameter) {
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
                return result[0];
            } else
                throw new IndexOutOfBoundsException(String.valueOf(mIndex));

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
        public final Entry<Integer, T> next() {
            if (hasNext()) {
                mIndex++;
                return actual();
            } else
                throw new NoSuchElementException();
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
        public final Entry<Integer, T> previous() {
            if (hasPrevious()) {
                mIndex--;
                return actual();
            } else
                throw new NoSuchElementException();
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
    public final Integer size() {
        return values.size();
    }

    @Override
    public final boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public final boolean containsKey(Integer pKey) {
        return values.containsKey(pKey);
    }

    @Override
    public final boolean containsValue(T pValue) {
        return values.containsValue(pValue);
    }

    @Override
    public final T get(Integer pKey) {
        return values.get(pKey);
    }

    @Override
    public final T put(Integer pKey, T pValue) {
        return values.put(pKey,pValue);
    }

    @Override
    public final T remove(Integer pKey) {
        return values.remove(pKey);
    }

    @Override
    public final void clear() {
        values.clear();
    }

    @Override
    public final KeySetIterator<Integer> keySet() {
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
    public final CollectionIterator<T, Integer> values() {
        final CollectionIterator<T, Integer> result = new IntegerListIterator<>();
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
    public final EntrySetIterator<T, Integer> entrySet() {
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
