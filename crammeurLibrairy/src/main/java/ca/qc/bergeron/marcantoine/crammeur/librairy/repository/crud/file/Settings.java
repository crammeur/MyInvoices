package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud.file;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;

/**
 * Created by Marc-Antoine on 2017-06-20.
 */

public final class Settings<K extends Integer> implements Serializable {

    public K nextAvailableId;

    @Override
    public final String toString() {
        return Data.toGenericString(this.getClass(), this);
    }

    public void clear() {
        nextAvailableId = null;
    }
}
