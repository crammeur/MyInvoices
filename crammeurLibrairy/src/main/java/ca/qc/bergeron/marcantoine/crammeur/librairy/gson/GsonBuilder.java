package ca.qc.bergeron.marcantoine.crammeur.librairy.gson;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.base.BaseDateTime;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters.BaseDateTimeAdapter;
import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters.DataAdapter;
import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters.DataArrayAdapter;
import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters.DataCollectionAdapter;
import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters.DataMapAdapter;
import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters.DateAdapter;
import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters.ObjectAdapter;
import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.adapters.ObjectArrayAdapter;
import ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

//import java.lang.Object;

/**
 * Created by Marc-Antoine on 2017-09-11.
 */

public class GsonBuilder<T extends Data<K>, K extends Serializable> {

    public static final com.google.gson.GsonBuilder GsonBuilder = new com.google.gson.GsonBuilder()
            .registerTypeHierarchyAdapter(java.lang.Object.class, new ObjectAdapter())
            .registerTypeHierarchyAdapter(java.lang.Object[].class, new ObjectArrayAdapter())
            .registerTypeHierarchyAdapter(Date.class, new DateAdapter())
            .registerTypeHierarchyAdapter(BaseDateTime.class, new BaseDateTimeAdapter())
            .registerTypeHierarchyAdapter(Data.class, new DataAdapter())
            .registerTypeHierarchyAdapter(Data[].class, new DataArrayAdapter())
            .registerTypeAdapter(new TypeToken<Collection<Data<Serializable>>>() {}.getType(), new DataCollectionAdapter())
            .registerTypeAdapter(new TypeToken<Map<Serializable, Data<Serializable>>>() {}.getType(), new DataMapAdapter());


    private final com.google.gson.GsonBuilder mGsonBuilder;

    public GsonBuilder(Class<T> pClass) {
        com.google.gson.GsonBuilder builder = GsonBuilder.registerTypeHierarchyAdapter(pClass, new DataAdapter<T, K>());
        for (final Field field : Object.getAllSerializableFields(pClass)) {
            final Type type;
            if (field.getGenericType() instanceof ParameterizedType) {
                type = TypeToken.getParameterized(((ParameterizedType) field.getGenericType()).getRawType(), ((ParameterizedType) field.getGenericType()).getActualTypeArguments()).getType();
            } else {
                type = TypeToken.get(field.getType()).getType();
            }
            if (type instanceof ParameterizedType) {
                if (Collection.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType()) && Data.class.isAssignableFrom((Class) ((ParameterizedType) type).getActualTypeArguments()[0])) {
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getRawType(), new DataCollectionAdapter());
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getActualTypeArguments()[0], new DataAdapter());
                } else if (Map.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType()) && Data.class.isAssignableFrom((Class) ((ParameterizedType) type).getActualTypeArguments()[1])) {
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getRawType(), new DataMapAdapter());
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getActualTypeArguments()[1], new DataAdapter());
                } else if (Arrays.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType()) && Data.class.isAssignableFrom((Class) ((ParameterizedType) type).getActualTypeArguments()[0])) {
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getRawType(), new DataArrayAdapter());
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getActualTypeArguments()[0], new DataAdapter());
                }
            } else if (type instanceof Class && Data.class.isAssignableFrom((Class) type)) {
                builder = builder.registerTypeAdapter(type, new DataAdapter());
            }
        }
        mGsonBuilder = builder;
    }

    public GsonBuilder(Class<T> pClass, Repository pRepository) {
        com.google.gson.GsonBuilder builder = GsonBuilder.registerTypeHierarchyAdapter(pClass, new DataAdapter<T, K>(pRepository));
        for (final Field field : Object.getAllSerializableFields(pClass)) {
            final Type type;
            if (field.getGenericType() instanceof ParameterizedType) {
                type = TypeToken.getParameterized(((ParameterizedType) field.getGenericType()).getRawType(), ((ParameterizedType) field.getGenericType()).getActualTypeArguments()).getType();
            } else {
                type = TypeToken.get(field.getType()).getType();
            }
            if (type instanceof ParameterizedType) {
                if (Collection.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType()) && Data.class.isAssignableFrom((Class) ((ParameterizedType) type).getActualTypeArguments()[0])) {
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getRawType(), new DataCollectionAdapter());
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getActualTypeArguments()[0], new DataAdapter(pRepository));
                } else if (Map.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType()) && Data.class.isAssignableFrom((Class) ((ParameterizedType) type).getActualTypeArguments()[1])) {
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getRawType(), new DataMapAdapter());
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getActualTypeArguments()[1], new DataAdapter(pRepository));
                } else if (Arrays.class.isAssignableFrom((Class) ((ParameterizedType) type).getRawType()) && Data.class.isAssignableFrom((Class) ((ParameterizedType) type).getActualTypeArguments()[0])) {
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getRawType(), new DataArrayAdapter());
                    builder = builder.registerTypeAdapter(((ParameterizedType) type).getActualTypeArguments()[0], new DataAdapter(pRepository));
                }
            } else if (type instanceof Class && Data.class.isAssignableFrom((Class) type)) {
                builder = builder.registerTypeAdapter(type, new DataAdapter(pRepository));
            }
        }

        mGsonBuilder = builder;
    }

    public Gson getGson() {
        return mGsonBuilder.create();
    }
}
