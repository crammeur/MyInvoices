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
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.DeleteException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud.files.Index;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud.files.Settings;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-05-31.
 */

public class FilesTemplate<T extends Data<K>, K extends Number> extends CRUD<T, K> {

    //DAO
    protected transient volatile static Map<String, SortedMap<Number, Data<Number>>> mKeyData = new HashMap<String, SortedMap<Number, Data<Number>>>();
    //Index
    protected volatile static Map<Class, Map<Serializable, Map<Class, Index<Number>>>> mGlobalIndex = new HashMap<Class, Map<Serializable, Map<Class, Index<Number>>>>();
    protected final File mBase;
    protected final File mFolder;
    protected final File mSettingsFile;
    protected final File mGlobalIndexFile;
    protected final boolean onDeleteCascade;
    protected final boolean onUpdateCascade;
    protected final boolean foreignKey;
    private final int timeWait = 500;
    //Settings
    protected volatile Settings<K> mSettings = new Settings<K>();
    private int loadTryCount;
    private int updateTryCount;
    private int readTryCount;
    private int writeTryCount;
    private SortedSet<K> resultgetAllKeys = new TreeSet<K>();
    private int deleteTryCount = 0;

    public FilesTemplate(Class<T> pClass, Class<K> pKey, Repository pRepository, File pBase, boolean pDeleteCascade, boolean pUpdateCascade, boolean pForeignKey) {
        super(pClass, pKey, pRepository);
        onDeleteCascade = pDeleteCascade;
        onUpdateCascade = pUpdateCascade;
        foreignKey = pForeignKey;

        mBase = new File(pBase, mTableName);
        mFolder = new File(mBase, "values");
        create();

        if (mKeyData.get(mTableName) == null)
            mKeyData.put(mTableName, new TreeMap<Number, Data<Number>>());

        mSettingsFile = new File(mBase, "settings");
        mGlobalIndexFile = new File(pBase, "mIndex");
        loadFilesSettings();

        if (mGlobalIndex.get(mClazz) == null) {
            mGlobalIndex.put(mClazz, new HashMap<Serializable, Map<Class, Index<Number>>>());
        }
    }

    public static synchronized void clearFolder(File pFolder, List<File> pExceptions) {
        if (pFolder.isDirectory()) {
            for (File f : pFolder.listFiles()) {
                if (f.isDirectory() && !pExceptions.contains(f)) {
                    clearFolder(f);
                } else if (!pExceptions.contains(f)) {
                    System.gc();
                    if (!f.delete())
                        throw new RuntimeException("Delete file failed");
                }
            }
        }
    }

    public static synchronized void clearFolder(File pFolder) {
        if (pFolder.isDirectory()) {
            for (File f : pFolder.listFiles()) {
                if (f.isDirectory()) {
                    clearFolder(f);
                } else {
                    System.gc();
                    if (!f.delete())
                        throw new RuntimeException("Delete file failed");
                }
            }
        }
    }

    public static synchronized void deleteFolder(File pFolder) {
        if (pFolder.isDirectory()) {
            clearFolder(pFolder);
            if (!pFolder.delete()) throw new RuntimeException("Delete folder failed");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mSettings.lastModification == mFolder.lastModified())
            updateFilesSettings();
        System.gc();
    }

