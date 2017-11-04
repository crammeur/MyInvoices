package ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Marc-Antoine on 2017-09-03.
 */

public class DateAdapter implements JsonDeserializer<Date>, JsonSerializer<Date> {
    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        SimpleDateFormat result = new SimpleDateFormat();
        try {
            return result.parse(json.getAsJsonPrimitive().getAsString());
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        JsonPrimitive result = new JsonPrimitive(src.toString());
        return result;
    }
}
