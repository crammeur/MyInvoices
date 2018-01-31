package ca.qc.bergeron.marcantoine.crammeur.librairy.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.GsonBuilder;
import ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.comparator.DataComparator;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Parallel;

/**
 * Created by Marc-Antoine on 2017-01-15.
 */
public abstract class Data<K extends Serializable> extends Object implements ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data<K> {

    @Nullable
    @Override
    public abstract K getId();

    @Override
    public abstract void setId(@Nullable K pId);

    @Override
    public int compareTo(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data<K> kData) {
        return new DataComparator<K>().compare(this, kData);
    }

    @Override
    public final String toString() {
        return ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object.toGenericString(this.getClass(),this);
    }

    @Override
    public final boolean equals(java.lang.Object obj) {
        if (ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data.class.isAssignableFrom(obj.getClass())) {
            return this.equals((ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data)obj);
        } else {
            return this == obj;
        }
    }

    public final boolean equals(final ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data pObject) {
        final boolean[] result = new boolean[1];
        result[0] = pObject != null && (this == pObject || this.toString().equals(pObject.toString()));
        if (!result[0]) {
            final Map<Field,java.lang.Object> fieldObjectMap = this.toMap();
            final Map<Field,java.lang.Object> fieldObjectMap2 = pObject.toMap();
            final Set<Field> fields = fieldObjectMap.keySet();
            final Set<Field> fields2 = fieldObjectMap2.keySet();
            if (result[0] = fields.size() == fields2.size()) {
                Parallel.For(fields, new Parallel.Operation<Field>() {
                    Iterator<Field> fieldIterator = fields2.iterator();
                    @Override
                    public void perform(Field pParameter) {
                        Field field = fieldIterator.next();
                        java.lang.Object object1 = fieldObjectMap.get(pParameter);
                        java.lang.Object object2 = fieldObjectMap2.get(field);
                        if (!field.getName().contains("$") && !field.getName().equals(pParameter.getName()) || (object1 != null && object2 != null && !object1.toString().equals(object2.toString()))) {
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
        }
        return result[0];
    }

    @Override
    public final boolean equals(ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data pData) {
        return this.equals((java.lang.Object)pData);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final String toJSON() {
        GsonBuilder builder = new GsonBuilder<Data<K>, K>((Class<Data<K>>) this.getClass());
        return builder.getGson().toJson(this);
    }

    public static <T extends Data<K>, K extends Serializable> T fromJSON(@NotNull Class<T> pClass, @NotNull String pData) {
        GsonBuilder builder = new GsonBuilder<T, K>(pClass);
        return builder.getGson().fromJson(pData, pClass);
    }
}
