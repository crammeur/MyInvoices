/*
 * Copyright (c) 2016.
 */

package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.DeleteException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine Bergeron on 2016-07-02.
 */
public interface DataFramework<T extends Data<K>, K extends Serializable> {

    /**
     * create or update data
     *
     * @param pData data
     * @return key
     */
    @NotNull
    K save(@NotNull T pData) throws KeyException;

    @NotNull
    Collection<K> save(@NotNull T... pDatas) throws KeyException;

    /**
     * @return all data
     */
    @NotNull
    Collection<T> getAll();

    /**
     * @param pLimit
     * @param pOffset
     * @return offset data limit
     */
    @NotNull
    Collection<T> getAll(@NotNull K pLimit, @NotNull K pOffset);

    /**
     * @return all keys
     */
    @NotNull
    SortedSet<K> getAllKeys();

    /**
     * Get data by key
     *
     * @param pKey key
     * @return data
     */
    @Nullable
    T getByKey(@NotNull K pKey);

    /**
     * Get id from data
     *
     * @param pEntity
     * @return key
     */
    @Nullable
    K getKey(@NotNull T pEntity);

    /**
     * @param pKeys
     * @return
     */
    @NotNull
    Collection<T> getByKeys(@NotNull Set<K> pKeys);

    /**
     * Check if contains pKey
     *
     * @param pKey key
     * @return
     */
    boolean contains(@NotNull K pKey);

    /**
     * delete data by the key
     *
     * @param pKey data
     * @throws KeyException
     * @throws DeleteException
     */
    void delete(@NotNull K pKey) throws KeyException, DeleteException;

    /**
     * delete all data
     */
    void clear();


    /**
     * Count number of data
     *
     * @return number of data
     */
    int count();
}
