package ca.qc.bergeron.marcantoine.crammeur.android.service.i;

import java.io.Serializable;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.DataFramework;

/**
 * Created by Marc-Antoine on 2017-01-09.
 */

public interface EntityFramework<T extends Data<K>,K extends Serializable> extends DataFramework<T,K> {
}
