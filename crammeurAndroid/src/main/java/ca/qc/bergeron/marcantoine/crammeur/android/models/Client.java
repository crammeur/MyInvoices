package ca.qc.bergeron.marcantoine.crammeur.android.models;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLiteClient;

import static ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLite.F_ID;

/**
 * Created by Marc-Antoine on 2017-01-11.
 */
public final class Client extends Data<Integer> {
    @Entity.Id(name = F_ID)
    @Nullable
    public Integer Id;
    @Entity.Column(name = SQLiteClient.F_CLIENT_NAME)
    @NonNull
    public String Name = "";
    @Entity.Column(name = SQLiteClient.F_CLIENT_EMAIL)
    @NonNull
    public String EMail = "";

    @Nullable
    @Override
    public final Integer getId() {
        return Id;
    }

    @Override
    public void setId(Integer pId) {
        Id = pId;
    }
}
