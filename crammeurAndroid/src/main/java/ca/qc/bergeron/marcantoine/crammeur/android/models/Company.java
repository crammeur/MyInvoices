package ca.qc.bergeron.marcantoine.crammeur.android.models;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;

import static ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLite.F_ID;

/**
 * Created by Marc-Antoine on 2017-01-05.
 */
public final class Company extends Data<Integer> {
    @Entity.Id(name = F_ID)
    @Nullable
    public Integer Id;
    @NonNull
    public String Name = "";
    @NonNull
    public String EMail = "";
    @NonNull
    public String Address = "";
    @NonNull
    public String Phone = "";
    @NonNull
    public String TPSCode = "";
    @NonNull
    public String TVPCode = "";
    @NonNull
    public String TVHCode = "";

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
