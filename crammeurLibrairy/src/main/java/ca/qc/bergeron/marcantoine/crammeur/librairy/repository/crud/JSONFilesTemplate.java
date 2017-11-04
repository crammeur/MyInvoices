package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.DataFramework;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-06-09.
 */

public final class JSONFilesTemplate<T extends Data<K>, K extends Number> extends FilesTemplate<T, K> implements DataFramework<T, K> {

    public JSONFilesTemplate(Class<T> pClass, Class<K> pKey, Repository pRepository, File pBase, boolean pDeleteCascade, boolean pUpdateCascade, boolean pForeignKey) {
        super(pClass, pKey, pRepository, pBase, pDeleteCascade, pUpdateCascade, pForeignKey);
    }

    @Override
    protected final T readObjectFromFile(File pFile) {
        try {
            FileReader fis = new FileReader(pFile);
            BufferedReader ois = new BufferedReader(fis);
            T t = Data.fromJSON(mClazz, ois.readLine());
            ois.close();
            fis.close();
            mKeyData.get(mTableName).put(t.getId(), (Data<Number>) t);
            mSettings.keyFileName.put(t.getId(), pFile.getName());
            mSettings.keyLastModifications.put(t.getId(), pFile.lastModified());
            return t;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected final void writeObjectToFile(File pFile, T pData) {
        try {
            FileWriter fos = new FileWriter(pFile);
            BufferedWriter oos = new BufferedWriter(fos);
            oos.write(pData.toJSON());
            oos.close();
            fos.close();
            mSettings.keyLastModifications.put(pData.getId(), pFile.lastModified());
            mSettings.keyFileName.put(pData.getId(), pFile.getName());
            mKeyData.get(mTableName).put(pData.getId(), (Data<Number>) pData);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
