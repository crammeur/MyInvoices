package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteCompany;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteInvoice;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteInvoiceProduct;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteProduct;
/**
 * Created by Marc-Antoine on 2016-10-30.
 */

public final class SQLiteDatabaseHelper extends SQLiteOpenHelper implements SQLiteProduct,SQLiteCompany,SQLiteInvoice,SQLiteInvoiceProduct{

    public final static String DATABASE_NAME = "database.db";
    public final static int DATABASE_VERSION = 3;

    //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/My Invoices/" +
    public SQLiteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CLIENTS);
        db.execSQL(CREATE_TABLE_COMPANYS);
        db.execSQL(CREATE_TABLE_PRODUCTS);
        db.execSQL(CREATE_TABLE_INVOICES);
        db.execSQL(CREATE_TABLE_INVOICES_PRODUCTS);
        db.execSQL("CREATE VIEW ShopProduct AS \n" +
                " SELECT *\n" +
                " FROM " + T_INVOICES + "\n" +
                " JOIN " + T_INVOICES_PRODUCTS + "\n" +
                " ON " + T_INVOICES+"."+F_ID + "=" + T_INVOICES_PRODUCTS+"."+F_INVOICE_PRODUCT_IID);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
            // Enable auto vaccum (truncate) when delete data
            db.execSQL("PRAGMA auto_vacuum=FULL");
        }
    }
}
