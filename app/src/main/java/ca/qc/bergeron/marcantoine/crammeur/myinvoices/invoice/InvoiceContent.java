package ca.qc.bergeron.marcantoine.crammeur.myinvoices.invoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.qc.bergeron.marcantoine.crammeur.librairy.lang.Object;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice;

/**
 * Created by Marc-Antoine on 2017-04-01.
 */

public class InvoiceContent {
    /**
     * An array of sample (invoice) items.
     */
    public static final List<InvoiceContent.InvoiceItem> ITEMS = new ArrayList<>();

    /**
     * A map of sample (invoice) items, by ID.
     */
    public static final Map<String, InvoiceContent.InvoiceItem> ITEM_MAP = new HashMap<>();

    public static void addItem(final InvoiceContent.InvoiceItem item) {
        ITEMS.add(0,item);
        ITEM_MAP.put(item.Id, item);
    }

    public static void removeItem(String pId) {
        ITEMS.remove(ITEM_MAP.get(pId));
        ITEM_MAP.remove(pId);
    }

    public static void clear() {
        ITEM_MAP.clear();
        ITEMS.clear();
    }

    /**
     * A invoice item representing a piece of content.
     */
    public static class InvoiceItem extends Object {
        public String Id;
        public Invoice Invoice;

        public InvoiceItem(Invoice pInvoice) {
            this.Id = (pInvoice.Id != null)?String.valueOf(pInvoice.Id):null;
            this.Invoice = pInvoice;
        }

    }
}
