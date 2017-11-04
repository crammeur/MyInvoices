package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i;

/**
 * Created by Marc-Antoine on 2016-12-27.
 */

public interface SQLiteCompany extends SQLite {

    String T_COMPANYS = "Companys";        // nom de la table
    String F_COMPANY_NAME = "COMPANY_NAME";                // nom de chacun des champs (F pour field)
    String F_COMPANY_EMAIL = "COMPANY_EMAIL";
    String F_COMPANY_ADDRESS = "COMPANY_ADDRESS";
    String F_COMPANY_PHONE = "COMPANY_PHONE";
    String F_COMPANY_TVHCODE = "COMPANY_HSTCODE";
    String F_COMPANY_TPSCODE = "COMPANY_GSTCODE";
    String F_COMPANY_TVPCODE = "COMPANY_PSTCODE";

    String CREATE_TABLE_COMPANYS = "CREATE TABLE IF NOT EXISTS " + T_COMPANYS + " (" +
            F_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            F_COMPANY_NAME + " TEXT NOT NULL COLLATE NOCASE, " +
            F_COMPANY_EMAIL + " TEXT NOT NULL COLLATE NOCASE, " +
            F_COMPANY_ADDRESS + " TEXT NOT NULL COLLATE NOCASE, " +
            F_COMPANY_PHONE + " TEXT NOT NULL COLLATE NOCASE, " +
            F_COMPANY_TPSCODE + " TEXT NOT NULL COLLATE NOCASE, " +
            F_COMPANY_TVPCODE + " TEXT NOT NULL COLLATE NOCASE, " +
            F_COMPANY_TVHCODE + " TEXT NOT NULL COLLATE NOCASE, " +
            "CHECK(" + F_COMPANY_NAME + " <> ''), " +
            "CHECK(" + F_COMPANY_EMAIL + " <> '' OR " + F_COMPANY_PHONE + " <> ''), " +
            "UNIQUE(" + F_COMPANY_EMAIL + "," + F_COMPANY_NAME + "," + F_COMPANY_PHONE + ") ON CONFLICT REPLACE" +
            ")";

}
