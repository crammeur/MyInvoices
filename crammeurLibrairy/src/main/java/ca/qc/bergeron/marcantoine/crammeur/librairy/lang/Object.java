package ca.qc.bergeron.marcantoine.crammeur.librairy.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Values;

/**
 * Created by Marc-Antoine on 2017-06-26.
 */

public abstract class Object implements Serializable {

    private transient LinkedList<Field> resultGetAllFields = null;
    private transient LinkedList<Field> resultGetAllSerializableFields = null;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        resultGetAllFields = null;
        resultGetAllSerializableFields = null;
    }

    public static LinkedList<Field> getAllFields(final Class<?> pType) {
        LinkedList<Field> fs = new LinkedList<Field>();
        fs.addAll(Arrays.asList(pType.getDeclaredFields()));
        if (pType.getSuperclass() != null) {
            fs.addAll(getAllFields(pType.getSuperclass()));
        }
        return fs;
    }

    public static LinkedList<Field> getAllSerializableFields(final Class<?> pType) {
        LinkedList<Field> fs = new LinkedList<Field>();
        for (Field f : Arrays.asList(pType.getDeclaredFields())) {
            if (!Modifier.isTransient(f.getModifiers()) && (!Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers()))) {
                fs.add(f);
            }
        }
        if (pType.getSuperclass() != null) {
            fs.addAll(getAllSerializableFields(pType.getSuperclass()));
        }
        return fs;
    }

    public static <T> T createObject(@NotNull final Class<T> pClazz, @Nullable final Map<Field, java.lang.Object> pMap) throws IllegalAccessException {
        T result = null;
        Constructor constructor = null;
        for (Constructor c : pClazz.getConstructors()) {
            if (constructor == null || constructor.getParameterTypes().length > c.getParameterTypes().length) {
                constructor = c;
                if (constructor.getParameterTypes().length == 0) break;
            }
        }
        if (constructor != null) {
            if (constructor.getParameterTypes().length == 0) {
                try {
                    result = (T) constructor.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } else {
                final Class<?>[] clazzs = constructor.getParameterTypes();
                final java.lang.Object[] params = new java.lang.Object[clazzs.length];
                for (int arrayIndex = 0; arrayIndex<clazzs.length; arrayIndex++) {
                    params[arrayIndex] = Values.defaultValueFor(clazzs[arrayIndex]);
                }
                try {
                    result = (T) constructor.newInstance(params);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            if (pMap != null) {
                Class<?> clazz = pClazz;
                Field[] fields;
                do {
                    fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if (pMap.containsKey(field)) {
                            if (!Modifier.isFinal(field.getModifiers()) || !Modifier.isStatic(field.getModifiers())) {
                                final boolean b = field.isAccessible();
                                try {
                                    field.setAccessible(true);
                                    field.set(result,pMap.get(field));
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                } finally {
                                    field.setAccessible(b);
                                }
                            } else {
                                throw new IllegalAccessException("Field static and final can't be modified");
                            }
                        }
                    }
                } while ((clazz = clazz.getSuperclass()) != null);
            }
        }
        return result;
    }

    public static <T> T cloneObject(@NotNull final T pObject) {
        T result = null;
        Constructor constructor = null;
        for (Constructor c : pObject.getClass().getConstructors()) {
            if (constructor == null || constructor.getParameterTypes().length > c.getParameterTypes().length) {
                constructor = c;
                if (constructor.getParameterTypes().length == 0) break;
            }
        }
        if (constructor != null) {
            if (constructor.getParameterTypes().length == 0) {
                try {
                    result = (T) constructor.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } else {
                final Class<?>[] clazzs = constructor.getParameterTypes();
                final java.lang.Object[] params = new java.lang.Object[clazzs.length];
                for (int arrayIndex = 0; arrayIndex<clazzs.length && arrayIndex<params.length; arrayIndex++) {
                    params[arrayIndex] = Values.defaultValueFor(clazzs[arrayIndex]);
                }
                try {
                    result = (T) constructor.newInstance(params);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            Class<?> clazz = pObject.getClass();
            Field[] fields;
            do {
                fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (!Modifier.isFinal(field.getModifiers()) || !Modifier.isStatic(field.getModifiers())) {
                        final boolean b = field.isAccessible();
                        try {
                            field.setAccessible(true);
                            field.set(result,field.get(pObject));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        } finally {
                            field.setAccessible(b);
                        }
                    }
                }
            } while ((clazz = clazz.getSuperclass()) != null);
        }
        return result;
    }

    public static <T> T updateObject(@NotNull final T pObject, @NotNull final Map<Field, java.lang.Object> pMap) throws IllegalAccessException {
        for (final Field field : getAllFields(pObject.getClass())) {
            final boolean b = field.isAccessible();
            try {
                field.setAccessible(true);
                if (pMap.containsKey(field)) {
                    if (!Modifier.isFinal(field.getModifiers()) || !Modifier.isStatic(field.getModifiers()))
                        field.set(pObject, pMap.get(field));
                    else
                        throw new IllegalAccessException("Field static and final can't be modified");
                }
            } finally {
                field.setAccessible(b);
            }
        }
        return pObject;
    }

    public static String toGenericString(final Class<?> pClass, final java.lang.Object pObject) {
        final StringBuffer sb = new StringBuffer(pClass.getSimpleName() + "{");
        final Field[] fields = pClass.getDeclaredFields();
        for (int index = 0; index < fields.length; index++) {
            if (!Modifier.isTransient(fields[index].getModifiers()) && (!Modifier.isStatic(fields[index].getModifiers()) && !Modifier.isFinal(fields[index].getModifiers()))) {
                final boolean b = fields[index].isAccessible();
                try {
                    if (sb.toString().lastIndexOf("{") != sb.toString().length() - 1 && sb.toString().lastIndexOf(", ") != sb.toString().length() - 1) {
                        sb.append(", ");
                    }

                    fields[index].setAccessible(true);
                    sb.append(fields[index].getName() + "=");
                    if (String.class.isAssignableFrom(fields[index].getType())) {
                        sb.append("'" + fields[index].get(pObject) + "'");
                    } else
                        sb.append(fields[index].get(pObject));

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } finally {
                    fields[index].setAccessible(b);
                }
            }
        }
        if (pClass.getSuperclass() != null) {
            sb.append("} ");
            sb.append(toGenericString(pClass.getSuperclass(), pObject));
        } else {
            sb.append("}");
        }
        return sb.toString();
    }

    public final LinkedList<Field> getAllFields() {
        if (resultGetAllFields == null) resultGetAllFields = getAllFields(this.getClass());
        return resultGetAllFields;
    }

    public final LinkedList<Field> getAllSerializableFields() {
        if (resultGetAllSerializableFields == null)
            resultGetAllSerializableFields = getAllSerializableFields(this.getClass());
        return resultGetAllSerializableFields;
    }

    @NotNull
    public Map<Field, java.lang.Object> toMap() {
        final Map<Field, java.lang.Object> result = new HashMap<Field, java.lang.Object>();
        for (final Field f : this.getAllFields()) {
            try {
                result.put(f, f.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return toGenericString(this.getClass(), this);
    }

    public final boolean equals(final Object pObject) {
        return pObject != null && (this == pObject || this.toString().equals((pObject.toString())));
    }

    @Override
    public int hashCode() {
        final java.lang.Object[] result = new Object[this.getAllFields().size()];
        for (int i = 0; i < result.length; i++) {
            try {
                Field f = this.getAllFields().get(i);
                result[i] = f.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return Objects.hash(result);
    }


}
