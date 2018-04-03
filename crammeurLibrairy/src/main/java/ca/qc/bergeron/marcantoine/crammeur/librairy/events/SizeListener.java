package ca.qc.bergeron.marcantoine.crammeur.librairy.events;

import java.io.Serializable;
import java.util.EventListener;

/**
 * Created by Marc-Antoine on 2017-12-07.
 */

public interface SizeListener<I extends Serializable> extends EventListener {
    void change(I pIndex, I pDelta);
}
