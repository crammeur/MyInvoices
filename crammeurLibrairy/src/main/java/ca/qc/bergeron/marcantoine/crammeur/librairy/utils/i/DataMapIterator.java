package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-09-18.
 */

public interface DataMapIterator<K extends Serializable, V extends Data<K>> extends MapIterator<K,V> {


}
