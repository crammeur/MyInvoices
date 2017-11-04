package ca.qc.bergeron.marcantoine.crammeur.librairy.models.comparator;

import java.io.Serializable;
import java.util.Comparator;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-06-22.
 */

public class DataComparator<K extends Serializable> implements Comparator<Data<K>> {
    public int compare(Data<K> a, Data<K> b) {
        if (a == b) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return 1;
        } else if (a.equals(b)) {
            return 0;
        } else {
            return a.toString().compareTo(b.toString());
        }
    }
}