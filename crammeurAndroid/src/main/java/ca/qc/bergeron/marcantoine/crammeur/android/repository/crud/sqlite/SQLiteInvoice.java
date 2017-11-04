package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Client;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Company;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Product;
import ca.qc.bergeron.marcantoine.crammeur.android.models.data.ShopProduct;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.SQLiteTemplate;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteInvoiceProduct;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Serialize;

/**
 * Created by Marc-Antoine on 2017-01-07.
 */

public final class SQLiteInvoice extends SQLiteTemplate<Invoice,Integer> implements ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteInvoice,SQLiteInvoiceProduct {
    public SQLiteInvoice(Repository pRepository, Context context) {
        super(Invoice.class, Integer.class, pRepository, context);
    }

    @Override
    @NonNull
    protected Cursor selectAll() {
        Cursor mCursor = mDB.rawQuery("SELECT * FROM " + mTableName + " JOIN " + T_INVOICES_PRODUCTS + " ON "+ mTableName +"."+F_ID +"=" + T_INVOICES_PRODUCTS+"."+F_INVOICE_PRODUCT_IID ,null);
        return mCursor;
    }

    @NonNull
    @Override
    protected Cursor selectAll(Integer pLimit, Integer pOffset) {
        Cursor mCursor = mDB.rawQuery("SELECT * FROM " + mTableName + " JOIN " + T_INVOICES_PRODUCTS + " ON "+ mTableName +"."+F_ID +"=" + T_INVOICES_PRODUCTS+"."+F_INVOICE_PRODUCT_IID + " LIMIT ? OFFSET ?",new String[]{String.valueOf(pLimit),String.valueOf(pOffset)});
        return mCursor;
    }

    @Override
    @NonNull
    protected Cursor selectByIds(Integer... pIds) {
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
        Cursor mCursor = mDB.rawQuery("SELECT * FROM " + mTableName + " JOIN " + T_INVOICES_PRODUCTS + " ON " + mTableName +"."+F_ID + "=" + T_INVOICES_PRODUCTS+"."+F_INVOICE_PRODUCT_IID + " WHERE " + mTableName +"."+mId.getAnnotation(Entity.Id.class).name() + " IN (" + sbParam + ")",keys);
        return mCursor;
    }

    @Override
    @NonNull
    protected Cursor selectById(Integer pId) {
        Cursor mCursor = mDB.rawQuery("SELECT * FROM  " + mTableName + " JOIN " + T_INVOICES_PRODUCTS + " ON " + mTableName +"."+F_ID + "=" + T_INVOICES_PRODUCTS+"."+F_INVOICE_PRODUCT_IID + " WHERE " + mTableName +"."+mId.getAnnotation(Entity.Id.class).name() + "=?", new String[]{String.valueOf(pId)});
        return mCursor;
    }

    @Override
    protected Invoice convertCursorToEntity(@NonNull Cursor pCursor) {
        Invoice result = new Invoice();
        result.Id = pCursor.getInt(pCursor.getColumnIndex(mId.getAnnotation(Entity.Id.class).name()));
        result.Date = DateTime.class.cast(Serialize.deserialize(pCursor.getBlob(pCursor.getColumnIndex(F_INVOICE_DATE))));
        result.Details = pCursor.getString(pCursor.getColumnIndex(F_INVOICE_DETAILS));
        result.Company = mRepository.getByKey(Company.class, pCursor.getInt(pCursor.getColumnIndex(F_INVOICE_COMPANY)));
        result.Client = mRepository.getByKey(Client.class, pCursor.getInt(pCursor.getColumnIndex(F_INVOICE_CLIENT)));
        result.TVH = pCursor.getDouble(pCursor.getColumnIndex(F_INVOICE_TVH));
        result.TPS = pCursor.getDouble(pCursor.getColumnIndex(F_INVOICE_TPS));
        result.TVP = pCursor.getDouble(pCursor.getColumnIndex(F_INVOICE_TVQ));
        //result.Subtotal = pCursor.getDouble(pCursor.getColumnIndex(F_INVOICE_SUBTOTAL));
        result.Total = pCursor.getDouble(pCursor.getColumnIndex(F_INVOICE_TOTAL));
        do {
            ShopProduct sp = new ShopProduct();
            sp.Product = mRepository.getByKey(Product.class, pCursor.getInt(pCursor.getColumnIndex(F_INVOICE_PRODUCT_PID)));
            sp.Quantity = pCursor.getInt(pCursor.getColumnIndex(F_INVOICE_PRODUCT_QUANTITY));
            sp.Subtotal = pCursor.getDouble(pCursor.getColumnIndex(F_INVOICE_PRODUCT_TOTAL));
            sp.TPS = pCursor.getDouble(pCursor.getColumnIndex(F_INVOICE_PRODUCT_GST));
            sp.TVP = pCursor.getDouble(pCursor.getColumnIndex(F_INVOICE_PRODUCT_PST));
            sp.TVH = pCursor.getDouble(pCursor.getColumnIndex(F_INVOICE_PRODUCT_HST));
            result.Products.add(sp);
        } while (pCursor.moveToNext() && pCursor.getInt(pCursor.getColumnIndex(mId.getAnnotation(Entity.Id.class).name())) == result.Id);
        if (!pCursor.isAfterLast())
            if (!pCursor.moveToPrevious()) throw new RuntimeException();
        return result;
    }

