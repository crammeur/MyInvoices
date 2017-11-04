package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i;

/**
 * Created by Marc-Antoine on 2017-01-07.
 */

public interface SQLiteInvoice extends SQLite,SQLiteProduct,SQLiteClient {

    String T_INVOICES = "Invoices"; // nom de la table
    String F_INVOICE_DATE = "INVOICE_DATE";
    String F_INVOICE_DETAILS = "INVOICE_DETAILS";
    String F_INVOICE_COMPANY = "INVOICE_COMPANY";
    String F_INVOICE_CLIENT = "INVOICE_CLIENT";
    String F_INVOICE_TVH = "INVOICE_HST";
    String F_INVOICE_TPS = "INVOICE_GST";
    String F_INVOICE_TVQ = "INVOICE_PST";
    String F_INVOICE_TOTAL = "INVOICE_TOTAL";


    String CREATE_TABLE_INVOICES = "CREATE TABLE " + T_INVOICES + " (" +
            F_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            F_INVOICE_DATE + " BLOB NOT NULL, " +
            F_INVOICE_COMPANY + " INTEGER NOT NULL, " +
            F_INVOICE_CLIENT  + " INTEGER NOT NULL, " +
            F_INVOICE_DETAILS + " TEXT NOT NULL, " +
            F_INVOICE_TVH + " DOUBLE NOT NULL, " +
            F_INVOICE_TPS + " DOUBLE NOT NULL, " +
            F_INVOICE_TVQ + " DOUBLE NOT NULL, " +
            F_INVOICE_TOTAL + " DOUBLE NOT NULL, " +
            "INVOICE_CREATED DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY ("+ F_INVOICE_COMPANY +") REFERENCES " + T_COMPANYS + "(" + F_ID + ") ON DELETE CASCADE, " +
            "FOREIGN KEY ("+ F_INVOICE_CLIENT +") REFERENCES " + T_CLIENTS + "(" + F_ID + ") ON DELETE CASCADE " +
            ")";
}
