package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marc-Antoine on 2017-06-03.
 */

public final class Values {
    private static final Map<Class<?>, Object> defaultValues = new HashMap<>();

    // load
    static {
        defaultValues.put(boolean.class, Boolean.FALSE);
        defaultValues.put(byte.class, (byte) 0);
        defaultValues.put(short.class, (short) 0);
        defaultValues.put(int.class, 0);
        defaultValues.put(long.class, 0L);
        defaultValues.put(char.class, '\0');
        defaultValues.put(float.class, 0.0F);
        defaultValues.put(double.class, 0.0);
        defaultValues.put(Boolean.class, Boolean.FALSE);
        defaultValues.put(Byte.class, (byte) 0);
        defaultValues.put(Short.class, (short) 0);
        defaultValues.put(Integer.class, 0);
        defaultValues.put(Long.class, 0L);
        defaultValues.put(Character.class, '\0');
        defaultValues.put(Float.class, 0.0F);
        defaultValues.put(Double.class, 0.0);
    }

    public static final <T> T defaultValueFor(Class<T> clazz) {
        if (!defaultValues.containsKey(clazz)) return null;
        return (T) defaultValues.get(clazz);
    }

}
