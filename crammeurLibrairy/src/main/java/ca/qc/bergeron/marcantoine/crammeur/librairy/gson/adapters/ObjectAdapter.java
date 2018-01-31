package ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;

import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Values;

/**
 * Created by Marc-Antoine on 2017-09-02.
 */

public class ObjectAdapter implements JsonDeserializer<java.lang.Object>, InstanceCreator<java.lang.Object>, JsonSerializer<java.lang.Object> {
    @Override
    public Object createInstance(Type type) {
        Object result = null;
        Constructor c = null;
        final Class clazz;

        if (type instanceof ParameterizedType) {
            clazz = TypeToken.getParameterized(((ParameterizedType) type).getRawType(), ((ParameterizedType) type).getActualTypeArguments()).getRawType();
        } else {
            clazz = (Class) type;
        }
        for (Constructor construtor : clazz.getConstructors()) {
            if (c == null || c.getParameterTypes().length > construtor.getParameterTypes().length)
                c = construtor;
        }
        try {
            if (c != null && c.getParameterTypes().length > 0) {
                Object[] param = new Object[c.getParameterTypes().length];
                boolean b = c.isAccessible();
                c.setAccessible(true);
                for (int index = 0; index < c.getParameterTypes().length; index++) {
                    if (c.getParameterTypes()[index].isPrimitive()) {
                        if (long.class.isAssignableFrom(c.getParameterTypes()[index]) || int.class.isAssignableFrom(c.getParameterTypes()[index]) ||
                                short.class.isAssignableFrom(c.getParameterTypes()[index]) || byte.class.isAssignableFrom(c.getParameterTypes()[index]) ||
                                float.class.isAssignableFrom(c.getParameterTypes()[index]) || double.class.isAssignableFrom(c.getParameterTypes()[index]))
                            param[index] = 0;
                        else if (char.class.isAssignableFrom(c.getParameterTypes()[index]))
                            param[index] = '\u0000';
                        else if (boolean.class.isAssignableFrom(c.getParameterTypes()[index]))
                            param[index] = false;
                        else
                            throw new RuntimeException(c.getTypeParameters()[index].getName());
                    } else if (!Object.class.equals(c.getParameterTypes()[index])) {
                        param[index] = Values.defaultValueFor(c.getParameterTypes()[index]);
                    } else
                        param[index] = null;
                }
                result = c.newInstance(param);
                c.setAccessible(b);
            } else if (c != null) {
                result = c.newInstance();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public Object deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Object result;
        if (json.isJsonObject()) {
            result = this.createInstance(typeOfT);
            final JsonObject jsonO = json.getAsJsonObject();
            try {
                Class c = (Class) typeOfT;
                do {
                    for (final Field field : c.getDeclaredFields()) {
                        if (!Modifier.isTransient(field.getModifiers()) && (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()))) {
                            String name = field.getType().getName() + "." + field.getName();
                            if (!jsonO.has(name))
                                name = field.getName();
                            Class type;
                            if (field.getGenericType() instanceof ParameterizedType) {
                                type = TypeToken.getParameterized(((ParameterizedType) field.getGenericType()).getRawType(), ((ParameterizedType) field.getGenericType()).getActualTypeArguments()).getRawType();
                            } else {
                                type = TypeToken.get(field.getType()).getRawType();
                            }
                            final boolean b = field.isAccessible();
                            field.setAccessible(true);
                            JsonElement element = jsonO.get(name);
                            if (String.class.isAssignableFrom(type))
                                field.set(result, element.getAsString());
                            else if (element.isJsonArray())
                                field.set(result, context.deserialize(element.getAsJsonArray(), type));
                            else if (java.lang.Object.class.isAssignableFrom(type) && !element.isJsonPrimitive())
                                field.set(result, context.deserialize(element.getAsJsonObject(), type));
                            else if (Serializable.class.isAssignableFrom(type) && element.isJsonPrimitive())
                                field.set(result, context.deserialize(element, type));
                            else
                                field.set(result, context.deserialize(element, type));
                            field.setAccessible(b);
                        }
                    }
                } while ((c = c.getSuperclass()) != null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if (json.isJsonPrimitive()) {
            final JsonPrimitive jsonP = json.getAsJsonPrimitive();
            if (Long.class.isAssignableFrom((Class) typeOfT))
                result = jsonP.getAsLong();
            else if (Integer.class.isAssignableFrom((Class) typeOfT))
                result = jsonP.getAsInt();
            else if (Short.class.isAssignableFrom((Class) typeOfT))
                result = jsonP.getAsShort();
            else if (Byte.class.isAssignableFrom((Class) typeOfT))
                result = jsonP.getAsByte();
            else if (Double.class.isAssignableFrom((Class) typeOfT))
                result = jsonP.getAsDouble();
            else if (Float.class.isAssignableFrom((Class) typeOfT)) {
                result = jsonP.getAsFloat();
            } else if (Character.class.isAssignableFrom((Class) typeOfT)) {
                result = jsonP.getAsCharacter();
            } else if (Boolean.class.isAssignableFrom((Class) typeOfT))
                result = jsonP.getAsBoolean();
            else if (String.class.isAssignableFrom(((Class) typeOfT)))
                result = jsonP.getAsString();
            else
                throw new JsonParseException("Primitive is not supported");
        } else
            throw new JsonParseException("json is not a json Object");
        return result;
    }

    @Override
    public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonElement result;
        if (Long.class.isAssignableFrom((Class) typeOfSrc) || Integer.class.isAssignableFrom((Class) typeOfSrc) || Short.class.isAssignableFrom((Class) typeOfSrc) ||
                Byte.class.isAssignableFrom((Class) typeOfSrc) || Double.class.isAssignableFrom((Class) typeOfSrc) || Float.class.isAssignableFrom((Class) typeOfSrc)) {
            result = new JsonPrimitive((Number) src);
        } else if (Character.class.isAssignableFrom((Class) typeOfSrc)) {
            result = new JsonPrimitive((Character) src);
        } else if (Boolean.class.isAssignableFrom((Class) typeOfSrc))
            result = new JsonPrimitive((Boolean) src);
        else if (String.class.isAssignableFrom((Class) typeOfSrc))
            result = new JsonPrimitive((String) src);
        else if (Arrays.class.isAssignableFrom((Class) typeOfSrc) || ArrayList.class.isAssignableFrom((Class) typeOfSrc)) {
            result = new JsonArray();
            if (Arrays.class.isAssignableFrom((Class) typeOfSrc)) {
                for (Object object : Arrays.asList((Object[]) src)) {
                    if (Long.class.isAssignableFrom(object.getClass()) || Integer.class.isAssignableFrom(object.getClass()) || Short.class.isAssignableFrom(object.getClass()) ||
                            Byte.class.isAssignableFrom(object.getClass()) || Double.class.isAssignableFrom(object.getClass()) || Float.class.isAssignableFrom(object.getClass())) {
                        ((JsonArray) result).add((Number) object);
                    } else if (Character.class.isAssignableFrom(object.getClass())) {
                        ((JsonArray) result).add((Character) object);
                    } else if (Boolean.class.isAssignableFrom(object.getClass()))
                        ((JsonArray) result).add((Boolean) object);
                    else if (String.class.isAssignableFrom(object.getClass()))
                        ((JsonArray) result).add((String) object);
                    else {
                        TypeVariable type = ((Class<ArrayList>) typeOfSrc).getTypeParameters()[0];
                        ((JsonArray) result).add(context.serialize(object, type));
                    }

                }
            } else {
                ArrayList iterable = (ArrayList) src;
                for (Object object : iterable) {
                    Class c = object.getClass();
                    if (Long.class.isAssignableFrom(object.getClass()) || Integer.class.isAssignableFrom(object.getClass()) || Short.class.isAssignableFrom(object.getClass()) ||
                            Byte.class.isAssignableFrom(object.getClass()) || Double.class.isAssignableFrom(object.getClass()) || Float.class.isAssignableFrom(object.getClass())) {
                        ((JsonArray) result).add((Number) object);
                    } else if (Character.class.isAssignableFrom(object.getClass())) {
                        ((JsonArray) result).add((Character) object);
                    } else if (Boolean.class.isAssignableFrom(object.getClass()))
                        ((JsonArray) result).add((Boolean) object);
                    else if (String.class.isAssignableFrom(object.getClass()))
                        ((JsonArray) result).add((String) object);
                    else
                        ((JsonArray) result).add(context.serialize(object, object.getClass()));
                }
            }
        } else {
            result = new JsonObject();
            Class c = src.getClass();
            do {
                for (Field field : c.getDeclaredFields()) {
                    if (!Modifier.isTransient(field.getModifiers()) && (!Modifier.isFinal(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()))) {
                        final boolean b = field.isAccessible();
                        try {
                            field.setAccessible(true);
                            String name = field.getName();
                            if (((JsonObject) result).has(name))
                                name = field.getType().getName() + "." + field.getName();
                            final Class type;
                            if (field.getGenericType() instanceof ParameterizedType) {
                                type = TypeToken.getParameterized(((ParameterizedType) field.getGenericType()).getRawType(), ((ParameterizedType) field.getGenericType()).getActualTypeArguments()).getRawType();
                            } else {
                                type = TypeToken.get(field.getType()).getRawType();
                            }
                            if (type.isPrimitive()) {
                                if (Number.class.isAssignableFrom(type))
                                    ((JsonObject) result).addProperty(name, (Number) field.get(src));
                                else if (Character.class.isAssignableFrom(type))
                                    ((JsonObject) result).addProperty(name, (Character) field.get(src));
                                else if (Boolean.class.isAssignableFrom(type))
                                    ((JsonObject) result).addProperty(name, (Boolean) field.get(src));
                                else
                                    ((JsonObject) result).addProperty(name, (field.get(src) != null) ? String.valueOf(field.get(src)) : null);
                            } else if (String.class.isAssignableFrom(type))
                                ((JsonObject) result).addProperty(name, (field.get(src) != null) ? String.valueOf(field.get(src)) : null);
                            else if (Number.class.isAssignableFrom(type))
                                ((JsonObject) result).addProperty(name, (Number) field.get(src));
                            else if (Character.class.isAssignableFrom(type))
                                ((JsonObject) result).addProperty(name, (Character) field.get(src));
                            else if (Boolean.class.isAssignableFrom(type))
                                ((JsonObject) result).addProperty(name, (Boolean) field.get(src));
                            else
                                ((JsonObject) result).add(name, context.serialize(field.get(src), type));

                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        } finally {
                            field.setAccessible(b);
                        }
                    }
                }
            } while ((c = c.getSuperclass()) != null);
        }
        return result;
    }
}