    protected final void loadFilesSettings() {
        loadTryCount = 0;
        boolean finish = false;
        while (!finish) {
            try {
                if (mSettingsFile.exists()) {
                    FileInputStream fis = new FileInputStream(mSettingsFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    mSettings = (Settings<K>) ois.readObject();
                    ois.close();
                    fis.close();
                }
                finish = true;
            } catch (IOException e) {
                e.printStackTrace();
                if (loadTryCount == 4)
                    throw new RuntimeException(e);
                else {
                    try {
                        Thread.sleep(timeWait);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        throw new RuntimeException(e1);
                    }
                    loadTryCount++;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        loadTryCount = 0;
        finish = false;
        while (!finish) {
            try {
                if (mGlobalIndexFile.exists()) {
                    FileInputStream fis = new FileInputStream(mGlobalIndexFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    mGlobalIndex = (Map<Class, Map<Serializable, Map<Class, Index<Number>>>>) ois.readObject();
                    ois.close();
                    fis.close();
                }
                finish = true;
            } catch (IOException e) {
                e.printStackTrace();
                if (loadTryCount == 4)
                    throw new RuntimeException(e);
                else {
                    try {
                        Thread.sleep(timeWait);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        throw new RuntimeException(e1);
                    }
                    loadTryCount++;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    protected final void updateFilesSettings() {
        updateTryCount = 0;
        boolean finish = false;
        while (!finish) {
            try {
                if (mSettingsFile.exists() || mSettingsFile.createNewFile()) {
                    FileOutputStream fos = new FileOutputStream(mSettingsFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(mSettings);
                    oos.close();
                    fos.close();
                }
                finish = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();
                if (updateTryCount == 4)
                    throw new RuntimeException(e);
                else {
                    try {
                        Thread.sleep(timeWait);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        throw new RuntimeException(e1);
                    }
                    updateTryCount++;
                }
            }
        }

        updateTryCount = 0;
        finish = false;
        while (!finish) {
            try {
                if (mGlobalIndexFile.exists() || mGlobalIndexFile.createNewFile()) {
                    FileOutputStream fos = new FileOutputStream(mGlobalIndexFile);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(mGlobalIndex);
                    oos.close();
                    fos.close();
                }
                finish = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();
                if (updateTryCount == 4)
                    throw new RuntimeException(e);
                else {
                    try {
                        Thread.sleep(timeWait);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        throw new RuntimeException(e1);
                    }
                    updateTryCount++;
                }
            }
        }
    }

    @Override
    public void create() {
        if (!mFolder.exists())
            if (!mFolder.mkdirs()) throw new RuntimeException("Mkdirs fail");
    }

    protected final K nextAvailableId() {
        synchronized (mKey) {
            if (mSettings.nextAvailableId == null) {
                SortedSet<K> keys = this.getAllKeys();
                if (Integer.class.isAssignableFrom(mKey)) {
                    if (keys.isEmpty())
                        mSettings.nextAvailableId = mKey.cast(1);
                    else
                        mSettings.nextAvailableId = mKey.cast((Integer) keys.last() + 1);
                } else if (Long.class.isAssignableFrom(mKey)) {
                    if (keys.isEmpty())
                        mSettings.nextAvailableId = mKey.cast(1l);
                    else
                        mSettings.nextAvailableId = mKey.cast((Long) keys.last() + 1);
                } else
                    throw new RuntimeException("Type is not supported : " + mKey.getName());
            }

            K result = mSettings.nextAvailableId;

            if (Integer.class.isAssignableFrom(mKey)) {
                mSettings.nextAvailableId = mKey.cast(Integer.parseInt(String.valueOf(mSettings.nextAvailableId)) + 1);
            } else if (Long.class.isAssignableFrom(mKey)) {
                mSettings.nextAvailableId = mKey.cast(Long.parseLong(String.valueOf(mSettings.nextAvailableId)) + 1);
            }
            return result;
        }
    }

    @NotNull
    protected T readObjectFromFile(@NotNull File pFile) {
        readTryCount = 0;
        boolean finish = false;
        T result = null;
        while (!finish) {
            try {
                FileInputStream fis = new FileInputStream(pFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                T t = (T) ois.readObject();
                ois.close();
                fis.close();
                mSettings.keyFileName.put(t.getId(), pFile.getName());
                mSettings.keyLastModifications.put(t.getId(), pFile.lastModified());
                mKeyData.get(mTableName).put(t.getId(), (Data<Number>) t);
                result = t;
                finish = true;
            } catch (IOException e) {
                e.printStackTrace();
                if (readTryCount == 4)
                    throw new RuntimeException(e);
                else {
                    try {
                        Thread.sleep(timeWait);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        throw new RuntimeException(e1);
                    }
                    readTryCount++;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    protected void writeObjectToFile(@NotNull File pFile, @NotNull T pData) {
        writeTryCount = 0;
        boolean finish = false;
        while (!finish) {
            try {
                FileOutputStream fos = new FileOutputStream(pFile);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(pData);
                oos.close();
                fos.close();
                mSettings.keyLastModifications.put(pData.getId(), pFile.lastModified());
                mSettings.keyFileName.put(pData.getId(), pFile.getName());
                mKeyData.get(mTableName).put(pData.getId(), (Data<Number>) pData);
                finish = true;
            } catch (IOException e) {
                e.printStackTrace();
                if (writeTryCount == 4)
                    throw new RuntimeException(e);
                else {
                    try {
                        Thread.sleep(timeWait);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        throw new RuntimeException(e1);
                    }
                    writeTryCount++;
                }
            }
        }
    }

    protected File fileFromData(T pData) {
        return new File(mFolder, String.valueOf(pData.getId()));
    }

    @NotNull
    @Override
    public final K save(@NotNull T pData) throws KeyException {
        synchronized (mClazz) {
            if (mSettings.lastModification != mFolder.lastModified())
                loadFilesSettings();
            boolean insert = pData.getId() == null || !this.contains(pData.getId());
            if (pData.getId() == null) {
                K key;
                if ((key = getKey(pData)) == null) {
                    pData.setId(nextAvailableId());
                } else {
                    pData.setId(key);
                    insert = false;
                }
            }

            if (foreignKey || onUpdateCascade || onDeleteCascade) {
                for (Field f : pData.getAllSerializableFields()) {
                    if (Data.class.isAssignableFrom(f.getType())) {
                        try {
                            boolean b = f.isAccessible();
                            f.setAccessible(true);
                            Data<Serializable> d = (Data) f.get(pData);
                            f.setAccessible(b);

                            Index<K> fIndex = new Index<K>();
                            if (f.isAnnotationPresent(Entity.Cascade.class)) {
                                fIndex.onDeleteCascade.put(pData.getId(), f.getAnnotation(Entity.Cascade.class).onDeleteCascade());
                                fIndex.onUpdateCascade.put(pData.getId(), f.getAnnotation(Entity.Cascade.class).onUpdateCascade());
                            } else {
                                fIndex.onDeleteCascade.put(pData.getId(), onDeleteCascade);
                                fIndex.onUpdateCascade.put(pData.getId(), onUpdateCascade);
                            }

                            if (d.getId() == null) {
                                d.setId(mRepository.<Data, Serializable>getKey((Class<Data>) f.getType(), d));
                                if (!fIndex.onUpdateCascade.get(pData.getId()) && d.getId() == null)
                                    throw new KeyException("Key is null");
                            }

                            if (fIndex.onUpdateCascade.get(pData.getId()))
                                mRepository.save(d);

                            if (d.getId() != null && mRepository.contains(d.getClass(), d.getId())) {

                                if (mGlobalIndex.get(d.getClass()) == null)
                                    mGlobalIndex.put(d.getClass(), new HashMap<Serializable, Map<Class, Index<Number>>>());

                                if (!mGlobalIndex.get(d.getClass()).containsKey(d.getId()))
                                    mGlobalIndex.get(d.getClass()).put(d.getId(), new HashMap<Class, Index<Number>>());

                                if (!mGlobalIndex.get(d.getClass()).get(d.getId()).containsKey(pData.getClass()))
                                    mGlobalIndex.get(d.getClass()).get(d.getId()).put(pData.getClass(), (Index<Number>) fIndex);

                                if (!mGlobalIndex.get(d.getClass()).get(d.getId()).get(pData.getClass()).contains(pData.getId()))
                                    mGlobalIndex.get(d.getClass()).get(d.getId()).get(pData.getClass()).mKeys.add(pData.getId());

                            } else if (d.getId() == null)
                                throw new KeyException("Key is null");

                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    } else if (Iterable.class.isAssignableFrom(f.getType()) && ParameterizedType.class.isInstance(f.getGenericType()) &&
                            Data.class.isAssignableFrom(((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0].getClass())) {
                        try {
                            boolean b = f.isAccessible();
                            f.setAccessible(true);
                            Iterable<Data> i = (Iterable) f.get(pData);
                            f.setAccessible(b);
                            Index<K> fIndex = new Index<K>();
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
                                    mRepository.save(d);

                                if (d.getId() != null && mRepository.contains(d.getClass(), d.getId())) {
                                    if (mGlobalIndex.get(d.getClass()) == null)
                                        mGlobalIndex.put(d.getClass(), new HashMap<Serializable, Map<Class, Index<Number>>>());

                                    if (!mGlobalIndex.get(d.getClass()).containsKey(d.getId()))
                                        mGlobalIndex.get(d.getClass()).put(d.getId(), new HashMap<Class, Index<Number>>());

                                    if (!mGlobalIndex.get(d.getClass()).get(d.getId()).containsKey(pData.getClass()))
                                        mGlobalIndex.get(d.getClass()).get(d.getId()).put(pData.getClass(), (Index<Number>) fIndex);

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

            File file = fileFromData(pData);
            if (!file.exists()) {
                if (insert) {
                    try {
                        if (file.createNewFile())
                            mSettings.lastModification = mFolder.lastModified();
                        else
                            throw new RuntimeException("Error create file");
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                } else
                    throw new RuntimeException("File not exist");
            }

            this.writeObjectToFile(file, pData);

            if (mSettings.lastModification == mFolder.lastModified())
                updateFilesSettings();

            return pData.getId();
        }
    }

    @NotNull
    @Override
    public final synchronized List<K> save(@NotNull T... pDatas) throws KeyException {
        List<K> result = new ArrayList<K>();
        for (T data : pDatas) {
            result.add(save(data));
        }
        return result;
    }

    @NotNull
    @Override
    public final List<T> getAll() {
        synchronized (mClazz) {
            List<T> result = new ArrayList<T>();
            if (mSettings.lastModification != mFolder.lastModified()) {
                for (K key : mSettings.keyFileName.keySet()) {
                    File f = new File(mFolder, mSettings.keyFileName.get(key));
                    if (!f.exists()) {
                        if (mKeyData.get(mTableName).containsKey(key))
                            mKeyData.get(mTableName).remove(key);
                    }
                }
                loadFilesSettings();
            }
            for (File f : mFolder.listFiles()) {
                if (f.exists() && f.isFile() && !f.getName().startsWith(".")) {
                    K key;
                    if (mSettings.keyFileName.containsValue(f.getName()) && (key = mSettings.keyFileName.inverse().get(f.getName())) != null &&
                            mSettings.keyLastModifications.containsKey(key) && mSettings.keyLastModifications.get(key) == f.lastModified() &&
                            mKeyData.get(mTableName).containsKey(key) && mKeyData.get(mTableName).get(key) != null) {
                        result.add((T) mKeyData.get(mTableName).get(key));
                    } else {
                        T t = readObjectFromFile(f);
                        if (foreignKey) {
                            for (Field field : t.getAllSerializableFields()) {
                                if (Data.class.isAssignableFrom(field.getType())) {
                                    try {
                                        boolean b = field.isAccessible();
                                        field.setAccessible(true);
                                        field.set(t, mRepository.getByKey((Class<Data>) field.getType(), ((Data) field.get(t)).getId()));
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
                                        for (Data data : (Iterable<? extends Data>) field.get(t)) {
                                            field.set(t, mRepository.getByKey((Class<Data>) field.getType(), data.getId()));
                                        }
                                        field.setAccessible(b);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                        result.add(t);
                    }
                } else if (!f.exists() && f.isFile() && !f.getName().startsWith(".") && mSettings.keyFileName.inverse().containsKey(f.getName())) {
                    mSettings.keyLastModifications.remove(mSettings.keyFileName.inverse().get(f.getName()));
                    if (mKeyData.get(mTableName).containsKey(mSettings.keyFileName.inverse().get(f.getName())))
                        mKeyData.get(mTableName).remove(mSettings.keyFileName.inverse().get(f.getName()));
                    mSettings.keyFileName.inverse().remove(f.getName());
                }
            }
            mSettings.lastModification = mFolder.lastModified();
            return result;
        }
    }

    @NotNull
    @Override
    public final synchronized List<T> getAll(@NotNull K pLimit, @NotNull K pOffset) {
        List<T> result = new ArrayList<T>();
        K index = mKey.cast(0);
        K count = mKey.cast(0);
        for (Iterator<K> iterator = this.getAllKeys().iterator(); iterator.hasNext() && count.longValue() <= pLimit.longValue(); index = mKey.cast(index.longValue() + 1)) {
            if (index.longValue() >= pOffset.longValue() && count.longValue() <= pLimit.longValue()) {
                T data = getByKey(iterator.next());
                if (data != null) {
                    result.add(data);
                    count = mKey.cast(count.longValue() + 1);
                }
            }
        }
        return result;
    }

    @NotNull
    protected String keyFromFileName(@NotNull String pFileName) {
        if (!pFileName.startsWith(".") && pFileName.contains(".")) {
            return pFileName.substring(0, pFileName.indexOf(".") - 1);
        } else {
            return pFileName;
        }
    }

    @Nullable
    protected final K keyFromFile(File pFile) {
        K result;
        if (pFile.isFile()) {
            if (Integer.class.isAssignableFrom(mKey)) {
                Integer key;
                result = (K) (key = Integer.parseInt(keyFromFileName(pFile.getName())));
                mSettings.keyFileName.put((K) key, pFile.getName());
                resultgetAllKeys.add((K) key);
            } else if (Long.class.isAssignableFrom(mKey)) {
                Long key;
                result = (K) (key = Long.parseLong(keyFromFileName(pFile.getName())));
                mSettings.keyFileName.put((K) key, pFile.getName());
                resultgetAllKeys.add((K) key);
            } else
                throw new RuntimeException("Type not supported");
            return result;
        } else
            return null;
    }

    @NotNull
    @Override
    public final SortedSet<K> getAllKeys() {
        synchronized (mKey) {
            if (mSettings.lastModification == mFolder.lastModified()) {
                if (resultgetAllKeys.size() == 0) {
                    for (K key : mSettings.keyFileName.keySet()) {
                        resultgetAllKeys.add(key);
                    }
                }
            } else {
                for (K key : mSettings.keyFileName.keySet()) {
                    File f = new File(mSettings.keyFileName.get(key));
                    if (!f.exists() && mKeyData.get(mTableName).containsKey(key)) {
                        mKeyData.get(mTableName).remove(key);
                        mSettings.keyLastModifications.remove(key);
                    }
                }
                resultgetAllKeys.clear();
                mSettings.keyFileName.clear();
                mSettings.lastModification = mFolder.lastModified();
                for (File f : mFolder.listFiles()) {
                    if (f.isFile() && !f.getName().startsWith(".")) {
                        K key = this.keyFromFile(f);
                        if (f.exists()) {
                            resultgetAllKeys.add(key);
                        } else {
                            if (mKeyData.get(mTableName).containsKey(key))
                                mKeyData.get(mTableName).remove(key);
                            if (mSettings.keyLastModifications.containsKey(key))
                                mSettings.keyLastModifications.remove(key);
                        }
                    }
                }
                updateFilesSettings();
            }
            return resultgetAllKeys;
        }
    }

    @Nullable
    @Override
    public final synchronized K getKey(@NotNull T pEntity) {
        if (pEntity.getId() != null)
            return pEntity.getId();

        K result = null;
        for (K key : this.getAllKeys()) {
            T data = this.getByKey(key);
            if (data != null) {
                data.setId(null);
                if (data.equals((ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data) pEntity))
                    result = key;
                data.setId(key);
                if (result != null)
                    break;
            }
        }

        return result;
    }

    @Nullable
    @Override
    public final T getByKey(@NotNull K pKey) {
        synchronized (mClazz) {
            if (!this.contains(pKey)) return null;
            T result = null;
            File f = new File(mFolder, mSettings.keyFileName.get(pKey));
            if (f.exists()) {
                if (mSettings.keyLastModifications.containsKey(pKey) && mSettings.keyLastModifications.get(pKey) == f.lastModified() &&
                        mKeyData.get(mTableName).containsKey(pKey)) {
                    result = (T) mKeyData.get(mTableName).get(pKey);
                } else {
                    result = readObjectFromFile(f);
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
                }
            } else {
                if (mSettings.keyFileName.containsKey(pKey))
                    mSettings.keyFileName.remove(pKey);
                if (mSettings.keyLastModifications.containsKey(pKey))
                    mSettings.keyLastModifications.remove(pKey);
                if (mKeyData.get(mTableName).containsKey(pKey))
                    mKeyData.get(mTableName).remove(pKey);
            }
            return result;
        }
    }

    @NotNull
    @Override
    public final List<T> getByKeys(@NotNull Set<K> pKeys) {
        synchronized (mClazz) {
            List<T> result = new ArrayList<T>();
            for (K key : pKeys) {
                result.add(getByKey(key));
            }
            return result;
        }
    }

    @Override
    public final void delete(@NotNull K pKey) throws KeyException, DeleteException {
        synchronized (mKey) {
            if (!this.contains(pKey))
                throw new KeyException("Database don't contains the key");
            File f = new File(mFolder, mSettings.keyFileName.get(pKey));
            if (f.exists()) {
                System.gc();
                boolean deleted = false;
                while (deleteTryCount < 5 && !deleted && f.exists()) {
                    if (f.delete()) deleted = true;
                    else {
                        try {
                            Thread.sleep(timeWait);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                            throw new RuntimeException(e1);
                        }
                    }
                    deleteTryCount++;
                }
                if (deleteTryCount == 5 && !deleted && f.exists()) {
                    throw new DeleteException("File as not be deleted");
                }
                deleteTryCount = 0;
                mSettings.lastModification = mFolder.lastModified();
            }

            if (mGlobalIndex.get(mClazz).containsKey(pKey)) {
                for (Class table : mGlobalIndex.get(mClazz).get(pKey).keySet()) {
                    for (Serializable key : mGlobalIndex.get(mClazz).get(pKey).get(table)) {
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
                /*mIndex.get(pKey).clear();*/
            }

            if (mKeyData.get(mTableName).containsKey(pKey))
                mKeyData.get(mTableName).remove(pKey);
            mSettings.keyFileName.remove(pKey);
            mSettings.keyLastModifications.remove(pKey);
            updateFilesSettings();
        }
    }

    @Override
    public final synchronized void clear() {
        if (mFolder.exists())
            clearFolder(mFolder);
        mSettings.lastModification = mFolder.lastModified();
        mSettings.keyFileName.clear();
        mSettings.keyLastModifications.clear();
        mGlobalIndex.clear();
        mKeyData.clear();
        updateFilesSettings();
    }

    @Override
    public void drop() {
        deleteFolder(mBase);
        mSettings.clear();
        mGlobalIndex.clear();
        mKeyData.clear();
    }

    @Override
    public final synchronized boolean contains(@NotNull K pKey) {
        return getAllKeys().contains(pKey);
    }

    @Override
    public final synchronized int count() {
        return getAllKeys().size();
    }
}
