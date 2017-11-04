package ca.qc.bergeron.marcantoine.crammeur.librairy.gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Marc-Antoine on 2017-07-15.
 */

public class ListOfSomething<X> implements ParameterizedType {

    private Class<X> wrapped;

    public ListOfSomething(Class<X> wrapped) {
        this.wrapped = wrapped;
    }

    public Type[] getActualTypeArguments() {
        return new Type[]{wrapped};
    }

    public Type getRawType() {
        return List.class;
    }

    public Type getOwnerType() {
        return null;
    }

}
