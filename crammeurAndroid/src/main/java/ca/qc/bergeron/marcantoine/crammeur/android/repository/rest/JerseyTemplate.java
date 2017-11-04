package ca.qc.bergeron.marcantoine.crammeur.android.repository.rest;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.DeleteException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.rest.REST;
import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.ListOfSomething;

/**
 * Created by Marc-Antoine on 2017-06-28.
 */
public final class JerseyTemplate<T extends Data<K>, K extends Number> extends REST<T,K> {

    private class PostTask extends AsyncTask<T,Void,Collection<K>> {
        @Override
        protected Collection<K> doInBackground(T... ts) {
            List<K> result = new ArrayList<>();
            Invocation.Builder invocationBuilder =  mWebTarget.path(mTableName)
                    .register(mClazz)
                    .request(MediaType.APPLICATION_JSON_TYPE);
            Response response;
            //final ClientConfig clientConfig = new ClientConfig().register(mClazz);
            //response = invocationBuilder.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(ts[0].toJSON(),MediaType.APPLICATION_JSON));
            for (T data : ts) {
                response = invocationBuilder.accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(data.toJSON()));
                if (response.getStatus() == Response.Status.CREATED.getStatusCode())
                    result.add(mGson.fromJson(response.readEntity(String.class),mClazz).getId());
                else
                    Log.e("Error : ",String.valueOf(response.getStatus()));
            }
            return result;
        }
    }

    private class PutTask extends AsyncTask<T[],Void,Collection<K>> {
        @Override
        protected Collection<K> doInBackground(T[]... collections) {
            List<K> result = new ArrayList<>();
            Invocation.Builder invocationBuilder =  mWebTarget.path(mTableName)
                    .register(mClazz)
                    .request(MediaType.APPLICATION_JSON);

            Response response;
            for (T[] table : collections) {
                response = invocationBuilder.put(Entity.entity(table, MediaType.APPLICATION_JSON));

                if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                    result.addAll(mGson.fromJson(response.readEntity(String.class),Collection.class));
                }
            }
            return result;
        }
    }

    private class GetTask extends AsyncTask<K,Void,T> {
        @Override
        protected T doInBackground(K... pParams) {
            Invocation.Builder invocationBuilder =  mWebTarget.path(mTableName).path(String.valueOf(pParams[0])).request(MediaType.APPLICATION_JSON_TYPE);
            T result = null;
            try {
                Response response = invocationBuilder.get(Response.class);

                result = mGson.fromJson(response.readEntity(String.class),mClazz);

                System.out.println(response.getStatus());

            } catch (Throwable t) {
                t.printStackTrace();
                Log.e(t.getCause().toString(),t.getMessage());
            }
            return result;
        }
    }

    private class GetAllTask extends AsyncTask<Void,Void,Collection<T>> {
        @Override
        protected Collection<T> doInBackground(Void... pParams) {
            Collection<T> result = new ArrayList<>();
            Invocation.Builder invocationBuilder =  mWebTarget.path(mTableName).request(MediaType.APPLICATION_JSON_TYPE);
            Response response = invocationBuilder.get(Response.class);

            if (response.getStatus() == Response.Status.OK.getStatusCode())
                result = mGson.fromJson(response.readEntity(String.class), new ListOfSomething<>(mClazz));
            else if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode())
                throw new RuntimeException(response.getStatusInfo().getStatusCode() + " : " + response.getStatusInfo().getReasonPhrase());

            return result;
        }
    }

    private class DeleteTask extends AsyncTask<K,Void,Void> {
        @Override
        protected Void doInBackground(K... ks) {
            for (K key : ks) {
                Invocation.Builder invocationBuilder =  mWebTarget.path(mTableName)
                        .path(String.valueOf(key))
                        .register(mKey)
                        .request(MediaType.APPLICATION_JSON);
                Response response = invocationBuilder.delete();
                if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new RuntimeException(String.valueOf(response.getStatus()));
                }
            }
            return null;
        }
    }

    private class DeleteAllTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Invocation.Builder invocationBuilder =  mWebTarget.path(mTableName).request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.delete();
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new RuntimeException(String.valueOf(response.getStatus()));
            }
            return null;
        }
    }

    protected final Client  mClient = ClientBuilder.newClient();
    protected final WebTarget mWebTarget;
    protected final Gson mGson = new GsonBuilder().create();

    public JerseyTemplate(Class<T> pClass, Class<K> pKey, Repository pRepository, URL pUrl) {
        super(pClass, pKey, pRepository, pUrl.toString());
        try {
            mWebTarget = mClient.target(pUrl.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public K save(@NonNull T pData) throws KeyException {
        try {
            K[] array = (K[]) new Serializable[1];
            return new PostTask().execute((T[]) new Data[] { pData }).get().toArray(array)[0];
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @NonNull
    @Override
    public List<K> save(@NonNull T... pDatas) throws KeyException {
        List<K> result;
        try {
            result = (List<K>) new PutTask().execute((T[][]) new Data[][] { pDatas }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }


    @NonNull
    @Override
    public List<T> getAll() {
        List<T> result;
        try {
            result = (List<T>) new GetAllTask().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;
    }


    @NonNull
    @Override
    public List<T> getAll(@NonNull K pLimit, @NonNull K pOffset) {
        List<T> result = new ArrayList<>();
        long index = 0;
        for (Iterator<T> iterator = this.getAll().iterator();iterator.hasNext();index++) {
            if (pOffset.longValue() <= index && (pOffset.longValue() - index) <= pLimit.longValue()) {
                result.add(iterator.next());
            } else
                iterator.next();
        }
        return result;
    }


    @NonNull
    @Override
    public SortedSet<K> getAllKeys() {
        SortedSet<K> result = new TreeSet<>();
        for (T data : this.getAll()) {
            result.add(data.getId());
        }
        return result;
    }


    @Nullable
    @Override
    public T getByKey(@NonNull K pKey) {
        try {
            return new GetTask().execute((K[]) new Number[] { pKey }).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Nullable
    @Override
    public K getKey(@NonNull T pEntity) {
        if (pEntity.getId() != null) return pEntity.getId();

        K result = null;
        for (T data : this.getAll()) {
            K key = data.getId();
            data.setId(null);
            if (data.equals(pEntity)) {
                result = key;
            }
            if (result != null)
                break;
        }
        return result;
    }

    @NonNull
    @Override
    public List<T> getByKeys(@NonNull Set<K> pKeys) {
        try {
            return (List<T>) new GetTask().execute((K[]) pKeys.toArray()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean contains(@NonNull K pKey) {
        return this.getAllKeys().contains(pKey);
    }

    @Override
    public void delete(@NonNull K pKey) throws KeyException, DeleteException {
        new DeleteTask().execute((K[]) new Serializable[] { pKey });
    }

    @Override
    public void clear() {
        new DeleteAllTask().execute();
    }

    @Override
    public int count() {
        return this.getAll().size();
    }
}
