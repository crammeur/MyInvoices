package ca.qc.bergeron.marcantoine.crammeur.myinvoices;

import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.SQLiteDatabaseHelper;

/**
 * An activity that pins a file to the device. Pinning allows
 * a file's latest version to be available locally all the time.
 * Your users should be informed about the extra bandwidth
 * and storage requirements of pinning.
 */
public class SaveDriveActivity extends BaseDriveActivity {

    private static final int REQUEST_CODE_SAVE = NEXT_AVAILABLE_REQUEST_CODE;

    private static final String TAG = "PinFileActivity";

    private byte[] file2Bytes(@NonNull File pFile) {
        byte[] buffer = new byte[4096];
        int len;
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(pFile));
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            while ((len = buf.read(buffer)) >= 0) {
                byteBuffer.write(buffer,0,len);
            }
            return byteBuffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts a file opener intent to pick a file.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(saveCallback);
    }

    /**
     * Handles response from the file picker.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SAVE:
                finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    final ResultCallback<DriveApi.DriveContentsResult> saveCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        // Handle error
                        return;
                    }
                    OutputStream outputStream = result.getDriveContents().getOutputStream();
                    try {
                        outputStream.write(file2Bytes(getDatabasePath(SQLiteDatabaseHelper.DATABASE_NAME)));
                    } catch (IOException e1) {
                        Log.i(TAG, "Unable to write file contents.");
                        throw new RuntimeException(e1);
                    }


                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                            .setPinned(true)
                            .setMimeType("application/x-sqlite3")
                            .setTitle("database.db")
                            .build();


                    IntentSender intentSender = Drive.DriveApi
                            .newCreateFileActivityBuilder()
                            .setInitialMetadata(metadataChangeSet)
                            .setInitialDriveContents(result.getDriveContents())
                            .build(getGoogleApiClient());
                    try {
                        startIntentSenderForResult(intentSender, REQUEST_CODE_SAVE, null, 0, 0, 0);
                    } catch (SendIntentException e) {
                        // Handle the exception
                    }
                }
            };
}
