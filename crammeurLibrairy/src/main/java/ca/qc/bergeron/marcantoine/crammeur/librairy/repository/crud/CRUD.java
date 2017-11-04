package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.DataFramework;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-06-15.
 */

public abstract class CRUD<T extends Data<K>, K extends Serializable> extends DataFramework<T, K> {

    protected final Field mId;
    protected final Set<Field> mFields;

    protected CRUD(Class<T> pClass, Class<K> pKey, Repository pRepository) {
        super(pClass, pKey, pRepository);
        Class c = pClass;
        Field fId = null;
        Set<Field> lFields = new HashSet<Field>();
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                if (!(Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) && !Modifier.isTransient(f.getModifiers())) {
                    if (f.isAnnotationPresent(Entity.Id.class)) {
                        if (fId != null) throw new RuntimeException();
                        fId = f;
                    } else {
                        lFields.add(f);
                    }
                }
            }
            c = c.getSuperclass();
        }
        mId = fId;
        mFields = lFields;
        if (mId == null) throw new NullPointerException(Entity.Id.class.getName());
    }

    /**
     * Create table
     */
    public abstract void create();

    /**
     * Drop table
     */
    public abstract void drop();
}
