package ca.qc.bergeron.marcantoine.crammeur.android.service.entity;


import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Client;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.DataFramework;
import ca.qc.bergeron.marcantoine.crammeur.android.service.EntityFramework;

/**
 * Created by Marc-Antoine on 2017-03-31.
 */

public final class EntityClient extends EntityFramework<Client,Integer> {

    public EntityClient(DataFramework<Client, Integer> pDataFramework) {
        super(pDataFramework);
    }

    @Override
    public Integer save(Client pEntity) throws KeyException {
        //if (pEntity.Name.equals("")) throw new RuntimeException("Name is empty");
        if (pEntity.EMail.equals("")) throw new RuntimeException("EMail is empty");
        if (pEntity.Id == null) {
            pEntity.Id = this.getKey(pEntity);
        }
        return mDataFramework.save(pEntity);
    }
}
