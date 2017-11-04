package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i;

/**
 * Created by Marc-Antoine on 2017-01-08.
 */
public interface SQLiteInvoiceProduct extends SQLiteProduct,SQLiteInvoice {

    String T_INVOICES_PRODUCTS = "Invoices_Products";
    String F_INVOICE_PRODUCT_IID = "INVOICE_PRODUCT_IID";
    String F_INVOICE_PRODUCT_PID = "INVOICE_PRODUCT_PID";
    String F_INVOICE_PRODUCT_QUANTITY = "INVOICE_PRODUCT_QUANTITY";
    String F_INVOICE_PRODUCT_HST = "INVOICE_PRODUCT_HST";
    String F_INVOICE_PRODUCT_GST = "INVOICE_PRODUCT_GST";
    String F_INVOICE_PRODUCT_PST = "INVOICE_PRODUCT_PST";
    String F_INVOICE_PRODUCT_TOTAL = "INVOICE_PRODUCT_TOTAL";

    String CREATE_TABLE_INVOICES_PRODUCTS = "CREATE TABLE " + T_INVOICES_PRODUCTS + " (" +
            F_INVOICE_PRODUCT_IID + " INTEGER NOT NULL, " +
            F_INVOICE_PRODUCT_PID +" INTEGER NOT NULL, " +
            F_INVOICE_PRODUCT_QUANTITY + " INT NOT NULL, " +
            F_INVOICE_PRODUCT_HST + " DOUBLE NOT NULL, " +
            F_INVOICE_PRODUCT_GST + " DOUBLE NOT NULL, " +
            F_INVOICE_PRODUCT_PST + " DOUBLE NOT NULL, " +
            F_INVOICE_PRODUCT_TOTAL + " DOUBLE NOT NULL, " +
            "FOREIGN KEY (" + F_INVOICE_PRODUCT_IID + ") REFERENCES " + T_INVOICES + "(" + F_ID + ") ON DELETE CASCADE, " +
            "FOREIGN KEY (" + F_INVOICE_PRODUCT_PID + ") REFERENCES " + T_PRODUCTS + "(" + F_ID + ") ON DELETE CASCADE," +
            "PRIMARY KEY (" + F_INVOICE_PRODUCT_IID + "," + F_INVOICE_PRODUCT_PID + ")" +
            ")";
}
