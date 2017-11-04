package ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.base.BaseDateTime;

import java.lang.reflect.Type;

/**
 * Created by Marc-Antoine on 2017-09-03.
 */

public class BaseDateTimeAdapter implements JsonDeserializer<BaseDateTime>, JsonSerializer<BaseDateTime> {

    @Override
    public BaseDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        BaseDateTime result = null;
        if (DateTime.class.isAssignableFrom((Class) typeOfT))
            result = DateTime.parse(json.getAsJsonPrimitive().getAsString());
        else if (MutableDateTime.class.isAssignableFrom((Class) typeOfT))
            result = MutableDateTime.parse(json.getAsJsonPrimitive().getAsString());
        return result;
    }

    @Override
    public JsonElement serialize(BaseDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        JsonPrimitive result = new JsonPrimitive(src.toString());
        return result;
    }
}
