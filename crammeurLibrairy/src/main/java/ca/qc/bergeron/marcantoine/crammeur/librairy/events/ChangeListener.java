package ca.qc.bergeron.marcantoine.crammeur.librairy.events;

import java.io.Serializable;
import java.util.EventListener;

/**
 * Created by Marc-Antoine on 2018-02-10.
 */

public interface ChangeListener<I extends Serializable, E> extends EventListener {
    void create(I pIndex, E pElement);
    void update(I pIndex, E pElement, E pPreviousElement);
    void delete(I pIndex, E pElement);
    void clear();
}
