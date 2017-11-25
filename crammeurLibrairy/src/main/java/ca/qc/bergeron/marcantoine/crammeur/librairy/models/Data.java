package ca.qc.bergeron.marcantoine.crammeur.librairy.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.GsonBuilder;
import ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.comparator.DataComparator;

/**
 * Created by Marc-Antoine on 2017-01-15.
 */
public abstract class Data<K extends Serializable> extends Object implements ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data<K> {

    public static <T extends Data<K>, K extends Serializable> T fromJSON(@NotNull Class<T> pClass, @NotNull String pData) {
        GsonBuilder builder = new GsonBuilder<T, K>(pClass);
        return builder.getGson().fromJson(pData, pClass);
    }

    @Nullable
    @Override
    public abstract K getId();

    @Override
    public abstract void setId(@Nullable K pId);

    @Override
    public int compareTo(@Nullable ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data<K> kData) {
        return new DataComparator<K>().compare(this, kData);
    }

    @Override
    public final String toString() {
        return super.toString();
    }

    @Override
    public final boolean equals(ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data pData) {
        return super.equals(pData);
    }

    @Override
    public final String toJSON() {
        GsonBuilder builder = new GsonBuilder<Data<K>, K>((Class<Data<K>>) this.getClass());
        return builder.getGson().toJson(this);
    }

}
