package ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Generic;

/**
 * Created by Marc-Antoine on 2017-09-08.
 */

public class DataCollectionAdapter<T extends Data<Integer>> implements JsonDeserializer<Collection<T>>, JsonSerializer<Collection<T>> {

    @Override
    public Collection<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonArray()) {
            final JsonArray jsonA = json.getAsJsonArray();
            final Type type = TypeToken.get(Generic.getActualType(((ParameterizedType) typeOfT).getActualTypeArguments()[0], Data.class)).getType();
            final Collection<T> result = (Collection<T>) new ObjectAdapter().createInstance(typeOfT);
            for (int index = 0; index < jsonA.size(); index++) {
                try {
                    result.add(context.<T>deserialize(jsonA.get(index), type));
                } catch (Throwable t) {
                    t.printStackTrace();
                    throw new RuntimeException(t);
                }
            }
            return result;
        } else
            throw new JsonParseException("Is not a json array");
    }

    @Override
    public JsonElement serialize(Collection<T> src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonElement result = new JsonArray(src.size());
        for (final T data : src) {
            ((JsonArray) result).add(context.serialize(data, TypeToken.get(data.getClass()).getRawType()));
        }
        return result;
    }
}
