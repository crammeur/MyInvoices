package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import ca.qc.bergeron.marcantoine.crammeur.android.repository.Repository;
import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.DeleteException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.DataFramework;

/**
 * Created by Marc-Antoine on 2017-04-02.
 */

public abstract class JDBCTemplate<T extends Data<K>,K extends Serializable> implements DataFramework<T,K> {

    protected final Class<T> mClazz;
    protected final Class<K> mKey;
    protected final String mTableName;
    protected final ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository mRepository;
    private final Connection mConnection;
    protected Field mId = null;
    protected List<Field> mFields = new ArrayList<>();



    public JDBCTemplate(Class<T> pClass, Class<K> pKey, Repository pRepository, Connection pConnection) {
        mClazz = pClass;
        synchronized (mClazz) {
            mKey = pKey;
            String dbName;
            if (!mClazz.isAnnotationPresent(Entity.class) || (dbName = mClazz.getAnnotation(Entity.class).dbName()) == "")
                dbName = mClazz.getSimpleName() + "s";
            mTableName = dbName;
            mRepository = pRepository;
            Class c = mClazz;
            while (c != null) {
                for (Field f : c.getFields()) {
                    if (!(Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) && !Modifier.isTransient(f.getModifiers())) {
                        if (f.isAnnotationPresent(Entity.Id.class)) {
                            if (mId != null) throw new RuntimeException();
                            mId = f;
                        } else {
                            mFields.add(f);
                        }
                    }
                }
                c = c.getSuperclass();
            }
            if (mId == null) throw new NullPointerException(Entity.Id.class.getName());
            mConnection = pConnection;
            try {
                if (mConnection.isClosed()) throw new RuntimeException("Connection is closed");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    @NonNull
    @Override
    public Collection<K> save(@NonNull T... pDatas) throws KeyException {
        List<K> result = new ArrayList<>();
        for (T data : pDatas) {
            result.add(this.save(data));
        }
        return result;
    }

    @NonNull
    @Override
    public Collection<T> getAll() {
        synchronized (mClazz) {
            try {
                List<T> result = new ArrayList<>();
                PreparedStatement ps = mConnection.prepareStatement("SELECT * FROM " + mTableName);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.isWrapperFor(mClazz)) {
                        result.add(rs.unwrap(mClazz));
                    } else {

                    }
                }
                return result;
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    @NonNull
    @Override
    public SortedSet<K> getAllKeys() {
        return null;
    }

    @Nullable
    @Override
    public T getByKey(@NonNull K pKey) {
        return null;
    }

    @Nullable
    @Override
    public K getKey(@NonNull T pEntity) {
        return null;
    }

    @NonNull
    @Override
    public Collection<T> getByKeys(@NonNull Set<K> pKeys) {
        return null;
    }

    @Override
    public boolean contains(@NonNull K pKey) {
        return false;
    }

    @Override
    public void delete(@NonNull K pKey) throws KeyException, DeleteException {
        synchronized (mClazz) {
            try {
                PreparedStatement ps = mConnection.prepareStatement("DELETE FROM " + mTableName + " WHERE ?=?");
                ps.setString(0, pKey.getClass().getAnnotation(Entity.Id.class).name());
                ps.setObject(1, pKey.toString());
                if (!ps.execute()) throw new DeleteException("Not executed");
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void clear() {
        synchronized (mClazz) {
            try {
                mConnection.prepareStatement("TRUNCATE TABLE " + mTableName).execute();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
