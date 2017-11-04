package ca.qc.bergeron.marcantoine.crammeur.myinvoices.setting;

import android.support.annotation.NonNull;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;

/**
 * Created by Marc-Antoine on 2017-08-15.
 */

public final class Settings implements Serializable {
    @NonNull
    public String GSTName = "";
    @NonNull
    public String PSTName = "";
    @NonNull
    public String HSTName = "";

    public Double GST = null;
    public Double PST = null;
    public Double HST = null;

    @Override
    public String toString() {
        return Data.toGenericString(this.getClass(),this);
    }
}
