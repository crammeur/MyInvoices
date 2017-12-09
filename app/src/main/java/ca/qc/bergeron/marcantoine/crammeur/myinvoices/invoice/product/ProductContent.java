package ca.qc.bergeron.marcantoine.crammeur.myinvoices.invoice.product;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Product;
import ca.qc.bergeron.marcantoine.crammeur.android.models.data.ShopProduct;
import ca.qc.bergeron.marcantoine.crammeur.android.service.Service;
import ca.qc.bergeron.marcantoine.crammeur.librairy.models.i.Data;

/**
 * Created by Marc-Antoine on 2017-03-28.
 */

public class ProductContent {
    /**
     * An array of sample (product) items.
     */
    public static final List<ProductItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample (product) items, by ID.
     */
    public static final Map<String, ProductItem> ITEM_MAP = new HashMap<>();

    public static void addItem(ProductItem item) {
        ITEMS.add(item);
        if (item.Id != null)
            ITEM_MAP.put(item.Id, item);
    }

    public static void removeItem(@NonNull ProductItem pProductItem) {
        ITEMS.remove(pProductItem);
        if (pProductItem.Id != null)
            ITEM_MAP.remove(pProductItem.Id);
    }

    public static void removeItem(@NonNull String pId) {
        ITEMS.remove(ITEM_MAP.get(pId));
        ITEM_MAP.remove(pId);
    }

    public static boolean contains(@NonNull Product pProduct) {
        for (ProductItem pi : ITEMS) {
            Product p1 = ProductContent.ProductItem.convert(pi);
            if (pProduct.Id == null) {
                p1.Id = null;
                if (p1.equals((Data) pProduct)) return true;
            } else if (pProduct.Id.equals(p1.Id)) {
                return true;
            }
        }
        return false;
    }

    public static void clear() {
        ITEM_MAP.clear();
        ITEMS.clear();
    }

    public static void update(Service pService) {
        for (ProductItem pi : ITEMS) {
            if (pi.Id == null || !ITEM_MAP.keySet().contains(pi.Id)) {
                Product p = ProductItem.convert(pi);
                try {
                    p.Id = pService.Products.save(p);
                    ProductItem pi2 = new ProductItem(ShopProduct.createFromProduct(p,pi.Quantity));
                    ITEM_MAP.put(pi2.Id,pi2);
                } catch (KeyException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        ITEMS.clear();
        ITEMS.addAll(ITEM_MAP.values());
    }

    /**
     * A invoice item representing a piece of content.
     */
    public static class ProductItem extends Object {
        public String Id;
        public String Name;
        public String Description;
        public double Price;
        //public final double Unit;
        public double Quantity;
        public double TVA;
        public double TPS;
        public double TVQ;

        public ProductItem(ShopProduct pProduct) {
            this.Id = (pProduct.Product.Id != null)?String.valueOf(pProduct.Product.Id):null;
            this.Name = pProduct.Product.Name;
            this.Description = pProduct.Product.Description;
            this.Price = pProduct.Product.Price;
            //this.Unit = pProduct.Product.Unit;
            this.Quantity = pProduct.Quantity;
            this.TVA = pProduct.Product.TVH;
            this.TPS = pProduct.Product.TPS;
            this.TVQ = pProduct.Product.TVP;
        }

        public static Product convert(ProductItem pProductItem) {
            Product result = new Product();
            result.Id = (pProductItem.Id != null)?Integer.parseInt(pProductItem.Id):null;
            result.Name = pProductItem.Name;
            result.Description = pProductItem.Description;
            result.Price = pProductItem.Price;
            result.TPS = pProductItem.TPS;
            result.TVP = pProductItem.TVQ;
            result.TVH = pProductItem.TVA;
            return result;
        }
    }
}
