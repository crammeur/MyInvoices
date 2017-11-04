package ca.qc.bergeron.marcantoine.crammeur.android.service.entity;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Company;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.DataFramework;
import ca.qc.bergeron.marcantoine.crammeur.android.service.EntityFramework;

/**
 * Created by Marc-Antoine on 2017-01-09.
 */

public final class EntityCompany extends EntityFramework<Company,Integer> {

    public EntityCompany(DataFramework<Company, Integer> pDataFramework) {
        super(pDataFramework);
    }

    @Override
    public Integer save(Company pEntity) throws KeyException {
        if (pEntity.Name.equals("")) throw new RuntimeException("Name is empty");
        if (pEntity.EMail.equals("")) throw new RuntimeException("EMail is empty");
        if (pEntity.Id == null) {
            pEntity.Id = this.getKey(pEntity);
        }
        return mDataFramework.save(pEntity);
    }
}
