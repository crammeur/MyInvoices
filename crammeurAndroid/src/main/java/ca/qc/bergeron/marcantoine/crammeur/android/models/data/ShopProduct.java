package ca.qc.bergeron.marcantoine.crammeur.android.models.data;

import android.support.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.math.BigDecimal;

import ca.qc.bergeron.marcantoine.crammeur.librairy.models.Data;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Product;
import ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object;


/**
 * Created by Marc-Antoine on 2017-01-11.
 */

public final class ShopProduct extends Data<ShopProduct.Id> {

    @Nullable
    public ShopProduct.Id Id;

    @NonNull
    public ca.qc.bergeron.marcantoine.crammeur.android.models.Product Product = new Product();
    public double Quantity = 0;
    public double Subtotal = 0;
    public double TVH = 0;
    public double TPS = 0;
    public double TVP = 0;

    @Nullable
    @Override
    public final ShopProduct.Id getId() {
        return Id;
    }

    @Override
    public void setId(@Nullable ShopProduct.Id pId) {
        Id = pId;
    }

    public double getTotal() {
        return BigDecimal.valueOf(Subtotal).add(BigDecimal.valueOf(TVH)).add(BigDecimal.valueOf(TPS)).add(BigDecimal.valueOf(TVP)).doubleValue();
    }

    public static ShopProduct createFromProduct(Product pProduct, double pQuantity) {
        ShopProduct result = new ShopProduct();
        result.Product = pProduct;
        result.Quantity = pQuantity;
        BigDecimal subtotal = BigDecimal.valueOf(result.Product.Price).multiply(BigDecimal.valueOf(result.Quantity));
        BigDecimal tvh = subtotal.multiply(BigDecimal.valueOf(result.Product.TVH)).divide(BigDecimal.valueOf(100),BigDecimal.ROUND_HALF_UP);
        BigDecimal tps = subtotal.multiply(BigDecimal.valueOf(result.Product.TPS)).divide(BigDecimal.valueOf(100),BigDecimal.ROUND_HALF_UP);
        BigDecimal tvp = subtotal.multiply(BigDecimal.valueOf(result.Product.TVP)).divide(BigDecimal.valueOf(100),BigDecimal.ROUND_HALF_UP);
        result.Subtotal = subtotal.doubleValue();
        result.TVH = tvh.doubleValue();
        result.TPS = tps.doubleValue();
        result.TVP = tvp.doubleValue();
        return result;
    }

    public class Id implements Serializable {
        public Integer pId;
        public Integer iId;

        @Override
        public String toString() {
            return Object.toGenericString(this.getClass(),this);
        }
    }
}
