package ca.qc.bergeron.marcantoine.crammeur.android.utils.itext.pdf;

import android.content.res.Resources;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import ca.qc.bergeron.marcantoine.crammeur.R;
import ca.qc.bergeron.marcantoine.crammeur.android.models.data.ShopProduct;

/**
 * Created by Marc-Antoine on 2017-01-11.
 */

public abstract class Invoice {

    static Font font18b = new Font(Font.FontFamily.TIMES_ROMAN,18,Font.BOLD);
    static Font font14b = new Font(Font.FontFamily.TIMES_ROMAN,14,Font.BOLD);
    static Font font14 = new Font(Font.FontFamily.TIMES_ROMAN,14,Font.NORMAL);

    public static class HeaderFooter extends PdfPageEventHelper {

        protected Phrase header;

        public HeaderFooter(ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice pInvoice) {
            DateTime dt = new DateTime(pInvoice.Date);
            header = new Phrase("Invoice/Facture#:" + pInvoice.Id + " - " +
                    dt.toString().split("T")[0] + " " + dt.toString().split("T")[1].split("[.]")[0]);
        }

        @Override
        public void onEndPage(PdfWriter pWriter, Document pDocument) {
            PdfContentByte cb = pWriter.getDirectContent();
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    header,
                    (pDocument.right() - pDocument.left()) / 2 + pDocument.leftMargin(),
                    pDocument.top() + 10, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Page " + pDocument.getPageNumber()),
                    (pDocument.right() - pDocument.left()) / 2 + pDocument.leftMargin(),
                    pDocument.bottom() - 20, 0);
        }
    }

    public static void createPDF(Resources pResources, File pFile, ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice pInvoice, String pGST, String pPST, String pHST) throws IOException {
        if (!pFile.exists() && !pFile.createNewFile()) throw new FileNotFoundException(pFile.getName());
        if (pFile.isDirectory()) throw new IOException(pFile.getName() + " is directory");
        try {
            int columns = 4;
            if (pInvoice.TPS != 0)
                columns++;
            if (pInvoice.TVP != 0)
                columns++;
            if (pInvoice.TVH != 0)
                columns++;

            Document document = new Document(PageSize.LETTER);
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pFile));
            writer.setPageEvent(new Invoice.HeaderFooter(pInvoice));

            //MetaData
            document.addAuthor(pInvoice.Company.Name);
            document.addCreationDate();
            document.addCreator(pInvoice.Company.Name);
            document.addTitle("Invoice #:"+pInvoice.Id.toString());

            document.open();
            Paragraph p = new Paragraph(pResources.getString(R.string.invoice)+"#"+pInvoice.Id,font18b);
            p.setAlignment(Element.ALIGN_LEFT);
            document.add(p);
            p = new Paragraph(new DateTime(pInvoice.Date).toString().split("T")[0] + " " + new DateTime(pInvoice.Date).toString().split("T")[1].split("[.]")[0],font14);
            p.setAlignment(Element.ALIGN_LEFT);
            document.add(p);

            if (!pInvoice.Details.equals("")) {
                p = new Paragraph(pInvoice.Details);
                p.setAlignment(Element.ALIGN_LEFT);
                document.add(p);
            }

            //Seller and Buyer
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            PdfPCell seller = new PdfPCell();
            seller.setBorder(PdfPCell.NO_BORDER);
            seller.addElement(new Paragraph(pResources.getString(R.string.seller),font18b));
            seller.addElement(new Paragraph(pInvoice.Company.Name,font14));
            seller.addElement(new Paragraph(pInvoice.Company.EMail,font14));
            seller.addElement(new Paragraph(pInvoice.Company.Address, font14));
            seller.addElement(new Paragraph(pInvoice.Company.Phone, font14));
            table.addCell(seller);
            PdfPCell buyer = new PdfPCell();
            buyer.setBorder(PdfPCell.NO_BORDER);
            buyer.addElement(new Paragraph(pResources.getString(R.string.buyer),font18b));
            buyer.addElement(new Paragraph(pInvoice.Client.Name,font14));
            buyer.addElement(new Paragraph(pInvoice.Client.EMail,font14));
            table.addCell(buyer);
            document.add(table);

            //document.add(Chunk.NEWLINE);

            PdfPTable summary = new PdfPTable(3);
            summary.setWidthPercentage(100);
            summary.setWidths(new float[]{18.75f,31.25f,50});

            PdfPCell cell = new PdfPCell();
            cell.setBorder(PdfPCell.BOTTOM);
            p = new Paragraph(pResources.getString(R.string.summary),font18b);
            p.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p);
            summary.addCell(cell);
            cell = new PdfPCell();
            cell.setBorder(PdfPCell.BOTTOM);
            p = new Paragraph(pResources.getString(R.string.dollar_1),font18b);
            p.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(p);
            summary.addCell(cell);
            cell = new PdfPCell();
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.addElement(Chunk.NEWLINE);
            summary.addCell(cell);
            if (columns > 4) {
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.BOTTOM);
                p = new Paragraph(pResources.getString(R.string.subtotal),font14);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                summary.addCell(cell);
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.BOTTOM);
                p = new Paragraph(new DecimalFormat("0.00").format(pInvoice.getPaidSubtotal()),font14);
                p.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(p);
                summary.addCell(cell);
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.NO_BORDER);
                cell.addElement(Chunk.NEWLINE);
                summary.addCell(cell);
            }
            if (pInvoice.TPS != 0) {
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.BOTTOM);
                p = new Paragraph(pGST,font14);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                summary.addCell(cell);
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.BOTTOM);
                p = new Paragraph(new DecimalFormat("0.00").format(pInvoice.TPS),font14);
                p.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(p);
                summary.addCell(cell);
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.NO_BORDER);
                if (!pInvoice.Company.TPSCode.equals("")) {
                    p = new Paragraph(" " + pGST + "#: " + pInvoice.Company.TPSCode,font14);
                    p.setAlignment(PdfPCell.ALIGN_LEFT);
                    cell.addElement(p);
                } else
                    cell.addElement(Chunk.NEWLINE);
                summary.addCell(cell);
            }
            if (pInvoice.TVP != 0) {
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.BOTTOM);
                p = new Paragraph(pPST,font14);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                summary.addCell(cell);
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.BOTTOM);
                p = new Paragraph(new DecimalFormat("0.00").format(pInvoice.TVP),font14);
                p.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(p);
                summary.addCell(cell);
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.NO_BORDER);
                if (!pInvoice.Company.TPSCode.equals("")) {
                    p = new Paragraph(" " + pPST + "#: " + pInvoice.Company.TVPCode,font14);
                    p.setAlignment(PdfPCell.ALIGN_LEFT);
                    cell.addElement(p);
                } else
                    cell.addElement(Chunk.NEWLINE);
                summary.addCell(cell);
            }
            if (pInvoice.TVH != 0) {
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.BOTTOM);
                p = new Paragraph(pHST,font14);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                summary.addCell(cell);
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.BOTTOM);
                p = new Paragraph(new DecimalFormat("0.00").format(pInvoice.TVH),font14);
                p.setAlignment(Element.ALIGN_RIGHT);
                cell.addElement(p);
                summary.addCell(cell);
                cell = new PdfPCell();
                cell.setBorder(PdfPCell.NO_BORDER);
                if (!pInvoice.Company.TPSCode.equals("")) {
                    p = new Paragraph(" " + pHST + "#: " + pInvoice.Company.TVHCode,font14);
                    p.setAlignment(PdfPCell.ALIGN_LEFT);
                    cell.addElement(p);
                } else
                    cell.addElement(Chunk.NEWLINE);
                summary.addCell(cell);
            }
            cell = new PdfPCell();
            cell.setBorder(PdfPCell.BOTTOM);
            p = new Paragraph(pResources.getString(R.string.total),font14);
            p.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p);
            summary.addCell(cell);
            cell = new PdfPCell();
            cell.setBorder(PdfPCell.BOTTOM);
            p = new Paragraph(new DecimalFormat("0.00").format(pInvoice.Total),font14);
            p.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(p);
            summary.addCell(cell);
            cell = new PdfPCell();
            cell.setBorder(PdfPCell.NO_BORDER);
            cell.addElement(Chunk.NEWLINE);
            summary.addCell(cell);

            document.add(summary);

            document.add(Chunk.NEWLINE);

            //Products
            table = new PdfPTable(columns);
            table.setWidthPercentage(100);
            switch (columns) {
                case 4 :
                    table.setWidths(new float[]{6.8f,1,1,1.2f});
                    break;
                case 5 :
                    table.setWidths(new float[]{5.8f,1,1,1.2f,1});
                    break;
                case 6 :
                    table.setWidths(new float[]{4.8f,1,1,1.2f,1,1});
                    break;
                case 7 :
                    table.setWidths(new float[]{3.8f, 1, 1, 1.2f, 1, 1, 1});
                    break;
            }

            cell = new PdfPCell();
            p = new Paragraph(pResources.getString(R.string.product),font14b);
            p.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p);
            p = new Paragraph(pResources.getString(R.string.description),font14b);
            p.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p);
            table.addCell(cell);
            cell = new PdfPCell();
            p = new Paragraph(pResources.getString(R.string.price),font14b);
            p.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p);
            table.addCell(cell);
            cell = new PdfPCell();
            p = new Paragraph(pResources.getString(R.string.qty),font14b);
            p.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p);
            table.addCell(cell);
            cell = new PdfPCell();
            p = new Paragraph(pResources.getString(R.string.subtotal),font14b);
            p.setAlignment(Element.ALIGN_LEFT);
            cell.addElement(p);
            table.addCell(cell);
            if (pInvoice.TPS != 0) {
                cell = new PdfPCell();
                p = new Paragraph(pGST + " %",font14b);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                table.addCell(cell);
            }
            if (pInvoice.TVP != 0) {
                cell = new PdfPCell();
                p = new Paragraph(pPST + " %",font14b);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                table.addCell(cell);
            }
            if (pInvoice.TVH != 0) {
                cell = new PdfPCell();
                p = new Paragraph(pHST + " %",font14b);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                table.addCell(cell);
            }

            for (ShopProduct sp : pInvoice.Products) {
                cell = new PdfPCell();
                p = new Paragraph(sp.Product.Name,font14b);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                p = new Paragraph(sp.Product.Description,font14);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                table.addCell(cell);
                cell = new PdfPCell();
                p = new Paragraph(new DecimalFormat("#0.00##").format(sp.Product.Price),font14);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                table.addCell(cell);
                /*cell = new PdfPCell();
                p = new Paragraph(new DecimalFormat("#0.0###").format(sp.Product.Unit),font14);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                table.addCell(cell);*/
                cell = new PdfPCell();
                p = new Paragraph(new DecimalFormat("##0.0##").format(sp.Quantity),font14);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                table.addCell(cell);
                cell = new PdfPCell();
                p = new Paragraph(new DecimalFormat("####0.00#").format(sp.Subtotal),font14);
                p.setAlignment(Element.ALIGN_LEFT);
                cell.addElement(p);
                table.addCell(cell);
                if (pInvoice.TPS != 0) {
                    cell = new PdfPCell();
                    p = new Paragraph((sp.Product.TPS != 0)?new DecimalFormat("#0.00##").format(sp.Product.TPS):"",font14);
                    p.setAlignment(Element.ALIGN_LEFT);
                    cell.addElement(p);
                    table.addCell(cell);
                }
                if (pInvoice.TVP != 0) {
                    cell = new PdfPCell();
                    p = new Paragraph((sp.Product.TVP != 0)?new DecimalFormat("#0.00##").format(sp.Product.TVP):"",font14);
                    p.setAlignment(Element.ALIGN_LEFT);
                    cell.addElement(p);
                    table.addCell(cell);
                }
                if (pInvoice.TVH != 0) {
                    cell = new PdfPCell();
                    p = new Paragraph((sp.Product.TVH != 0)?new DecimalFormat("#0.00##").format(sp.Product.TVH):"",font14);
                    p.setAlignment(Element.ALIGN_LEFT);
                    cell.addElement(p);
                    table.addCell(cell);
                }
            }

            document.add(table);

            //document.add(Chunk.NEWLINE);

            document.close();

            /*FileOutputStream fos = new FileOutputStream(pFile);
            fos.write(baos.toByteArray());
            fos.close();*/
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void createPDF(Resources pResources, File pFile, ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice pInvoice)
            throws IOException {
        createPDF(pResources,pFile,pInvoice,pResources.getString(R.string.gst),pResources.getString(R.string.pst),pResources.getString(R.string.hst));
    }

/*    public static void createPDF(Resources pResource, File pFile, ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice pInvoice) throws DocumentException {
*//*        Document document = new Document(PageSize.LETTER);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        PdfContentByte canvas = writer.getDirectContentUnder();
        Image image;
        image.setAbsolutePosition(0, 0);
        canvas.addImage(image);

        document.close();*//*
    }*/
}
