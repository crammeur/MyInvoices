package ca.qc.bergeron.marcantoine.crammeur.android.enums;

import android.content.res.Resources;

import ca.qc.bergeron.marcantoine.crammeur.R;

/**
 * Created by Marc-Antoine on 2017-03-12.
 */

public enum Categories {
    Sale(0, R.string.sale);

    public static Resources res = null;

    public final int sqlId;
    public final int id;

    Categories(int pSqlId, int pId) {
        sqlId = pSqlId;
        id = pId;
    }

    public static int getId(Categories pCategorie){
        for (Categories value : values()) {
            if (value == pCategorie) return value.id;
        }
        throw new NullPointerException();
    }

    public static int getSqlId(Categories pCategorie) {
        for (Categories value : values()) {
            if (value == pCategorie) return value.sqlId;
        }
        throw new NullPointerException();
    }

    public static Categories getBySqlId(int pSlqId) {
        for (Categories value : values()) {
            if (value.sqlId == pSlqId) return value;
        }
        throw new NullPointerException();
    }

    public static Categories getById(int pId) {
        for (Categories value : values()) {
            if (value.id == pId) return value;
        }
        throw new NullPointerException();
    }

    private String getName(){ return (String) res.getText(id); }

    @Override
    public String toString() {
        if (res == null) {
            return super.toString();
        }
        else {
            return getName();
        }
    }
}
