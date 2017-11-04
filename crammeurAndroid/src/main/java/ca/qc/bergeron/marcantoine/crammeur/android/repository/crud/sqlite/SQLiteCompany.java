package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Company;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.SQLiteTemplate;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-01-05.
 */

public final class SQLiteCompany extends SQLiteTemplate<Company,Integer> implements ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteCompany {

    public SQLiteCompany(Repository pRepository, Context context) {
        super(Company.class, Integer.class, pRepository, context);
    }

    @Override
    public void create() {
        mDB.execSQL(CREATE_TABLE_COMPANYS);
    }

    @NonNull
    @Override
    public Integer save(@NonNull Company pData) throws KeyException {
        try {
            ContentValues values = new ContentValues();
            if (pData.Id == null) {
                pData.Id = this.getKey(pData);
            }
            values.put(mId.getAnnotation(Entity.Id.class).name(), mKey.cast(mId.get(pData)));
            values.put(F_COMPANY_NAME, pData.Name);
            values.put(F_COMPANY_EMAIL, pData.EMail.toLowerCase());
            values.put(F_COMPANY_ADDRESS, pData.Address);
            values.put(F_COMPANY_PHONE, pData.Phone);
            values.put(F_COMPANY_TPSCODE, pData.TPSCode);
            values.put(F_COMPANY_TVPCODE, pData.TVPCode);
            values.put(F_COMPANY_TVHCODE, pData.TVHCode);
            if (mId.get(pData) == null || !this.contains(mKey.cast(mId.get(pData)))) {
                mId.set(pData, (int) mDB.insert(T_COMPANYS, null, values));
            } else {
                mDB.update(T_COMPANYS, values, mId.getAnnotation(Entity.Id.class).name() + "=?", new String[]{String.valueOf(mKey.cast(mId.get(pData)))});
            }
            return (Integer) mId.get(pData);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nullable
    public Integer getKey(@NonNull Company pCompany) {
        Integer result = null;
        try {
            if (mId.get(pCompany) != null) return (Integer) mId.get(pCompany);
            String[] columns = new String[] {F_ID};
            String where = "LOWER(" + F_COMPANY_NAME + ")=LOWER(?) AND (LOWER(" + F_COMPANY_EMAIL + ")=LOWER(?) OR " + F_COMPANY_PHONE + "=?)";
            String[] whereArgs = new String[] {pCompany.Name,pCompany.EMail,pCompany.Phone};

            // limit 1 row = "1";
            Cursor cursor = mDB.query(T_COMPANYS, columns, where, whereArgs, null, null, null, "1");
            if (cursor.moveToFirst()) {
                result = cursor.getInt(cursor.getColumnIndex(F_ID));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    protected Company convertCursorToEntity(@NonNull Cursor pCursor) {
        Company o = new Company();
        o.Id = pCursor.getInt(pCursor.getColumnIndex(mId.getAnnotation(Entity.Id.class).name()));
        o.Name = pCursor.getString(pCursor.getColumnIndex(F_COMPANY_NAME));
        o.EMail = pCursor.getString(pCursor.getColumnIndex(F_COMPANY_EMAIL));
        o.Address = pCursor.getString(pCursor.getColumnIndex(F_COMPANY_ADDRESS));
        o.Phone = pCursor.getString(pCursor.getColumnIndex(F_COMPANY_PHONE));
        o.TPSCode = pCursor.getString(pCursor.getColumnIndex(F_COMPANY_TPSCODE));
        o.TVPCode = pCursor.getString(pCursor.getColumnIndex(F_COMPANY_TVPCODE));
        o.TVHCode = pCursor.getString(pCursor.getColumnIndex(F_COMPANY_TVHCODE));
        return o;
    }

    @Override
    protected Integer convertCursorToId(@NonNull Cursor pCursor) {
        Integer result;
        result = pCursor.getInt(pCursor.getColumnIndex(mId.getAnnotation(Entity.Id.class).name()));
        return result;
    }

    public boolean containsName(String pName) {
        String[] columns = new String[] {F_COMPANY_NAME};
        String where = F_COMPANY_NAME + " = ?";
        String[] whereArgs = new String[] {pName};

        // limit 1 row = "1";
        Cursor cursor = mDB.query(T_COMPANYS, columns, where, whereArgs, null, null, null, "1");
        return cursor.moveToFirst();
    }

    public boolean containsEMail(String pEMail) {
        String[] columns = new String[] {F_COMPANY_EMAIL};
        String where = F_COMPANY_EMAIL + " = ?";
        String[] whereArgs = new String[] {pEMail};

        // limit 1 row = "1";
        Cursor cursor = mDB.query(T_COMPANYS, columns, where, whereArgs, null, null, null, "1");
        return cursor.moveToFirst();
    }
}
