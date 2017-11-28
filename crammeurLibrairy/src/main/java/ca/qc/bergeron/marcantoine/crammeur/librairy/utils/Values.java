package ca.qc.bergeron.marcantoine.crammeur.librairy.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marc-Antoine on 2017-06-03.
 */

public final class Values {
    public static final Map<Class<?>, Object> DEFAULT_VALUES = new HashMap<>();

    // load
    static {
        DEFAULT_VALUES.put(boolean.class, Boolean.FALSE);
        DEFAULT_VALUES.put(byte.class, (byte) 0);
        DEFAULT_VALUES.put(short.class, (short) 0);
        DEFAULT_VALUES.put(int.class, 0);
        DEFAULT_VALUES.put(long.class, 0L);
        DEFAULT_VALUES.put(char.class, '\0');
        DEFAULT_VALUES.put(float.class, 0.0F);
        DEFAULT_VALUES.put(double.class, 0.0);
        DEFAULT_VALUES.put(Boolean.class, Boolean.FALSE);
        DEFAULT_VALUES.put(Byte.class, (byte) 0);
        DEFAULT_VALUES.put(Short.class, (short) 0);
        DEFAULT_VALUES.put(Integer.class, 0);
        DEFAULT_VALUES.put(Long.class, 0L);
        DEFAULT_VALUES.put(Character.class, '\0');
        DEFAULT_VALUES.put(Float.class, 0.0F);
        DEFAULT_VALUES.put(Double.class, 0.0);
    }

    public static <T> T defaultValueFor(Class<T> clazz) {
        if (!DEFAULT_VALUES.containsKey(clazz)) return null;
        return (T) DEFAULT_VALUES.get(clazz);
    }

}
