package ca.qc.bergeron.marcantoine.crammeur.android.repository.crud;

import android.content.Context;
import android.os.AsyncTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Marc-Antoine on 2017-03-29.
 */

public final class JDBCConnect extends AsyncTask<Context, Integer, Connection> {
    @Override
    protected Connection doInBackground(Context... params) {
        Connection connection;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://x.x.x.x", "root", "toor");
            return connection;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
