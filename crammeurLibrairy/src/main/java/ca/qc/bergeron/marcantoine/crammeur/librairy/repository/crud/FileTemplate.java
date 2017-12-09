package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.DeleteException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud.file.Index;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud.file.Settings;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-06-20.
 */

public class FileTemplate<T extends Data<Integer>> extends CRUD<T, Integer> {

    private transient final static Map<String, Boolean> loaded = new HashMap<String, Boolean>();
    //DAO
    protected transient volatile static Map<String, TreeMap<Integer, Data<Integer>>> mTableKeyData = new HashMap<String, TreeMap<Integer, Data<Integer>>>();
    //Index
    protected volatile static Map<Class, Map<Serializable, Map<Class, Index<Integer>>>> mGlobalIndex = new HashMap<Class, Map<Serializable, Map<Class, Index<Integer>>>>();
    protected final File mBase;
    protected final File mFile;
    protected final File mSettingsFile;
    protected final File mGlobalIndexFile;
    protected final boolean onDeleteCascade;
    protected final boolean onUpdateCascade;
    protected final boolean foreignKey;
    //Settings
    protected volatile Settings<Integer> mSettings = new Settings<Integer>();

    public FileTemplate(Class<T> pClass, Repository pRepository, File pBase, boolean pDeleteCascade, boolean pUpdateCascade, boolean pForeignKey) {
        super(pClass, Integer.class, pRepository);
        mBase = new File(pBase, mTableName);
        onDeleteCascade = pDeleteCascade;
        onUpdateCascade = pUpdateCascade;
        foreignKey = pForeignKey;

        mFile = new File(mBase, "values");
        mSettingsFile = new File(mBase, "settings");
        mGlobalIndexFile = new File(pBase, "mIndex");
        if (!mTableKeyData.containsKey(mTableName)) {
            mTableKeyData.put(mTableName, new TreeMap<Integer, Data<Integer>>());
        }
        if (mGlobalIndex.get(mClazz) == null) {
            mGlobalIndex.put(mClazz, new HashMap<Serializable, Map<Class, Index<Integer>>>());
        }
        create();
        if (!loaded.containsKey(mTableName) || loaded.get(mTableName) == null)
            loaded.put(mTableName, false);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        updateFile();
        updateFilesSettings();
    }

