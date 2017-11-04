package ca.qc.bergeron.marcantoine.crammeur.android.utils.zxing;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ca.qc.bergeron.marcantoine.crammeur.librairy.utils.Serialize;

/**
 * Created by Marc-Antoine on 2017-03-16.
 */

public abstract class QRCode {

    public static Bitmap generateBitmap(@NonNull String pData, int pSize) {
        try {
            BitMatrix bm = new MultiFormatWriter().encode(new String(Serialize.serialize(pData),"ISO-8859-1"), BarcodeFormat.QR_CODE, pSize, pSize);
            Bitmap result = QRCode.createBitmap(bm);
            for (int i = 0; i < pSize; i++) {
                for (int j = 0; j < pSize; j++) {
                    result.setPixel(i, j, bm.get(i, j) ? Color.BLACK: Color.WHITE);
                }
            }
            return result;
        } catch (WriterException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static BinaryBitmap bitmapToBBitmap(Bitmap pBitmap) {
        int[] intArray = new int[pBitmap.getWidth()*pBitmap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        pBitmap.getPixels(intArray, 0, pBitmap.getWidth(), 0, 0, pBitmap.getWidth(), pBitmap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(pBitmap.getWidth(), pBitmap.getHeight(),intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        return bitmap;
    }

    public static String convertBitmapToString(Bitmap pBitmap) {
        String result;
        try {
            Result content = new MultiFormatReader().decode(QRCode.bitmapToBBitmap(pBitmap));
            result = new String(content.getText().getBytes("ISO-8859-1"),"ISO-8859-1");
            //byte[] rawBytes = content.getRawBytes();
            //BarcodeFormat format = content.getBarcodeFormat();
            //ResultPoint[] points = content.getResultPoints();
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return result;

    }

    public static Bitmap createBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    public static Drawable createDrawable(Bitmap pBitmap) {
        return new BitmapDrawable(Resources.getSystem(),pBitmap);
    }
}
