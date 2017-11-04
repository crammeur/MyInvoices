package ca.qc.bergeron.marcantoine.crammeur.myinvoices;

import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ca.qc.bergeron.marcantoine.crammeur.android.repository.crud.SQLiteDatabaseHelper;

/**
 * An activity that pins a file to the device. Pinning allows
 * a file's latest version to be available locally all the time.
 * Your users should be informed about the extra bandwidth
 * and storage requirements of pinning.
 */
public class LoadDriveActivity extends BaseDriveActivity {

    private static final int REQUEST_CODE_OPENER = NEXT_AVAILABLE_REQUEST_CODE;

    private static final String TAG = "PinFileActivity";

    private DriveId mFileId;

    private void load(DriveFile pFile) {
        pFile.open(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, new DriveFile.DownloadProgressListener() {
            @Override
            public void onProgress(long l, long l1) {
                Toast.makeText(LoadDriveActivity.this, String.valueOf(l) + " / " + String.valueOf(l1),Toast.LENGTH_SHORT).show();
            }
        }).setResultCallback(loadCallback);
    }

    /**
     * Starts a file opener intent to pick a file.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        super.onConnected(connectionHint);
        if (mFileId == null) {
            IntentSender intentSender = Drive.DriveApi
                    .newOpenFileActivityBuilder()
                    .setMimeType(new String[] {"application/x-sqlite3"})
                    .build(getGoogleApiClient());
            try {
                startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER,
                        null, 0, 0, 0);
            } catch (SendIntentException e) {
                Log.w(TAG, "Unable to send intent", e);
            }
        } else {
            DriveFile file = mFileId.asDriveFile();
            load(file);
        }
    }

    /**
     * Handles response from the file picker.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPENER:
                if (resultCode == RESULT_OK) {
                    mFileId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                } else {
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    final ResultCallback<DriveApi.DriveContentsResult> loadCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
        @Override
        public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
            if (!driveContentsResult.getStatus().isSuccess()) {
                Log.i(TAG, "Failed to create new contents.");
                return;
            }
            // Otherwise, we can write our data to the new contents.
            Log.i(TAG, "New contents created.");

            DriveContents contents = driveContentsResult.getDriveContents();
            BufferedInputStream reader = new BufferedInputStream(contents.getInputStream());
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            try {
                while ((len = reader.read(buffer)) >= 0) {
                    byteBuffer.write(buffer,0,len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            File database = getDatabasePath(SQLiteDatabaseHelper.DATABASE_NAME);
            try {
                OutputStream os = new FileOutputStream(database);
                os.write(byteBuffer.toByteArray());
                os.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
        }
    };
}

