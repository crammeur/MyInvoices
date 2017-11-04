package ca.qc.bergeron.marcantoine.crammeur.android.utils;

import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by Marc-Antoine on 2017-03-15.
 */

public abstract class Android {

    public static void addImageToGallery(final File pFile, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.DATA, pFile.getAbsolutePath());

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
