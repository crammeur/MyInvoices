package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i;

/**
 * Created by Marc-Antoine on 2017-01-11.
 */

public interface SQLiteClient extends SQLite {
    String T_CLIENTS = "Clients";        // nom de la table
    String F_CLIENT_NAME = "CLIENT_NAME";
    String F_CLIENT_EMAIL = "CLIENT_EMAIL";

    String CREATE_TABLE_CLIENTS = "CREATE TABLE IF NOT EXISTS " + T_CLIENTS + " ( " +
            F_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            F_CLIENT_NAME + " TEXT NOT NULL COLLATE NOCASE, " +
            F_CLIENT_EMAIL + " TEXT NOT NULL COLLATE NOCASE," +
            //"CHECK(" + F_CLIENT_NAME + " <> ''), " +
            "CHECK(" + F_CLIENT_EMAIL + " <> ''), " +
            "UNIQUE(" + F_CLIENT_EMAIL + "," + F_CLIENT_NAME + ") ON CONFLICT REPLACE" +
            " )";
}
