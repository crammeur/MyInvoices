package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Product;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.SQLiteTemplate;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteInvoiceProduct;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-01-05.
 */

public final class SQLiteProduct extends SQLiteTemplate<Product,Integer> implements ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteProduct,SQLiteInvoiceProduct {

    public SQLiteProduct(Repository pRepository, Context context) {
        super(Product.class,Integer.class, pRepository, context);
    }

    @Override
    protected Product convertCursorToEntity(@NonNull Cursor pCursor) {
        Product o = new Product();
        o.Id = pCursor.getInt(pCursor.getColumnIndex(mId.getAnnotation(Entity.Id.class).name()));
        o.Name = pCursor.getString(pCursor.getColumnIndex(F_PRODUCT_NAME));
        o.Description = pCursor.getString(pCursor.getColumnIndex(F_PRODUCT_DESCRIPTION));
        o.Price = pCursor.getDouble(pCursor.getColumnIndex(F_PRODUCT_PRICE));
        //o.Unit = pCursor.getDouble(pCursor.getColumnIndex(F_PRODUCT_UNIT));
        o.TVH = pCursor.getDouble(pCursor.getColumnIndex(F_PRODUCT_TVA));
        o.TPS = pCursor.getDouble(pCursor.getColumnIndex(F_PRODUCT_TPS));
        o.TVP = pCursor.getDouble(pCursor.getColumnIndex(F_PRODUCT_TVQ));
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
        mDB.execSQL(CREATE_TABLE_PRODUCTS);
    }

    @NonNull
    @Override
    public Integer save(@NonNull Product pData) throws KeyException {
        ContentValues values = new ContentValues();
        try {
            if (pData.Id == null) {
                pData.Id = this.getKey(pData);
            }
            values.put(mId.getAnnotation(Entity.Id.class).name(), mKey.cast(mId.get(pData)));
            values.put(F_PRODUCT_NAME, pData.Name);
            values.put(F_PRODUCT_DESCRIPTION, pData.Description);
            values.put(F_PRODUCT_PRICE, pData.Price);
            //values.put(F_PRODUCT_UNIT, pData.Unit);
            values.put(F_PRODUCT_TVA, pData.TVH);
            values.put(F_PRODUCT_TPS, pData.TPS);
            values.put(F_PRODUCT_TVQ, pData.TVP);
            boolean b = this.contains(mKey.cast(mId.get(pData)));
            if (mId.get(pData) == null || !b) {
                mId.set(pData, (int) mDB.insert(T_PRODUCTS, null, values));
            } else {
                mDB.update(T_PRODUCTS, values, mId.getAnnotation(Entity.Id.class).name() + " = ?", new String[]{String.valueOf(pData.Id)});
            }
            return pData.Id;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nullable
    public Integer getKey(@NonNull Product pProduct) {
        Integer result = null;
        try {
            if (mId.get(pProduct) != null) return (Integer) mId.get(pProduct);
            String[] columns = new String[] {F_ID};
            String where = "LOWER(" + F_PRODUCT_NAME + ")=LOWER(?) AND LOWER(" + F_PRODUCT_DESCRIPTION + ")=LOWER(?) AND " + F_PRODUCT_PRICE + "=? AND " + F_PRODUCT_TPS + "=? AND " + F_PRODUCT_TVQ + "=? AND " + F_PRODUCT_TVA + "=?";
            String[] whereArgs = new String[] {pProduct.Name,pProduct.Description,String.valueOf(pProduct.Price),String.valueOf(pProduct.TPS),String.valueOf(pProduct.TVP),String.valueOf(pProduct.TVH)};

            // limit 1 row = "1";
            Cursor cursor = mDB.query(T_PRODUCTS, columns, where, whereArgs, null, null, null, "1");
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
