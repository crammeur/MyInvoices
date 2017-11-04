package ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Generic;

/**
 * Created by Marc-Antoine on 2017-09-17.
 */

public class ObjectArrayAdapter implements JsonDeserializer<Object[]>, JsonSerializer<Object[]> {

    @Override
    public Object[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final Class clazz;
        if (typeOfT instanceof ParameterizedType) {
            clazz = TypeToken.getParameterized(((ParameterizedType) typeOfT).getRawType(), ((ParameterizedType) typeOfT).getActualTypeArguments()).getRawType();
        } else {
            clazz = TypeToken.get(typeOfT).getRawType();
        }
        if (json.isJsonArray() && Array.class.isAssignableFrom(clazz)) {
            final JsonArray jsonA = json.getAsJsonArray();
            final Type type = Generic.getActualClass(TypeToken.get(((ParameterizedType) typeOfT).getActualTypeArguments()[0]).getType(), Object.class);
            final Object[] result = (Object[]) Array.newInstance((Class<Object>) type, jsonA.size());
            for (int index = 0; index < jsonA.size(); index++) {
                result[index] = context.deserialize(jsonA.get(index), type);
            }
            return result;
        } else
            throw new JsonParseException("Is not a json array");
    }

    @Override
    public JsonElement serialize(Object[] src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonArray result = new JsonArray(src.length);
        for (final Object data : src) {
            result.add(context.serialize(data, TypeToken.get(data.getClass()).getRawType()));
        }
        return result;
    }
}
