package ca.qc.bergeron.marcantoine.crammeur.librairy.repository;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-06-28.
 */

public abstract class DataFramework<T extends Data<K>, K extends Serializable> implements ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.DataFramework<T, K> {

    protected final Class<T> mClazz;
    protected final Class<K> mKey;
    protected final String mTableName;
    protected final Repository mRepository;

    public DataFramework(Class<T> pClass, Class<K> pKey, Repository pRepository) {
        mClazz = pClass;
        mKey = pKey;
        String dbName;
        if (!pClass.isAnnotationPresent(Entity.class) || (dbName = pClass.getAnnotation(Entity.class).dbName()).equals(""))
            dbName = pClass.getSimpleName() + "s";
        mTableName = dbName;
        mRepository = pRepository;
    }
}
