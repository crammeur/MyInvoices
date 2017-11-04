package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Client;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.SQLiteTemplate;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-01-11.
 */

public final class SQLiteClient extends SQLiteTemplate<Client,Integer>  implements ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteClient {
    public SQLiteClient(Repository pRepository, Context context) {
        super(Client.class,Integer.class,pRepository, context);
    }

    @Override
    protected Client convertCursorToEntity(@NonNull Cursor pCursor) {
        Client o = new Client();
        o.Id = pCursor.getInt(pCursor.getColumnIndex(mId.getAnnotation(Entity.Id.class).name()));
        o.Name = pCursor.getString(pCursor.getColumnIndex(F_CLIENT_NAME));
        o.EMail = pCursor.getString(pCursor.getColumnIndex(F_CLIENT_EMAIL));
        return o;
    }

    @Override
    protected Integer convertCursorToId(@NonNull Cursor pCursor) {
        Integer result;
        result = pCursor.getInt(pCursor.getColumnIndex(mId.getAnnotation(Entity.Id.class).name()));
        return result;
    }

    @Override
    public void create() {
        mDB.execSQL(CREATE_TABLE_CLIENTS);
    }

    @NonNull
    @Override
    public Integer save(@NonNull Client pData) throws KeyException {
        ContentValues values = new ContentValues();
        try {
            if (pData.Id == null) {
                pData.Id = this.getKey(pData);
            }
            values.put(mId.getAnnotation(Entity.Id.class).name(), mKey.cast(mId.get(pData)));
            values.put(F_CLIENT_NAME, pData.Name);
            values.put(F_CLIENT_EMAIL, pData.EMail);
            if (mId.get(pData) == null || !this.contains(mKey.cast(mId.get(pData)))) {
                mId.set(pData, (int) mDB.insert(T_CLIENTS, null, values));
            } else {
                mDB.update(T_CLIENTS, values, mId.getAnnotation(Entity.Id.class).name() + "=?", new String[]{String.valueOf(pData.Id)});
            }
            return pData.Id;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public Integer getKey(@NonNull Client pEntity) {
        Integer result = null;
        try {
            if (mId.get(pEntity) != null) return (Integer) mId.get(pEntity);
            String[] columns = new String[] {F_ID};
            String where = "LOWER(" + F_CLIENT_NAME + ")=LOWER(?) AND LOWER(" + F_CLIENT_EMAIL + ")=LOWER(?)";
            String[] whereArgs = new String[] {pEntity.Name,pEntity.EMail};

            // limit 1 row = "1";
            Cursor cursor = mDB.query(T_CLIENTS, columns, where, whereArgs, null, null, null, "1");
            if (cursor.moveToFirst()) {
                result = cursor.getInt(cursor.getColumnIndex(F_ID));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }
}
