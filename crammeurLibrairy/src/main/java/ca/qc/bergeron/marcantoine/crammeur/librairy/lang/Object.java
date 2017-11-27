package ca.qc.bergeron.marcantoine.crammeur.librairy.lang;

import java.io.Serializable;
import java.lang.reflect.Field;
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

public class Object implements Serializable {

    private transient LinkedList<Field> resultGetAllFields = null;
    private transient LinkedList<Field> resultGetAllSerializableFields = null;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        resultGetAllFields = null;
        resultGetAllSerializableFields = null;
    }

    public static LinkedList<Field> getAllFields(Class<?> pType) {
        LinkedList<Field> fs = new LinkedList<Field>();
        for (Field f : Arrays.asList(pType.getDeclaredFields())) {
            fs.add(f);
        }
        if (pType.getSuperclass() != null) {
            fs.addAll(getAllFields(pType.getSuperclass()));
        }
        return fs;
    }

    public static LinkedList<Field> getAllSerializableFields(Class<?> pType) {
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

    public static <T> T createObject(Class<T> pClazz, Map<Field, java.lang.Object> pMap) throws IllegalAccessException, InstantiationException {
        T result = pClazz.newInstance();
        for (Field field : getAllFields(pClazz)) {
            boolean b = field.isAccessible();
            field.setAccessible(true);
            if (pMap.containsKey(field)) {
                field.set(result, pMap.get(field));
            } else {
                field.set(result, Values.defaultValueFor(field.getType()));
            }
            field.setAccessible(b);
        }
        return result;
    }

    public static <T> T changeObject(T pObject, Map<Field, java.lang.Object> pMap) throws IllegalAccessException {
        T result = pObject;
        for (Field field : getAllFields(result.getClass())) {
            boolean b = field.isAccessible();
            field.setAccessible(true);
            if (pMap.containsKey(field)) {
                field.set(result, pMap.get(field));
            }
            field.setAccessible(b);
        }
        return result;
    }

    public static <T extends Object> T fromMap(Class<T> pClass, Map<Field, java.lang.Object> pObject) throws IllegalAccessException, InstantiationException {
        T result = pClass.newInstance();
        for (Field f : result.getAllFields()) {
            f.set(result, pObject.get(f));
        }
        return result;
    }

    public static String toGenericString(Class<?> pClass, java.lang.Object pObject) {
        StringBuffer sb = new StringBuffer(pClass.getSimpleName() + "{");
        Field[] fields = pClass.getDeclaredFields();
        for (int index = 0; index < fields.length; index++) {
            if (!Modifier.isTransient(fields[index].getModifiers()) && (!Modifier.isStatic(fields[index].getModifiers()) && !Modifier.isFinal(fields[index].getModifiers())))
                try {
                    if (sb.toString().lastIndexOf("{") != sb.toString().length() - 1 && sb.toString().lastIndexOf(", ") != sb.toString().length() - 1) {
                        sb.append(", ");
                    }
                    final boolean b = fields[index].isAccessible();
                    fields[index].setAccessible(true);
                    sb.append(fields[index].getName() + "=");
                    if (String.class.isAssignableFrom(fields[index].getType())) {
                        sb.append("'" + fields[index].get(pObject) + "'");
                    } else
                        sb.append(fields[index].get(pObject));
                    fields[index].setAccessible(b);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
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

    public Map<Field, java.lang.Object> toMap() {
        Map<Field, java.lang.Object> result = new HashMap<Field, java.lang.Object>();
        for (Field f : this.getAllFields()) {
            try {
                result.put(f, f.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * @return
     */
    public final String getAddress() {
        return java.lang.Object.class.toString();
    }

    @Override
    public String toString() {
        return toGenericString(this.getClass(), this);
    }

    @Override
    public boolean equals(java.lang.Object pObject) {
        return (pObject != null && this.toString().equals(pObject.toString()));
    }

    @Override
    public int hashCode() {
        java.lang.Object[] result = new Object[this.getAllFields().size()];
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
