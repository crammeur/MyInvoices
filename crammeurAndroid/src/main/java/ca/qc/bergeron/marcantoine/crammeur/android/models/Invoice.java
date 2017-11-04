package ca.qc.bergeron.marcantoine.crammeur.android.models;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;

import ca.qc.bergeron.marcantoine.crammeur.android.models.data.ShopProduct;
import ca.qc.bergeron.marcantoine.crammeur.librairy.annotations.Entity;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;

import static ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.sqlite.i.SQLite.F_ID;

/**
 * Created by Marc-Antoine on 2017-01-05.
 */
public final class Invoice extends Data<Integer> {
    @Entity.Id(name = F_ID)
    @Nullable
    public Integer Id;
    @NonNull
    public DateTime Date = new DateTime();
    @NonNull
    public String Details = "";
    @NonNull
    public Company Company = new Company();
    @NonNull
    public Client Client = new Client();
    //GST
    public double TPS;
    //PST
    public double TVP;
    //HST
    public double TVH;
    public double Total;

    @NonNull
    public volatile ArrayList<ShopProduct> Products = new ArrayList<>();

    @Nullable
    @Override
    public final Integer getId() {
        return Id;
    }

    @Override
    public void setId(Integer pId) {
        Id = pId;
    }

    public final double getPaidSubtotal() {
        return BigDecimal.valueOf(Total)
                .add(BigDecimal.valueOf(TPS).negate())
                .add(BigDecimal.valueOf(TVP).negate())
                .add(BigDecimal.valueOf(TVH).negate())
                .doubleValue();
    }

    public final double getCalculatedSubtotal() {
        BigDecimal result = new BigDecimal(0);
        for (ShopProduct sp : Products) {
            result = result.add(BigDecimal.valueOf(sp.Subtotal));
        }
        return result.doubleValue();
    }
}
