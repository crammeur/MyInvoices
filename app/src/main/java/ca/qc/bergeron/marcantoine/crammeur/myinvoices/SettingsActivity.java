package ca.qc.bergeron.marcantoine.crammeur.myinvoices;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import ca.qc.bergeron.marcantoine.crammeur.librairy.repository.crud.FilesTemplate;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.invoice.InvoiceContent;

/**
 * Created by Marc-Antoine on 2017-07-29.
 */

public class SettingsActivity extends AppCompatActivity {

    private TextView tvGST;
    private TextView tvPST;
    private TextView tvHST;
    private TextView tvGSTPercent;
    private TextView tvPSTPercent;
    private TextView tvHSTPercent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvGST = (TextView) findViewById(R.id.tvSettingsGST);
        tvPST = (TextView) findViewById(R.id.tvSettingsPST);
        tvHST = (TextView) findViewById(R.id.tvSettingsHST);
        tvGSTPercent = (TextView) findViewById(R.id.etSettingsGSTPercent);
        tvPSTPercent = (TextView) findViewById(R.id.etSettingsPSTPercent);
        tvHSTPercent = (TextView) findViewById(R.id.etSettingsHSTPercent);

        tvGST.setText(MainActivity.Settings.GSTName);
        tvPST.setText(MainActivity.Settings.PSTName);
        tvHST.setText(MainActivity.Settings.HSTName);

        tvGSTPercent.setHint(((MainActivity.Settings.GSTName.equals(""))?getString(R.string.gst):MainActivity.Settings.GSTName) + " %");
        tvGSTPercent.setText((MainActivity.Settings.GST != null)?String.valueOf(MainActivity.Settings.GST):"");
        tvPSTPercent.setHint(((MainActivity.Settings.PSTName.equals(""))?getString(R.string.pst):MainActivity.Settings.PSTName) + " %");
        tvPSTPercent.setText((MainActivity.Settings.PST != null)?String.valueOf(MainActivity.Settings.PST):"");
        tvHSTPercent.setHint(((MainActivity.Settings.HSTName.equals(""))?getString(R.string.hst):MainActivity.Settings.HSTName) + " %");
        tvHSTPercent.setText((MainActivity.Settings.HST != null)?String.valueOf(MainActivity.Settings.HST):"");

        TextView tvCSV = findViewById(R.id.tvSettingsCSV);
        tvCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this, CSVDriveActivity.class);
                startActivity(i);
            }
        });
        TextView tvClearDB = findViewById(R.id.tvSettingsClearDB);
        tvClearDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setCancelable(false);
                builder.setMessage(SettingsActivity.this.getResources().getString(R.string.delete_message));
                builder.setPositiveButton(SettingsActivity.this.getResources().getString(R.string.yes), new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File folderPDF = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"My Invoices/PDF");
                        File folderCSV = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"My Invoices/CSV");
                        System.gc();
                        if (folderPDF.exists())
                            FilesTemplate.clearFolder(folderPDF);
                        if (folderCSV.exists())
                            FilesTemplate.clearFolder(folderCSV);

                        MainActivity.Service.Invoices.clear();
                        InvoiceContent.clear();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(SettingsActivity.this.getResources().getString(R.string.no), new Dialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        TextView tvSaveDrive = findViewById(R.id.tvSettingsSaveDrive);
        tvSaveDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this,SaveDriveActivity.class);
                startActivity(i);
            }
        });
        TextView tvLoadDrive = findViewById(R.id.tvSettingsLoadDrive);
        tvLoadDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SettingsActivity.this,LoadDriveActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        MainActivity.Settings.GSTName = tvGST.getText().toString();
        MainActivity.Settings.PSTName = tvPST.getText().toString();
        MainActivity.Settings.HSTName = tvHST.getText().toString();
        MainActivity.Settings.GST = (!tvGSTPercent.getText().toString().equals(""))?Double.parseDouble(tvGSTPercent.getText().toString()):null;
        MainActivity.Settings.PST = (!tvPSTPercent.getText().toString().equals(""))?Double.parseDouble(tvPSTPercent.getText().toString()):null;
        MainActivity.Settings.HST = (!tvHSTPercent.getText().toString().equals(""))?Double.parseDouble(tvHSTPercent.getText().toString()):null;
        try {
            if (MainActivity.FileSettings.exists() || MainActivity.FileSettings.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(MainActivity.FileSettings);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(MainActivity.Settings);
                oos.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
