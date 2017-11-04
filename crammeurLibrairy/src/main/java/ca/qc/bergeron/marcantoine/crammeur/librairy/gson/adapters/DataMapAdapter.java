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
import java.util.Map;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Generic;

/**
 * Created by Marc-Antoine on 2017-09-10.
 */

public class DataMapAdapter<T extends Data<Integer>> implements JsonDeserializer<Map<Integer, T>>, JsonSerializer<Map<Integer, T>> {

    @Override
    public Map<Integer, T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonArray()) {
            JsonArray jsonA = json.getAsJsonArray();
            Map<Integer, T> result = (Map<Integer, T>) new ObjectAdapter().createInstance(typeOfT);
            Type type = TypeToken.get(Generic.getActualType(((ParameterizedType) typeOfT).getActualTypeArguments()[1], Data.class)).getType();
            for (int index = 0; index < jsonA.size(); index++) {
                T data = context.deserialize(jsonA.get(index), type);
                result.put(data.getId(), data);
            }
            return result;
        } else
            throw new JsonParseException("Is not a json array");
    }

    @Override
    public JsonElement serialize(Map<Integer, T> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray result = new JsonArray(src.size());
        for (T data : src.values()) {
            result.add(context.serialize(data));
        }
        return result;
    }
}
