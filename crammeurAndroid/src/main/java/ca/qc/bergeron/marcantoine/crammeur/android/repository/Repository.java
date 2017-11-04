package ca.qc.bergeron.marcantoine.crammeur.android.repository;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import ca.qc.bergeron.marcantoine.crammeur.android.models.Client;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Company;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Product;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.DeleteException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud.GSONFileTemplate;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.DataFramework;

/**
 * Created by Marc-Antoine on 2017-01-05.
 */

public class Repository implements ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository {

    //Only (DataFramework,CRUD,REST) interface support
    public DataFramework<Company,Integer> Companys;
    public DataFramework<Client,Integer> Clients;
    public DataFramework<Product,Integer> Products;
    public DataFramework<Invoice,Integer> Invoices;

    public Repository(Context pContext) {
        File folder = new File(pContext.getFilesDir(),"databases");
        if (!folder.mkdirs() && !folder.exists())  throw new RuntimeException("Folders not created");
/*        if (Companys == null) Companys = new JSONFilesTemplate<>(Company.class,Integer.class,this,folder, false,false,false);
        *//*if (Clients == null) Clients = new JSONFilesTemplate<>(Client.class,Integer.class,this,folder,false,false,false);*//*
        if (Products == null) Products = new JSONFilesTemplate<>(Product.class,Integer.class,this,folder,false,false,false);
        if (Invoices == null) Invoices = new JSONFilesTemplate<>(Invoice.class,Integer.class,this,folder,true,true,true);*/
/*        Companys = new SQLiteCompany(this, pContext);
        Clients = new SQLiteClient(this, pContext);
        Products = new SQLiteProduct(this, pContext);
        Invoices = new SQLiteInvoice(this, pContext);*/
        Companys = new GSONFileTemplate<>(Company.class,this,folder,false,false,false);
        Clients = new GSONFileTemplate<>(Client.class,this,folder,false,false,false);
        Products = new GSONFileTemplate<>(Product.class,this,folder,false,false,false);
        Invoices = new GSONFileTemplate<>(Invoice.class,this,folder,true,true,true);
/*        try {
            Clients = new JerseyTemplate<>(Client.class,Integer.class,this,new URL("http://crammeur.ddns.net:8081/JerseyWebService/rest"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }*/
    }

    /**
     *
     * @return
     */
    @NonNull
    protected <T extends Data<K>, K extends Serializable> List<DataFramework<T,K>> getAllDataFramework() {
        List<DataFramework<T,K>> result = new ArrayList<>();
        for (Field f : this.getClass().getDeclaredFields()) {
            if (DataFramework.class.isAssignableFrom(f.getType())) {
                final boolean b = f.isAccessible();
                f.setAccessible(true);
                try {
                    result.add(DataFramework.class.cast(f.get(this)));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                f.setAccessible(b);
            }
        }
        return result;
    }

    /**
     * @param pClass
     * @return DataFramework
     */
    @Nullable
    protected final <T extends Data<K>,K extends Serializable> DataFramework<T,K> getDataFramework(@NonNull Class<T> pClass) {
        DataFramework<T,K> result = null;
        for (Field f : this.getClass().getDeclaredFields()) {
            Class c = f.getType();
            do {
                if (DataFramework.class.isAssignableFrom(f.getType()) && ParameterizedType.class.isInstance(f.getGenericType()) &&
                        (((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]).equals(pClass)) {
                    final boolean b = f.isAccessible();
                    f.setAccessible(true);
                    try {
                        result = DataFramework.class.cast(f.get(this));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    f.setAccessible(b);
                }
            } while ((c = c.getSuperclass()) != null && result == null);
            if (result != null) break;
        }
        return result;
    }

    @NonNull
    @Override
    public final  <T extends Data<K>,K extends Serializable> K save(@NonNull T pData) throws KeyException {
       return this.save((Class<T>) pData.<T>getClass(), pData);
    }

    /**
     * @param pClass
     * @param pData
     * @return Key
     * @throws KeyException
     */
    private final <T extends Data<K>,K extends Serializable> K save(@NonNull Class<T> pClass, @NonNull T pData) throws KeyException {
        return (this.getDataFramework(pClass)).save(pData);
    }

    @NonNull
    @Override
    public final <T extends Data> Collection<T> getAll(@NonNull Class<T> pClass) {
        return (this.<T,Serializable>getDataFramework(pClass)).<T,Serializable>getAll();
    }

    @NonNull
    @Override
    public final <T extends Data<K>,K extends Serializable> Set<K> getAllKeys(@NonNull Class<T> pClass) {
        return (this.getDataFramework(pClass)).getAllKeys();
    }

    @Nullable
    @Override
    public final <T extends Data<K>,K extends Serializable> T getByKey(@NonNull Class<T> pClass, @NonNull K pKey) {
        return (this.getDataFramework(pClass)).getByKey(pKey);
    }

    @Nullable
    @Override
    public final <T extends Data<K>, K extends Serializable> K getKey(@NonNull Class<T> pClass,@NonNull T pData) {
        return (this.getDataFramework(pClass)).getKey(pData);
    }

    @NonNull
    @Override
    public final <T extends Data<K>,K extends Serializable> Collection<T> getByKeys(@NonNull Class<T> pClass, @NonNull Set<K> pKeys) {
        return (this.getDataFramework(pClass)).getByKeys(pKeys);
    }

    @Override
    public final <T extends Data> boolean contains(@NonNull Class<T> pClass) {
        return this.getDataFramework(pClass) != null;
    }

    @Override
    public final <T extends Data<K>, K extends Serializable> boolean contains(@NonNull Class<T> pClass, @NonNull K pKey) {
        return this.getDataFramework(pClass).contains(pKey);
    }

    @Override
    public final <T extends Data> boolean contains(@NonNull Class<T> pClass, @NonNull T pData) {
        if (pData.getId() != null)
            return this.contains(pClass,pData.getId());
        else
            return this.<T,Serializable>getDataFramework(pClass).<T,Serializable>getKey(pData) != null;
    }

    @Override
    public final <T extends Data> void clear(@NonNull Class<T> pClass) {
        this.getDataFramework(pClass).clear();
    }

    @Override
    public final <T extends Data<K>,K extends Serializable> void delete(@NonNull Class<T> pClass, @NonNull K pKey) throws KeyException, DeleteException {
        this.getDataFramework(pClass).delete(pKey);
    }

    @Override
    public final void clear() {
        List<DataFramework<Data<Serializable>,Serializable>> l = this.getAllDataFramework();
        for (int i=l.size()-1;i >= 0; i--) {
            l.get(i).clear();
        }
    }
}
