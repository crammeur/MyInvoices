package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ca.qc.bergeron.marcantoine.crammeur.librairy.gson.GsonBuilder;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-09-01.
 */

public final class GSONFileTemplate<T extends Data<Integer>> extends FileTemplate<T> {

    protected final Gson mGson;

    public GSONFileTemplate(Class<T> pClass, Repository pRepository, File pBase, boolean pDeleteCascade, boolean pUpdateCascade, boolean pForeignKey) {
        super(pClass, pRepository, pBase, pDeleteCascade, pUpdateCascade, pForeignKey);
        mGson = new GsonBuilder<T, Integer>(pClass, pRepository).getGson();
    }

    @Override
    protected final void loadFile() {
        try {
            if (mFile.exists()) {
                FileReader fr = new FileReader(mFile);
                BufferedReader br = new BufferedReader(fr);
                //mTableKeyData.put(mTableName, (TreeMap<Integer, Data<Integer>>) mGson.fromJson(br.readLine(), new TypeToken<TreeMap<Integer,T>>(){}.getType()));
                mTableKeyData.get(mTableName).clear();
                String line;
                while ((line = br.readLine()) != null) {
                    final T data = mGson.fromJson(line, mClazz);
                    mTableKeyData.get(mTableName).put(data.getId(), data);
                }
                br.close();
                fr.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected final void updateFile() {
        try {
            if (mFile.exists() || mFile.createNewFile()) {
                FileWriter fw = new FileWriter(mFile);
                BufferedWriter bw = new BufferedWriter(fw);
                //bw.write(mGson.toJson(mTableKeyData.get(mTableName)));
                for (Integer key : mTableKeyData.get(mTableName).keySet()) {
                    bw.write(mGson.toJson(mTableKeyData.get(mTableName).get(key)) + "\n");
                }
                bw.close();
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
