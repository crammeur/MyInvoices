package ca.qc.bergeron.marcantoine.crammeur.librairy.itext;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Marc-Antoine on 2017-01-12.
 */

public class PDF {

    public void createPdf(String filename, PdfReader[] readers) throws IOException, DocumentException {
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, new FileOutputStream(filename));
        copy.setMergeFields();
        document.open();
        for (PdfReader reader : readers) {
            copy.addDocument(reader);
        }
        document.close();
        for (PdfReader reader : readers) {
            reader.close();
        }
    }
}
