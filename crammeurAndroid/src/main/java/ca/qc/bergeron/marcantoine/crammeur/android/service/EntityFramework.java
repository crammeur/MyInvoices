package ca.qc.bergeron.marcantoine.crammeur.android.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.DeleteException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.DataFramework;

/**
 * Created by Marc-Antoine on 2017-01-09.
 */

public class EntityFramework<T extends Data<K>, K extends Serializable> implements ca.qc.bergeron.marcantoine.crammeur.android.service.i.EntityFramework<T,K> {

    protected final DataFramework<T, K> mDataFramework;

    public EntityFramework(DataFramework<T, K> pDataFramework) {
        mDataFramework = pDataFramework;
    }

    @NonNull
    @Override
    public K save(@NonNull T pData) throws KeyException {
        return mDataFramework.save(pData);
    }

    @NonNull
    @Override
    public Collection<K> save(@NonNull T... pDatas) throws KeyException {
        return mDataFramework.save(pDatas);
    }

    @NonNull
    @Override
    public Collection<T> getAll() {
        return mDataFramework.getAll();
    }

    @NonNull
    @Override
    public Collection<T> getAll(@NonNull K pLimit, @NonNull K pOffset) {
        return mDataFramework.getAll(pLimit,pOffset);
    }

    @NonNull
    @Override
    public SortedSet<K> getAllKeys() {
        return mDataFramework.getAllKeys();
    }

    @Nullable
    @Override
    public T getByKey(@NonNull K pKey) {
        return mDataFramework.getByKey(pKey);
    }

    @NonNull
    @Override
    public Collection<T> getByKeys(@NonNull Set<K> pKeys) {
        return mDataFramework.getByKeys(pKeys);
    }

    @Nullable
    @Override
    public K getKey(@NonNull T pEntity) {
        return mDataFramework.getKey(pEntity);
    }

    @Override
    public void clear() {
        mDataFramework.clear();
    }

    @Override
    public boolean contains(@NonNull K pKey) {
        return mDataFramework.contains(pKey);
    }

    @Override
    public int count() {
        return mDataFramework.count();
    }

    @Override
    public void delete(@NonNull K pKey) throws KeyException, DeleteException {
        mDataFramework.delete(pKey);
    }
}
