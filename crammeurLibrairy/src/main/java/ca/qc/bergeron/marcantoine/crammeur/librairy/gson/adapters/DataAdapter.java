package ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.ws.rs.DefaultValue;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Generic;

/**
 * Created by Marc-Antoine on 2017-06-25.
 */

public class DataAdapter<T extends Data<K>, K extends Serializable> implements JsonDeserializer<T>, InstanceCreator<T>, JsonSerializer<T> {

    private final Repository mRepository;

    public DataAdapter() {
        this(null);
    }

    public DataAdapter(Repository pRepository) {
        mRepository = pRepository;
    }

    @Override
    public final JsonElement serialize(@NotNull Data src, @NotNull Type typeOfSrc, @NotNull JsonSerializationContext context) {
        final JsonElement result = new JsonObject();
        for (final Field field : src.getAllSerializableFields()) {
            final boolean b = field.isAccessible();
            field.setAccessible(true);
            String name = field.getName();
            if (((JsonObject) result).has(name))
                name = field.getType().getName() + "." + field.getName();
            try {
                if (Data.class.isAssignableFrom(field.getType())) {
                    if (mRepository != null && mRepository.contains((Class<? extends Data>) field.getType())) {
                        final Data data = (Data) field.get(src);
                        final Class<? extends Serializable> c = data.getId().getClass();
                        if (String.class.isAssignableFrom(c))
                            ((JsonObject) result).add(name, new JsonPrimitive((data.getId() != null) ? String.valueOf(data.getId()) : null));
                        else if (Number.class.isAssignableFrom(c))
                            ((JsonObject) result).add(name, new JsonPrimitive((Number) data.getId()));
                        else
                            ((JsonObject) result).add(name, context.serialize(field.get(src), TypeToken.get(field.getGenericType()).getRawType()));
                    } else
                        ((JsonObject) result).add(name, context.serialize(field.get(src), TypeToken.get(field.getGenericType()).getRawType()));
                } else {
                    ((JsonObject) result).add(name, context.serialize(field.get(src), TypeToken.get(field.getGenericType()).getRawType()));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            field.setAccessible(b);
        }
        return result;
    }

    @Override
    public T deserialize(@NotNull JsonElement json, @NotNull Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
        final T result = this.createInstance(typeOfT);
        final JsonObject jsonO = json.getAsJsonObject();
        try {
            for (final Field field : result.getAllSerializableFields()) {
                String name = field.getType().getName() + "." + field.getName();
                if (!jsonO.has(name))
                    name = field.getName();
                final boolean b = field.isAccessible();
                field.setAccessible(true);
                final JsonElement element = jsonO.get(name);
                if (Data.class.isAssignableFrom(field.getType())) {
                    if (mRepository != null && mRepository.contains((Class<? extends Data>) field.getType()) && ParameterizedType.class.isInstance(field.getType().getGenericSuperclass())) {
                        final K id;
                        final Class<K> c = (Class<K>) ((ParameterizedType) field.getType().getGenericSuperclass()).getActualTypeArguments()[0];
                        if (String.class.isAssignableFrom(c)) {
                            id = (K) element.getAsString();
                        } else if (Number.class.isAssignableFrom(c)) {
                            if (Long.class.isAssignableFrom(c))
                                id = (K) (Long) element.getAsNumber().longValue();
                            else
                                id = (K) (Integer) element.getAsNumber().intValue();
                        } else {
                            id = context.deserialize(element.getAsJsonObject(), field.getType());
                        }
                        field.set(result, mRepository.getByKey((Class<T>) field.getType(), id));
                    } else
                        field.set(result, context.deserialize(element.getAsJsonObject(), TypeToken.get(field.getGenericType()).getType()));

                } else {
                    final TypeToken type;
                    if (field.getGenericType() instanceof ParameterizedType) {
                        type = TypeToken.getParameterized(((ParameterizedType) field.getGenericType()).getRawType(), ((ParameterizedType) field.getGenericType()).getActualTypeArguments());
                    } else {
                        type = TypeToken.get(field.getType());
                    }
                    field.set(result, context.deserialize(element, type.getType()));
                }
                field.setAccessible(b);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }

    @NotNull
    @Override
    public T createInstance(@NotNull Type type) {
        T result = null;
        Constructor c = null;
        final Class clazz;
        if (type instanceof ParameterizedType) {
            clazz = Generic.getActualClass(TypeToken.getParameterized(((ParameterizedType) type).getRawType(), ((ParameterizedType) type).getActualTypeArguments()).getRawType(), Data.class);
        } else {
            clazz = (Class) type;
        }
        for (Constructor construtor : clazz.getDeclaredConstructors()) {
            if (c == null || c.getParameterTypes().length > construtor.getParameterTypes().length)
                c = construtor;
        }
        try {
            if (c != null && c.getParameterTypes().length > 0) {
                Object[] param = new Object[c.getParameterTypes().length];
                boolean b = c.isAccessible();
                c.setAccessible(true);
                for (int index = 0; index < c.getParameterTypes().length; index++) {

                    if (c.getParameterTypes()[index].isAnnotationPresent(DefaultValue.class)) {
                        param[index] = Data.fromJSON(c.getParameterTypes()[index], ((DefaultValue) c.getParameterTypes()[index].getAnnotation(DefaultValue.class)).value());
                    } else if (c.getParameterTypes()[index].isPrimitive()) {
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

                    } else {
                        param[index] = null;
                    }
                }
                result = (T) c.newInstance(param);
                c.setAccessible(b);
            } else if (c != null) {
                result = (T) c.newInstance();
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

/*    @Override
    public void write(JsonWriter out, T value) throws IOException {
        out.beginObject();
        Map<String,Field> keyField = new HashMap<>();
        for (Field field : value.getAllSerializableFields()) {
            boolean b = field.isAccessible();
            field.setAccessible(true);
            String name = field.getName();
            if (keyField.keySet().contains(name))
                name = field.getType().getName() + "." + field.getName();
            keyField.put(name,field);
            try {
                if (Data.class.isAssignableFrom(field.getType()))
                    out.name(name).value(((Data)field.get(value)).toJSON());
                else if (Number.class.isAssignableFrom(field.getType()))
                    out.name(name).value((Number) field.get(value));
                else if (Boolean.class.isAssignableFrom(field.getType()))
                    out.name(name).value((Boolean) field.get(value));
                else
                    out.name(name).value((field.get(value) != null)?String.valueOf(field.get(value)):null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            field.setAccessible(b);
        }
        out.endObject();
    }

    @Override
    public T read(JsonReader in) throws IOException {
        T result;
        try {
            result = mClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }*/
}
