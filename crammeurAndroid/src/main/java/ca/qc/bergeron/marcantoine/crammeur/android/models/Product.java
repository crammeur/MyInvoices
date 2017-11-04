/*
 * Copyright (c) 2016.
 */

package ca.qc.bergeron.marcantoine.crammeur.android.models;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;

import static ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLite.F_ID;

/**
 * Created by Marc-Antoine on 2016-10-15.
 */
public final class Product extends Data<Integer> {
    @Entity.Id(name = F_ID)
    @Nullable
    public Integer Id;
    @NonNull
    public String Name;
    @NonNull
    public String Description = "";
    public double Price = 0;
    //TVH %
    public double TVH = 0;
    //TPS %
    public double TPS = 5;
    //TVP %
    public double TVP = 9.975;

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
