package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud.CRUD;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-01-05.
 */

public abstract class SQLiteTemplate<T extends Data<K>, K extends Serializable> extends CRUD<T,K> {

    protected final SQLiteDatabase mDB;

    public SQLiteTemplate(Class<T> pClass, Class<K> pKey, Repository pRepository, Context pContext) {
        super(pClass,pKey,pRepository);
        mDB = new SQLiteDatabaseHelper(pContext).getWritableDatabase();
    }

    public T convertCursor(@NonNull Cursor pCursor) {
        try {
            T result = mClazz.newInstance();
            for (Object f : result.getAllSerializableFields()) {
                Field field = (Field) f;
                final boolean b = field.isAccessible();
                field.setAccessible(true);
                switch (field.getType().getName()) {
                    case "java.lang.String" :
                        break;
                    case "java.lang.Object" :
                        break;
                    case "java.lang.Double" :
                        break;
                    case "java.lang.Float" :
                        break;
                    case "java.lang.Long" :
                        break;
                    case "java.lang.Integer" :
                        break;
                    case "java.lang.Short" :
                        break;
                    case "java.lang.Byte" :
                        break;
                    default:
                        String d = field.getType().getName();
                        System.out.println(d);
                }

            }
            return result;
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public K convertCusror(@NonNull Cursor pCursor) {

        return null;
    }

    protected abstract T convertCursorToEntity(@NonNull Cursor pCursor);

    protected abstract K convertCursorToId(@NonNull Cursor pCursor);

    public final List<T> query(String[] pColums, String pSelection, String[] pSelectArgs, String pGroupBy, String pHaving, String pOrderBy, String pLimit) {
        List<T> result = new ArrayList<>();
        Cursor cursor = mDB.query(mTableName,pColums,pSelection,pSelectArgs,pGroupBy,pHaving,pOrderBy,pLimit);
        while (cursor.moveToNext()) {
            result.add(convertCursorToEntity(cursor));
        }
        return result;
    }

    public final List<T> rawQuery(String pSQL, String[] pParams) {
        List<T> result = new ArrayList<>();
        Cursor cursor = mDB.rawQuery(pSQL,pParams);
        while (cursor.moveToNext()) {
            result.add(convertCursorToEntity(cursor));
        }
        return result;
    }

    @NonNull
    protected Cursor selectAll() {
        Cursor cursor = mDB.rawQuery("SELECT * FROM " + mTableName ,null);
        return cursor;
    }

    protected Cursor selectAll(K pLimit, K pOffset) {
        Cursor cursor = mDB.rawQuery("SELECT * FROM " + mTableName + " LIMIT ? OFFSET ?",new String[]{String.valueOf(pLimit),String.valueOf(pOffset)});
        return cursor;
    }

    @NonNull
    protected Cursor selectByIds(K... pIds) {
        StringBuilder sbParam = new StringBuilder();
        String[] keys = new String[pIds.length];
        for (int i=0;i<pIds.length;i++) {
            if (sbParam.toString().equals("")) {
                sbParam.append("?");
            } else {
                sbParam.append(",?");
            }
            keys[i] = String.valueOf(pIds[i]);
        }
        Cursor cursor = mDB.rawQuery("SELECT * FROM " + mTableName + " WHERE " + mId.getAnnotation(Entity.Id.class).name() + " IN (" + sbParam + ")",keys);
        return cursor;
    }

    @NonNull
    protected Cursor selectById(K pId) {
        Cursor mCursor = mDB.rawQuery("SELECT * FROM " + mTableName + " WHERE " + mId.getAnnotation(Entity.Id.class).name() + "=? LIMIT 1", new String[]{String.valueOf(pId)});
        return mCursor;
    }

    @NonNull
    protected final Cursor selectIds() {
        String[] columns = new String[]{mId.getAnnotation(Entity.Id.class).name()};
        Cursor mCursor = mDB.query(mTableName,columns,null,null,null,null,null);
        return mCursor;
    }

    @NonNull
    @Override
    public final List<K> save(@NonNull T... pDatas) throws KeyException {
        List<K> result = new ArrayList<>();
        for (T data : pDatas) {
            result.add(this.save(data));
        }
        return result;
    }

    @NonNull
    @Override
    public final List<T> getAll() {
        List<T> result = new ArrayList<>();
        Cursor c = this.selectAll();
        while (c.moveToNext()) {
            result.add(this.convertCursorToEntity(c));
        }
        return result;
    }

    @NonNull
    @Override
    public final List<T> getAll(K pLimit, K pOffset) {
        List<T> result = new ArrayList<>();
        Cursor c = this.selectAll(pLimit,pOffset);
        while (c.moveToNext()) {
            result.add(this.convertCursorToEntity(c));
        }
        return result;
    }

    @NonNull
    @Override
    public final SortedSet<K> getAllKeys() {
        SortedSet<K> result = new TreeSet<>();
        Cursor c = this.selectIds();
        while (c.moveToNext()) {
            result.add(this.convertCursorToId(c));
        }
        return result;
    }

    @Nullable
    @Override
    public T getByKey(@NonNull K pKey) {
        T result = null;
        Cursor c = this.selectById(pKey);
        if (c.moveToFirst()) {
            result = this.convertCursorToEntity(c);
        }
        return result;
    }

    @Nullable
    @Override
    public K getKey(@NonNull T pEntity) {
        synchronized (mClazz) {
            if (pEntity.getId() != null)
                return pEntity.getId();
            for (T data : this.getAll()) {
                K key = data.getId();
                data.setId(null);
                if (data.equals(pEntity)) {
                    data.setId(key);
                    return key;
                }
            }
            return null;
        }
    }

    @NonNull
    @Override
    public List<T> getByKeys(@NonNull Set<K> pKeys) {
        List<T> result = new ArrayList<>();
        Cursor mCursor = this.selectByIds((K[]) pKeys.toArray());
        while (mCursor.moveToNext()) {
            result.add(this.convertCursorToEntity(mCursor));
        }
        return result;
    }

    @Override
    public boolean contains(@NonNull K pKey) {
        return this.selectById(pKey).moveToFirst();
    }

    @Override
    public void clear() {
        mDB.execSQL("DELETE FROM " + mTableName);
    }

    @Override
    public void drop() {
        mDB.execSQL("DROP " + mTableName);
    }

    @Override
    public int count() {
        Cursor c = mDB.rawQuery("SELECT COUNT(*) FROM " + mTableName, null);
        c.moveToFirst();
        return c.getInt(0);
    }

    @Override
    public void delete(@NonNull K pKey) {
        mDB.execSQL("DELETE FROM " + mTableName + " WHERE " + mId.getAnnotation(Entity.Id.class).name() + "=" + pKey);
    }

}
