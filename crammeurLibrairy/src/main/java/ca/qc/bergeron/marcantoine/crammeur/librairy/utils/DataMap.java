package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-11-26.
 */

public abstract class DataMap<K extends Serializable, V extends Data<K>> extends Map<K,V> implements ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i.DataMap<K,V> {

    @Override
    public final V put(V pData) {
        return this.put(pData.getId(),pData);
    }
}
