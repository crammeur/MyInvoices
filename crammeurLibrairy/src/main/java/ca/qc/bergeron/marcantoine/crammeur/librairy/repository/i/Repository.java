package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Set;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.DeleteException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-01-05.
 */
public interface Repository {

    /**
     * Insert or Update the data
     *
     * @param <T>
     * @param <K>
     * @param pData
     * @return key
     * @throws KeyException
     */
    @NotNull
    <T extends Data<K>, K extends Serializable> K save(@NotNull T pData) throws KeyException;

    /**
     * @param pClass
     * @param <T>
     * @return
     */
    @NotNull
    <T extends Data> Iterable<T> getAll(@NotNull Class<T> pClass);

    /**
     * @param pClass
     * @param <T>
     * @param <K>
     * @return
     */
    @NotNull
    <T extends Data<K>, K extends Serializable> Set<K> getAllKeys(@NotNull Class<T> pClass);

    @Nullable
    <T extends Data<K>, K extends Serializable> K getKey(@NotNull Class<T> pClass, @NotNull T pData);

    /**
     * @param pClass
     * @param pKey
     * @param <T>
     * @param <K>
     * @return
     */
    @Nullable
    <T extends Data<K>, K extends Serializable> T getByKey(@NotNull Class<T> pClass, @NotNull K pKey);

    /**
     * @param pClass
     * @param pKeys
     * @param <T>
     * @param <K>
     * @return
     */
    @NotNull
    <T extends Data<K>, K extends Serializable> Iterable<T> getByKeys(@NotNull Class<T> pClass, @NotNull Set<K> pKeys);

    /**
     * Check if contains DataFramework for pClass
     *
     * @param pClass model
     * @param <T>
     * @return
     */
    <T extends Data> boolean contains(@NotNull Class<T> pClass);

    /**
     * Check if contain key
     *
     * @param pClass
     * @param pKey
     * @param <T>
     * @param <K>
     * @return
     */
    <T extends Data<K>, K extends Serializable> boolean contains(@NotNull Class<T> pClass, @NotNull K pKey);

    /**
     * Check if contain the data (if id is not null check by id else check by data)
     *
     * @param pClass
     * @param pData
     * @param <T>
     * @return
     */
    <T extends Data> boolean contains(@NotNull Class<T> pClass, @NotNull T pData);

    /**
     * Delete data by key
     *
     * @param pClass
     * @param pKey
     * @param <T>
     * @param <K>
     */
    <T extends Data<K>, K extends Serializable> void delete(@NotNull Class<T> pClass, @NotNull K pKey) throws KeyException, DeleteException;

    /**
     * Clear the table (delete all data) (truncate)
     *
     * @param pClass
     * @param <T>
     */
    <T extends Data> void clear(@NotNull Class<T> pClass);

    /**
     *
     */
    void clear();
}
