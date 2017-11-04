package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud.files;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;

/**
 * Created by Marc-Antoine on 2017-06-04.
 */

public final class Settings<K extends Serializable> implements Serializable {

    public K nextAvailableId;
    public long lastModification = 0;
    public BiMap<K, String> keyFileName = HashBiMap.create();
    public Map<K, Long> keyLastModifications = new HashMap<K, Long>();

    @Override
    public final String toString() {
        return Data.toGenericString(this.getClass(), this);
    }

    public void clear() {
        nextAvailableId = null;
        lastModification = 0;
        keyFileName.clear();
        keyLastModifications.clear();
    }
}
