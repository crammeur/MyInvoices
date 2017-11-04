package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud.files;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;

/**
 * Created by Marc-Antoine on 2017-06-11.
 */

public class Index<K extends Serializable> implements Serializable, Iterable<K> {

    public Set<K> mKeys = new HashSet<K>();
    public Map<K, Boolean> onDeleteCascade = new HashMap<K, Boolean>();
    public Map<K, Boolean> onUpdateCascade = new HashMap<K, Boolean>();

    public final String toString() {
        return Data.toGenericString(this.getClass(), this);
    }

    @Override
    public Iterator<K> iterator() {
        return mKeys.iterator();
    }

    public boolean contains(K pKey) {
        return mKeys.contains(pKey);
    }

    public final void clear() {
        mKeys.clear();
        onDeleteCascade.clear();
        onUpdateCascade.clear();
    }
}
