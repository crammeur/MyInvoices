package ca.qc.bergeron.marcantoine.crammeur.android.utils.csv;

import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

import ca.qc.bergeron.marcantoine.crammeur.android.models.data.ShopProduct;

/**
 * Created by Marc-Antoine on 2017-03-22.
 */

public abstract class Invoice {

    //28 Columns
    private static final String HEADER = "Invoice Id / Id Facture;" +
            "Product Id / Id Produit;" +
            "Date;" +
            "Client;" +
            "Product Name / Nom Produit;" +
            "Company Name / Nom Compagnie;" +
            "Company Address / Adresse Compagnie;" +
            "Company Phone / Téléphone Compagnie;" +
            "Company E-mail / Courriel Compagnie;" +
            "Description;" +
            "HST % / TVH %;" +
            "GST % / TPS %;" +
            "PST % / TVP %;" +
            "Price / Prix;" +
            //"Unit / Unité;" +
            "Quantity / Quantité;" +
            "Subtotal / Sous-total;" +
            "HST / TVH;" +
            "GST / TPS;" +
            "PST / TVP;" +
            "Total;" +
            "Subtotal / Sous-total;" +
            "HST / TVH;" +
            "GST / TPS;" +
            "PST / TVP;" +
            "HST Code / Code TVH;" +
            "GST Code / Code TPS;" +
            "PST Code / Code TVP;" +
            "Total\n";

    /**
     * Create CSV file of invoice
     * @param pFile
     * @param pInvoice
     * @return CSV String
     * @throws IOException
     */
    public static void createCSV(File pFile, ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice pInvoice) throws IOException {
        if (!pFile.exists() && !pFile.createNewFile()) throw new FileNotFoundException(pFile.getName());
        if (pFile.isDirectory()) throw new IOException(pFile.getName() + " is directory");
        String result = HEADER;
        DateTime dt = new DateTime(pInvoice.Date);
        result = result.concat(pInvoice.Id+";;" + dt.toString().split("T")[0] + " " + dt.toString().split("T")[1].split("[.]")[0] + ";" + pInvoice.Client.Name +";;" + pInvoice.Company.Name + ";" + pInvoice.Company.Address.replaceAll("\\n"," ") + ";" + pInvoice.Company.Phone + ";" + pInvoice.Company.EMail + ";" + pInvoice.Details + ";;;;;;;;;;;" + pInvoice.getPaidSubtotal() + ";" + pInvoice.TVH + ";" + pInvoice.TPS + ";" + pInvoice.TVP + ";" + pInvoice.Company.TVHCode + ";" + pInvoice.Company.TPSCode + ";" + pInvoice.Company.TVPCode + ";" + pInvoice.Total + "\n");
        for (ShopProduct p : pInvoice.Products) {
            result = result.concat(pInvoice.Id + ";");
            result = result.concat(p.Product.Id + ";");
            result = result.concat(dt.toString().split("T")[0] + " " + dt.toString().split("T")[1].split("[.]")[0] +";");
            result = result.concat(pInvoice.Client.Name + ";");
            result = result.concat(p.Product.Name + ";");
            result = result.concat(pInvoice.Company.Name + ";;;;");
            result = result.concat(p.Product.Description + ";");
            result = result.concat(String.valueOf(p.Product.TVH) + ";");
            result = result.concat(String.valueOf(p.Product.TPS) + ";");
            result = result.concat(String.valueOf(p.Product.TVP) + ";");
            result = result.concat(String.valueOf(p.Product.Price) + ";");
            //result = result.concat(String.valueOf(p.Product.Unit) + ";");
            result = result.concat(String.valueOf(p.Quantity) + ";");
            result = result.concat(String.valueOf(p.Subtotal) + ";");
            result = result.concat(String.valueOf(p.TVH) + ";");
            result = result.concat(String.valueOf(p.TPS) + ";");
            result = result.concat(String.valueOf(p.TVP) + ";");
            result = result.concat(String.valueOf(BigDecimal.valueOf(p.Subtotal)
                    .add(BigDecimal.valueOf(p.TVH))
                    .add(BigDecimal.valueOf(p.TPS))
                    .add(BigDecimal.valueOf(p.TVP))
                    .doubleValue()) + "\n");
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(pFile));
        bw.write(result);
        bw.close();
    }

    public static void createCSV(File pFile, Iterable<ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice> pInvoice) throws IOException {
        if (!pFile.exists() && !pFile.createNewFile()) throw new FileNotFoundException(pFile.getName());
        if (pFile.isDirectory()) throw new IOException(pFile.getName() + " is directory");
        String result = HEADER;
        for (ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice invoice : pInvoice) {
            DateTime dt = new DateTime(invoice.Date);
            result = result.concat(invoice.Id+";;" + dt.toString().split("T")[0] + " " + dt.toString().split("T")[1].split("[.]")[0] + ";" + invoice.Client.Name +";;" + invoice.Company.Name + ";" + invoice.Company.Address.replaceAll("\\n"," ") + ";" + invoice.Company.Phone + ";" + invoice.Company.EMail + ";" + invoice.Details + ";;;;;;;;;;;" + invoice.getPaidSubtotal() + ";" + invoice.TVH + ";" + invoice.TPS + ";" + invoice.TVP + ";" + invoice.Company.TVHCode + ";" + invoice.Company.TPSCode + ";" + invoice.Company.TVPCode + ";" + invoice.Total + "\n");
            for (ShopProduct p : invoice.Products) {
                result = result.concat(invoice.Id + ";");
                result = result.concat(p.Product.Id + ";");
                result = result.concat(dt.toString().split("T")[0] + " " + dt.toString().split("T")[1].split("[.]")[0] +";");
                result = result.concat(invoice.Client.Name + ";");
                result = result.concat(p.Product.Name + ";");
                result = result.concat(invoice.Company.Name + ";;;;");
                result = result.concat(p.Product.Description + ";");
                result = result.concat(String.valueOf(p.Product.TVH) + ";");
                result = result.concat(String.valueOf(p.Product.TPS) + ";");
                result = result.concat(String.valueOf(p.Product.TVP) + ";");
                result = result.concat(String.valueOf(p.Product.Price) + ";");
                //result = result.concat(String.valueOf(p.Product.Unit) + ";");
                result = result.concat(String.valueOf(p.Quantity) + ";");
                result = result.concat(String.valueOf(p.Subtotal) + ";");
                result = result.concat(String.valueOf(p.TVH) + ";");
                result = result.concat(String.valueOf(p.TPS) + ";");
                result = result.concat(String.valueOf(p.TVP) + ";");
                result = result.concat(String.valueOf(BigDecimal.valueOf(p.Subtotal)
                        .add(BigDecimal.valueOf(p.TVH))
                        .add(BigDecimal.valueOf(p.TPS))
                        .add(BigDecimal.valueOf(p.TVP))
                        .doubleValue()) + "\n");
            }
            //result = result.concat(invoice.Id+";;" + dt.toString().split("T")[0] + " " + dt.toString().split("T")[1].split("[.]")[0] + ";" + invoice.Client.Name +";;" + invoice.Company.Name + ";" + invoice.Company.Phone + ";" + invoice.Company.EMail + ";" + invoice.Details + ";;;;;;;;;;;" + invoice.Subtotal + ";" + invoice.TVH + ";" + invoice.TPS + ";" + invoice.TVP + ";" + invoice.Company.TVHCode + ";" + invoice.Company.TPSCode + ";" + invoice.Company.TVPCode + ";" + invoice.Total + "\n");
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(pFile));
        bw.write(result);
        bw.close();
    }

}
