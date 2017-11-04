package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i;

/**
 * Created by Marc-Antoine on 2016-12-27.
 */

public interface SQLiteProduct extends SQLite,SQLiteCompany {

    String T_PRODUCTS = "Products";        // nom de la table
    String F_PRODUCT_NAME = "PRODUCT_NAME";
    String F_PRODUCT_DESCRIPTION = "PRODUCT_DESCRIPTION";
    String F_PRODUCT_PRICE = "PRODUCT_PRICE";
    //String F_PRODUCT_UNIT = "PRODUCT_UNIT";
    String F_PRODUCT_TVA = "PRODUCT_TVA";
    String F_PRODUCT_TPS = "PRODUCT_TPS";
    String F_PRODUCT_TVQ = "PRODUCT_TVQ";

    String CREATE_TABLE_PRODUCTS = "CREATE TABLE " + T_PRODUCTS + " (" +
            F_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            F_PRODUCT_NAME + " TEXT NOT NULL, " +
            F_PRODUCT_DESCRIPTION + " TEXT NOT NULL, " +
            F_PRODUCT_PRICE + " DOUBLE NOT NULL, " +
            //F_PRODUCT_UNIT + " DOUBLE NOT NULL, " +
            F_PRODUCT_TVA + " DOUBLE NOT NULL, " +
            F_PRODUCT_TPS + " DOUBLE NOT NULL, " +
            F_PRODUCT_TVQ + " DOUBLE NOT NULL, " +
            "CHECK(" + F_PRODUCT_NAME + " <> '')" +
            ")";

}