    @Override
    protected Integer convertCursorToId(@NonNull Cursor pCursor) {
        Integer result;
        result = pCursor.getInt(pCursor.getColumnIndex(mId.getAnnotation(Entity.Id.class).name()));
        return result;
    }

    @Override
    public void create() {
        mDB.execSQL(CREATE_TABLE_INVOICES);
    }

    @NonNull
    @Override
    public Integer save(@NonNull Invoice pData) throws KeyException {
        pData.Client.Id = mRepository.save(pData.Client);
        pData.Company.Id = mRepository.save(pData.Company);
        try {
            ContentValues values = new ContentValues();
            values.put(mId.getAnnotation(Entity.Id.class).name(), (Integer) mId.get(pData));
            values.put(F_INVOICE_DATE, Serialize.serialize(pData.Date));
            values.put(F_INVOICE_DETAILS, pData.Details);
            values.put(F_INVOICE_COMPANY, pData.Company.Id);
            values.put(F_INVOICE_CLIENT, pData.Client.Id);
            values.put(F_INVOICE_TVH, pData.TVH);
            values.put(F_INVOICE_TPS, pData.TPS);
            values.put(F_INVOICE_TVQ, pData.TVP);
            //values.put(F_INVOICE_SUBTOTAL, pData.Subtotal);
            values.put(F_INVOICE_TOTAL, pData.Total);
            if (mId.get(pData) == null || !this.contains(mKey.cast(mId.get(pData)))) {
                mId.set(pData, (int) mDB.insert(T_INVOICES, null, values));
                if (mId.get(pData).equals(-1)) throw new KeyException("Key = -1");
            } else {
                mDB.update(T_INVOICES, values, mId.getAnnotation(Entity.Id.class).name() + " = ?", new String[]{String.valueOf(pData.Id)});
                mDB.execSQL("DELETE FROM " + T_INVOICES_PRODUCTS + " WHERE " + F_INVOICE_PRODUCT_IID + "=" + String.valueOf(pData.Id));
            }
            for (ShopProduct p : pData.Products) {
                p.Product.Id = mRepository.save(p.Product);
                ContentValues productValues = new ContentValues();
                productValues.put(F_INVOICE_PRODUCT_IID, pData.Id);
                productValues.put(F_INVOICE_PRODUCT_PID, p.Product.Id);
                productValues.put(F_INVOICE_PRODUCT_QUANTITY, p.Quantity);
                productValues.put(F_INVOICE_PRODUCT_HST, p.TVH);
                productValues.put(F_INVOICE_PRODUCT_GST, p.TPS);
                productValues.put(F_INVOICE_PRODUCT_PST, p.TVP);
                productValues.put(F_INVOICE_PRODUCT_TOTAL, p.Subtotal);
                mDB.insert(T_INVOICES_PRODUCTS, null, productValues);
                /*if (mDB.rawQuery("SELECT * FROM " + T_INVOICES_PRODUCTS + " WHERE " + F_INVOICE_PRODUCT_IID + "=? AND " + F_INVOICE_PRODUCT_PID + "=?",
                        new String[]{String.valueOf(pData.Id),String.valueOf(p.Product.Id)}).moveToFirst()) {
                    mDB.update(T_INVOICES_PRODUCTS,productValues,F_INVOICE_PRODUCT_IID + "=? AND " + F_INVOICE_PRODUCT_PID + "=?",new String[]{String.valueOf(pData.Id),String.valueOf(p.Product.Id)});
                } else
                    mDB.insert(T_INVOICES_PRODUCTS, null, productValues);*/
            }
            return (Integer) mId.get(pData);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
