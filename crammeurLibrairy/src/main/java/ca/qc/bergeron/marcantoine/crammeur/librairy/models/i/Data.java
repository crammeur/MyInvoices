package ca.qc.bergeron.marcantoine.crammeur.librairy.models.i;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Created by Marc-Antoine on 2017-09-07.
 */

public interface Data<K extends Serializable> extends Comparable<Data<K>>, Serializable {
    @Nullable
    K getId();

    void setId(@Nullable K pId);

    LinkedList<Field> getAllSerializableFields();

    String toJSON();

    boolean equals(Data pData);
}
