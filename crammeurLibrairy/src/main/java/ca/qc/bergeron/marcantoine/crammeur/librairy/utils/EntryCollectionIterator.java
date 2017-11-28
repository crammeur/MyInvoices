package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;

import ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map;

/**
 * Created by Marc-Antoine on 2017-11-27.
 */

abstract class EntryCollectionIterator<T, S extends Serializable> extends CollectionIterator<Map.Entry<S,T>,S> implements Map.EntryCollectionIterator<T,S> {

    @Override
    public final <E2 extends T> boolean removeAll(@NotNull Map.EntryCollectionIterator<E2, S> pEntryCollectionIterator) {
        final boolean[] result = new boolean[1];
        result[0] = true;
        for (Collection<Map.Entry<S,E2>> collection : pEntryCollectionIterator.allCollections()) {
            Parallel.For(collection, new Parallel.Operation<Map.Entry<S, E2>>() {

                @Override
                public void perform(final Map.Entry<S, E2> pParameter) {
                    if (!EntryCollectionIterator.this.remove(new Map.Entry<S, T>() {

                        final S key = pParameter.getKey();

                        @Override
                        public S getKey() {
                            return key;
                        }

                        @Override
                        public T getValue() {
                            return pParameter.getValue();
                        }

                        @Override
                        public T setValue(T pValue) {
                            java.util.Map<Field, java.lang.Object> map = new HashMap<>();
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
                                E2 value = Object.updateObject(pParameter.getValue(),map);
                                return pParameter.setValue(value);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                                throw new RuntimeException(e);
                            }
                        }
                    })) {
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
    public final boolean equals(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.Map.EntryCollectionIterator<T,S> pEntryCollectionIterator) {
        return super.equals(pEntryCollectionIterator);
    }
}
