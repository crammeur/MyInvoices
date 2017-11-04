package ca.qc.bergeron.marcantoine.crammeur.myinvoices;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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

import ca.qc.bergeron.marcantoine.crammeur.android.service.Service;
import ca.qc.bergeron.marcantoine.crammeur.android.utils.csv.Invoice;

/**
 * Created by Marc-Antoine on 2017-07-23.
 */

public class CSVDriveActivity extends BaseDriveActivity {

    private static final int REQUEST_CODE_SAVE = NEXT_AVAILABLE_REQUEST_CODE;
    private static final String TAG = "PinFileActivity";

    private Service mService;

    private byte[] file2Bytes(@NonNull File pFile) {
        byte[] buffer = new byte[8192];
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mService = new Service(this.getApplicationContext());
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
                if (resultCode == RESULT_OK) {
                    finish();
                } else if (resultCode == RESULT_CANCELED) {
                    finish();
                    Toast.makeText(this.getApplicationContext(),getString(R.string.cancelled),Toast.LENGTH_LONG).show();
                }
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
            File file;
            try {
                file = File.createTempFile("temp","csv");
                Invoice.createCSV(file,mService.Invoices.getAll());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            OutputStream outputStream = result.getDriveContents().getOutputStream();
            try {
                outputStream.write(file2Bytes(file));
                outputStream.close();
            } catch (IOException e1) {
                Log.i(TAG, "Unable to write file contents.");
                throw new RuntimeException(e1);
            }


            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                    .setMimeType("text/csv").build();


            IntentSender intentSender = Drive.DriveApi
                    .newCreateFileActivityBuilder()
                    .setInitialMetadata(metadataChangeSet)
                    .setInitialDriveContents(result.getDriveContents())
                    .build(getGoogleApiClient());
            try {
                startIntentSenderForResult(intentSender, REQUEST_CODE_SAVE, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // Handle the exception
            }
        }
    };
}
