package ca.qc.bergeron.marcantoine.crammeur.android.service.entity;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Product;
import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.i.DataFramework;
import ca.qc.bergeron.marcantoine.crammeur.android.service.EntityFramework;

/**
 * Created by Marc-Antoine on 2017-03-31.
 */

public final class EntityProduct extends EntityFramework<Product,Integer> {

    public EntityProduct(DataFramework<Product, Integer> pDataFramework) {
        super(pDataFramework);
    }

    @Override
    public Integer save(Product pEntity) throws KeyException {
        if (pEntity.Name.equals("")) throw new RuntimeException("Name is empty");
        if (pEntity.Id == null) {
            pEntity.Id = this.getKey(pEntity);
        }
        return mDataFramework.save(pEntity);
    }
}
