package ca.qc.bergeron.marcantoine.crammeur.android.service;

import android.content.Context;

import ca.qc.bergeron.marcantoine.crammeur.android.models.Client;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Company;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Product;
import ca.qc.bergeron.marcantoine.crammeur.android.repository.Repository;
import ca.qc.bergeron.marcantoine.crammeur.android.service.entity.EntityCompany;
import ca.qc.bergeron.marcantoine.crammeur.android.service.entity.EntityProduct;

/**
 * Created by Marc-Antoine on 2017-01-09.
 */

public class Service{

    public final EntityFramework<Company, Integer> Companys;
    public final EntityFramework<Client, Integer> Clients;
    public final EntityFramework<Product, Integer> Products;
    public final EntityFramework<Invoice, Integer> Invoices;
    protected final Repository mRepository;

    public Service(Context pContext) {
        mRepository = new ca.qc.bergeron.marcantoine.crammeur.android.repository.Repository(pContext);
        Companys = new EntityCompany(mRepository.Companys);
        Clients = new EntityFramework<>(mRepository.Clients);
        Products = new EntityProduct(mRepository.Products);
        Invoices = new EntityFramework<>(mRepository.Invoices);
    }

    public void clear() {
        mRepository.clear();
    }
}
