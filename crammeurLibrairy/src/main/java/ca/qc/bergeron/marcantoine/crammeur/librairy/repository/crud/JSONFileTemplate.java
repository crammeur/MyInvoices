package ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.Repository;

/**
 * Created by Marc-Antoine on 2017-06-25.
 */

public final class JSONFileTemplate<T extends Data<Integer>> extends FileTemplate<T> {

    public JSONFileTemplate(Class<T> pClass, Repository pRepository, File pBase, boolean pDeleteCascade, boolean pUpdateCascade, boolean pForeignKey) {
        super(pClass, pRepository, pBase, pDeleteCascade, pUpdateCascade, pForeignKey);
    }

    @Override
    protected void loadFile() {
        try {
            if (mFile.exists()) {
                FileReader fr = new FileReader(mFile);
                BufferedReader br = new BufferedReader(fr);
                mTableKeyData.get(mTableName).clear();
                String line;
                while ((line = br.readLine()) != null) {
                    Data<Integer> data = Data.fromJSON(mClazz, line);
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
    protected void updateFile() {
        try {
            if (mFile.exists() || mFile.createNewFile()) {
                FileWriter fw = new FileWriter(mFile);
                BufferedWriter bw = new BufferedWriter(fw);
                for (Integer key : mTableKeyData.get(mTableName).keySet()) {
                    bw.write(mTableKeyData.get(mTableName).get(key).toJSON() + "\n");
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