    protected void loadFile() {
        try {
            if (mFile.exists()) {
                FileInputStream fis = new FileInputStream(mFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                mTableKeyData.put(mTableName, (TreeMap<Integer, Data<Integer>>) ois.readObject());
                ois.close();
                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected void updateFile() {
        try {
            if (mFile.createNewFile() || mFile.exists()) {
                FileOutputStream fos = new FileOutputStream(mFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(mTableKeyData.get(mTableName));
                oos.close();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected final void loadFilesSettings() {
        try {
            if (mSettingsFile.exists()) {
                FileInputStream fis = new FileInputStream(mSettingsFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                mSettings = (Settings<Integer>) ois.readObject();
                ois.close();
                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try {
            if (mGlobalIndexFile.exists()) {
                FileInputStream fis = new FileInputStream(mGlobalIndexFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                mGlobalIndex = (Map<Class, Map<Serializable, Map<Class, Index<Integer>>>>) ois.readObject();
                ois.close();
                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected final void updateFilesSettings() {
        try {
            if (mSettingsFile.exists() || mSettingsFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(mSettingsFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(mSettings);
                oos.close();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        this.updateFileIndex();
    }

    private void updateFileIndex() {
        try {
            if (mGlobalIndexFile.exists() || mGlobalIndexFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(mGlobalIndexFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(mGlobalIndex);
                oos.close();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected final Integer nextAvailableId() {
        synchronized (mKey) {
            if (!loaded.get(mTableName)) {
                loaded.put(mTableName, true);
                this.loadFilesSettings();
                this.loadFile();
            }

            if (mSettings.nextAvailableId == null) {
                SortedSet<Integer> keys = this.getAllKeys();
                if (keys.isEmpty())
                    mSettings.nextAvailableId = mKey.cast(1);
                else
                    mSettings.nextAvailableId = mKey.cast(keys.last() + 1);
            }

            Integer result = mSettings.nextAvailableId;

            mSettings.nextAvailableId = mKey.cast(Integer.parseInt(String.valueOf(mSettings.nextAvailableId)) + 1);
            return result;
        }
    }

    @NotNull
    @Override
    public final Integer save(@NotNull T pData) throws KeyException {
        if (!loaded.get(mTableName)) {
            loaded.put(mTableName, true);
            this.loadFilesSettings();
            this.loadFile();
        }

        boolean insert = pData.getId() == null || !this.contains(pData.getId());
        if (pData.getId() == null) {
            Integer key;
            if ((key = getKey(pData)) == null) {
                pData.setId(nextAvailableId());
            } else {
                pData.setId(key);
                insert = false;
            }
        }

        /*Map<Class<T>, Map<Integer, Map<T, Index<Integer>>>> delta = new HashMap<>();*/
        if (foreignKey || onUpdateCascade || onDeleteCascade) {
            for (Field f : pData.getAllSerializableFields()) {
                Index<Integer> fIndex;
                if (Data.class.isAssignableFrom(f.getType())) {
                    try {
                        boolean b = f.isAccessible();
                        f.setAccessible(true);
                        Data<Serializable> d = (Data) f.get(pData);
                        f.setAccessible(b);

                        fIndex = new Index<Integer>();
                        if (f.isAnnotationPresent(Entity.Cascade.class)) {
                            fIndex.onDeleteCascade.put(pData.getId(), f.getAnnotation(Entity.Cascade.class).onDeleteCascade());
                            fIndex.onUpdateCascade.put(pData.getId(), f.getAnnotation(Entity.Cascade.class).onUpdateCascade());
                        } else {
                            fIndex.onDeleteCascade.put(pData.getId(), onDeleteCascade);
                            fIndex.onUpdateCascade.put(pData.getId(), onUpdateCascade);
                        }

                        if (d.getId() == null) {
                            d.setId(mKey.cast(mRepository.<Data, Integer>getKey((Class<Data>) f.getType(), d)));
                            if (!fIndex.onUpdateCascade.get(pData.getId()) && d.getId() == null)
                                throw new KeyException("Key is null");
                        }

                        if (fIndex.onUpdateCascade.get(pData.getId()))
                            d.setId(mRepository.save(d));

                        if (d.getId() != null && mRepository.contains(d.getClass(), d.getId())) {

                            /*if (delta.get(d.getClass()) == null)
                                delta.put((Class<T>) d.getClass(), new HashMap<Integer, Map<T, Index<Integer>>>());
                            if (delta.get(d.getClass()).get(d.getId()) == null)
                                delta.get(d.getClass()).put(d.getId(), new HashMap<T, Index<Integer>>());
                            if (delta.get(d.getClass()).get(d.getId()).get(d.getClass()) == null)
                                delta.get(d.getClass()).get(d.getId()).put(pData, fIndex);
                            if (!delta.get(d.getClass()).get(d.getId()).get(pData).contains(pData.getId()))
                                delta.get(d.getClass()).get(d.getId()).get(pData).mKeys.add(pData.getId());*/

                            if (mGlobalIndex.get(d.getClass()) == null)
                                mGlobalIndex.put(d.getClass(), new HashMap<Serializable, Map<Class, Index<Integer>>>());
                            if (!mGlobalIndex.get(d.getClass()).containsKey(d.getId()))
                                mGlobalIndex.get(d.getClass()).put(d.getId(), new HashMap<Class, Index<Integer>>());
                            if (!mGlobalIndex.get(d.getClass()).get(d.getId()).containsKey(pData.getClass()))
                                mGlobalIndex.get(d.getClass()).get(d.getId()).put(pData.getClass(), fIndex);
                            if (!mGlobalIndex.get(d.getClass()).get(d.getId()).get(pData.getClass()).contains(pData.getId()))
                                mGlobalIndex.get(d.getClass()).get(d.getId()).get(pData.getClass()).mKeys.add(pData.getId());

                        } else if (d.getId() == null)
                            throw new KeyException("Key is null");

                    } catch (IllegalAccessException

                            e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                } else if (Iterable.class.isAssignableFrom(f.getType()) && ParameterizedType.class.isInstance(f.getGenericType()) &&
                        Data.class.isAssignableFrom(((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0].getClass())) {
                    try {
                        boolean b = f.isAccessible();
                        f.setAccessible(true);
                        Iterable<Data<Integer>> i = (Iterable) f.get(pData);
                        f.setAccessible(b);

                        fIndex = new Index<Integer>();
                        if (f.isAnnotationPresent(Entity.Cascade.class)) {
                            fIndex.onDeleteCascade.put(pData.getId(), f.getAnnotation(Entity.Cascade.class).onDeleteCascade());
                            fIndex.onUpdateCascade.put(pData.getId(), f.getAnnotation(Entity.Cascade.class).onUpdateCascade());
                        } else {
                            fIndex.onDeleteCascade.put(pData.getId(), onDeleteCascade);
                            fIndex.onUpdateCascade.put(pData.getId(), onUpdateCascade);
                        }

                        for (Data d : i) {
                            if (d.getId() == null) {
                                d.setId(mRepository.<Data, Serializable>getKey((Class<Data>) f.getType(), d));
                                if (!fIndex.onUpdateCascade.get(pData.getId()) && d.getId() == null)
                                    throw new KeyException("Key is null");
                            }

                            if (fIndex.onUpdateCascade.get(pData.getId()))
                                d.setId(mRepository.save(d));

                            if (d.getId() != null && mRepository.contains(d.getClass(), d.getId())) {

                                /*if (delta.get(d.getClass()) == null)
                                    delta.put((Class<T>) d.getClass(), new HashMap<Integer, Map<T, Index<Integer>>>());
                                if (delta.get(d.getClass()).get(d.getId()) == null)
                                    delta.get(d.getClass()).put(mKey.cast(d.getId()), new HashMap<T, Index<Integer>>());
                                if (delta.get(d.getClass()).get(d.getId()).get(d.getClass()) == null)
                                    delta.get(d.getClass()).get(d.getId()).put(pData, fIndex);
                                if (!delta.get(d.getClass()).get(d.getId()).get(pData).contains(pData.getId()))
                                    delta.get(d.getClass()).get(d.getId()).get(pData).mKeys.add(pData.getId());*/

                                if (mGlobalIndex.get(d.getClass()) == null)
                                    mGlobalIndex.put(d.getClass(), new HashMap<Serializable, Map<Class, Index<Integer>>>());
                                if (!mGlobalIndex.get(d.getClass()).containsKey(d.getId()))
                                    mGlobalIndex.get(d.getClass()).put(d.getId(), new HashMap<Class, Index<Integer>>());
                                if (!mGlobalIndex.get(d.getClass()).get(d.getId()).containsKey(pData.getClass()))
                                    mGlobalIndex.get(d.getClass()).get(d.getId()).put(pData.getClass(), fIndex);
                                if (!mGlobalIndex.get(d.getClass()).get(d.getId()).get(pData.getClass()).contains(pData.getId()))
                                    mGlobalIndex.get(d.getClass()).get(d.getId()).get(pData.getClass()).mKeys.add(pData.getId());

                            } else if (d.getId() == null)
                                throw new KeyException("Key is null");
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        if ((insert && !mTableKeyData.get(mTableName).containsKey(pData.getId())) || (!insert && mTableKeyData.get(mTableName).containsKey(pData.getId())))
            mTableKeyData.get(mTableName).put(pData.getId(), pData);
        else
            throw new RuntimeException("Insert or Update error");

        updateFile();
        updateFilesSettings();

        return pData.getId();
    }

    @NotNull
    @Override
    public final List<Integer> save(@NotNull T... pDatas) throws KeyException {
        List<Integer> result = new ArrayList<Integer>();
        for (T data : pDatas) {
            result.add(this.save(data));
        }
        return result;
    }

    @NotNull
    @Override
    public final List<T> getAll() {
        if (!loaded.get(mTableName)) {
            loaded.put(mTableName, true);
            this.loadFilesSettings();
            this.loadFile();
        }

        return (List<T>) new ArrayList(mTableKeyData.get(mTableName).values());
    }

    @NotNull
    @Override
    public final List<T> getAll(@NotNull Integer pLimit, @NotNull Integer pOffset) {
        if (!loaded.get(mTableName)) {
            loaded.put(mTableName, true);
            this.loadFilesSettings();
            this.loadFile();
        }

        List<T> result = new ArrayList<T>();
        Object[] array = this.getAllKeys().toArray();
        for (int index = pOffset; index < array.length && index - pOffset <= pLimit; index++) {
            T data = this.getByKey(mKey.cast(array[index]));
            if (data != null)
                result.add(data);
        }
        return result;
    }

    @NotNull
    @Override
    public final SortedSet<Integer> getAllKeys() {
        if (!loaded.get(mTableName)) {
            loaded.put(mTableName, true);
            this.loadFilesSettings();
            this.loadFile();
        }
        return (SortedSet<Integer>) (mTableKeyData.get(mTableName).keySet());
    }

    @Nullable
    @Override
    public final T getByKey(@NotNull Integer pKey) {
        if (!loaded.get(mTableName)) {
            loaded.put(mTableName, true);
            this.loadFilesSettings();
            this.loadFile();
        }
        if (!this.contains(pKey)) return null;
        //Store double don't know why
        T result = mClazz.cast(mTableKeyData.get(mTableName).get(pKey));
        if (foreignKey) {
            for (Field field : result.getAllSerializableFields()) {
                if (Data.class.isAssignableFrom(field.getType())) {
                    try {
                        boolean b = field.isAccessible();
                        field.setAccessible(true);
                        field.set(result, mRepository.getByKey((Class<Data>) field.getType(), ((Data) field.get(result)).getId()));
                        field.setAccessible(b);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                } else if (Iterable.class.isAssignableFrom(field.getType()) && ParameterizedType.class.isInstance(field.getGenericType()) &&
                        Data.class.isAssignableFrom(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getClass())) {
                    try {
                        boolean b = field.isAccessible();
                        field.setAccessible(true);
                        for (Data data : (Iterable<? extends Data>) field.get(result)) {
                            field.set(result, mRepository.getByKey((Class<Data>) field.getType(), data.getId()));
                        }
                        field.setAccessible(b);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return result;
    }

    @Nullable
    @Override
    public final Integer getKey(@NotNull T pEntity) {
        if (!loaded.get(mTableName)) {
            loaded.put(mTableName, true);
            this.loadFilesSettings();
            this.loadFile();
        }
        if (pEntity.getId() != null) return pEntity.getId();

        Integer result = null;
        for (T data : this.getAll()) {
            Integer key = data.getId();
            data.setId(null);
            if (data.equals((ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data) pEntity))
                result = key;
            data.setId(key);
            if (result != null)
                break;
        }

        return result;
    }

    @NotNull
    @Override
    public final List<T> getByKeys(@NotNull Set<Integer> pKeys) {
        List<T> result = new ArrayList<T>();
        for (Integer key : pKeys) {
            result.add(this.getByKey(key));
        }
        return result;
    }

    @Override
    public final boolean contains(@NotNull Integer pKey) {
        //return this.getAllKeys().contains(pKey);
        boolean result = false;
        try {
            final Iterator<Integer> iterator = this.getAllKeys().iterator();
            while (iterator.hasNext() && !result) {
                //Return double all time don't know why
                final Integer i = iterator.<Integer>next();
                if (pKey.equals(i)) result = true;
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return result;
    }

    @Override
    public final void delete(@NotNull Integer pKey) throws KeyException, DeleteException {
        if (!this.contains(pKey)) throw new KeyException("Key is not in database");
        mTableKeyData.get(mTableName).remove(pKey);
        updateFile();
        if (mGlobalIndex.containsKey(mClazz) && mGlobalIndex.get(mClazz).containsKey(pKey)) {
            for (Class table : mGlobalIndex.get(mClazz).get(pKey).keySet()) {
                for (Integer key : mGlobalIndex.get(mClazz).get(pKey).get(table)) {
                    if (mGlobalIndex.get(mClazz).get(pKey).get(table).onDeleteCascade.get(key)) {
                        mRepository.delete(table, key);
                    }

                    if (mGlobalIndex.containsKey(table) && mGlobalIndex.get(table).containsKey(key) &&
                            mGlobalIndex.get(table).get(key).containsKey(mClazz) && mGlobalIndex.get(table).get(key).get(mClazz).contains(pKey)) {
                        mGlobalIndex.get(table).get(key).get(mClazz).mKeys.remove(pKey);
                    }
                }
            }
            mGlobalIndex.get(mClazz).remove(pKey);
            updateFileIndex();
        }
    }

    @Override
    public final void clear() {
        System.gc();
        if (mFile.exists() && !mFile.delete()) throw new RuntimeException("Delete file values");
        mTableKeyData.get(mTableName).clear();
        mGlobalIndex.clear();
        updateFilesSettings();
    }

    @Override
    public final int count() {
        return this.getAllKeys().size();
    }

    @Override
    public final void create() {
        if (!mBase.exists() && !mBase.mkdirs())
            throw new RuntimeException("Folder : " + mBase.getName());
    }

    @Override
    public final void drop() {
        if (mBase.exists())
            FilesTemplate.deleteFolder(mBase);
        mSettings.clear();
        mTableKeyData.clear();
    }
}
