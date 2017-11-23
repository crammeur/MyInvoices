package ca.qc.bergeron.marcantoine.crammeur.librairy.utils.i;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by Marc-Antoine on 2017-11-23.
 */

interface SerializableValueOf<S extends Serializable> {

    S firstValue();

    S nextValueOf(@NotNull S pValue);

    S previousValueOf(@NotNull S pValue);
}
