package ca.qc.bergeron.marcantoine.crammeur.myinvoices;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ca.qc.bergeron.marcantoine.crammeur.android.models.Company;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice;
import ca.qc.bergeron.marcantoine.crammeur.android.service.Service;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.DeleteException;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.invoice.InvoiceActivity;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.invoice.InvoiceContent;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.setting.Settings;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private InvoiceRecyclerViewAdapter iRVAdapter;
    private SpinnerCompanyAdapter sCAdapter;
    private List<Company> mCompanys = new ArrayList<>();
    public static Service Service;
    public static File FileSettings;
    public static ca.qc.bergeron.marcantoine.crammeur.myinvoices.setting.Settings Settings = new Settings();

    private TextView tvSubTotal;
    private TextView tvTPS;
    private TextView tvTVQ;
    private TextView tvTVH;
    private TextView tvTotal;
    private Spinner sCompany;

    public void refresh() {
        BigDecimal tps = new BigDecimal(0);
        BigDecimal tvq = new BigDecimal(0);
        BigDecimal tva = new BigDecimal(0);
        BigDecimal subTotal = new BigDecimal(0);
        BigDecimal total = new BigDecimal(0);
        for (InvoiceContent.InvoiceItem ii : InvoiceContent.ITEMS) {
            tva = tva.add(BigDecimal.valueOf(ii.Invoice.TVH));
            tps = tps.add(BigDecimal.valueOf(ii.Invoice.TPS));
            tvq = tvq.add(BigDecimal.valueOf(ii.Invoice.TVP));
            subTotal = subTotal.add(BigDecimal.valueOf(ii.Invoice.getPaidSubtotal()));
            total = total.add(BigDecimal.valueOf(ii.Invoice.Total));
        }
        tvSubTotal.setText(new DecimalFormat("0.00").format(subTotal.doubleValue()));
        tvTPS.setText(new DecimalFormat("0.00").format(tps.doubleValue()));
        tvTVQ.setText(new DecimalFormat("0.00").format(tvq.doubleValue()));
        tvTVH.setText(new DecimalFormat("0.00").format(tva.doubleValue()));
        tvTotal.setText(new DecimalFormat("0.00").format(total.doubleValue()));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FileSettings = new File(getApplicationContext().getCacheDir(),".settings");
        try {
            if (FileSettings.exists()) {
                FileInputStream fis = new FileInputStream(FileSettings);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Settings = (Settings) ois.readObject();
                ois.close();
                fis.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ||
                !ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                !ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.GET_ACCOUNTS) ||
                !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS},
                    PackageManager.PERMISSION_GRANTED);
        } else if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            finish();
        }

        TextView tvInvoice = findViewById(R.id.tvMainInvoice);
        tvInvoice.setText(getString(R.string.word_plurial, getString(R.string.invoice)));

        if (Service == null) Service = new Service(this);
        iRVAdapter = new InvoiceRecyclerViewAdapter(InvoiceContent.ITEMS);

        tvSubTotal = (TextView) findViewById(R.id.tvMainSubtotal);


        tvTPS = (TextView) findViewById(R.id.tvMainTPS);
        tvTVQ = (TextView) findViewById(R.id.tvMainTVQ);
        tvTVH = (TextView) findViewById(R.id.tvMainTVH);
        tvTotal = (TextView) findViewById(R.id.tvMainTotal);
        sCompany = (Spinner) findViewById(R.id.sMainCompany);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabMainAddInvoive);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,InvoiceActivity.class);
                startActivity(i);
            }
        });
        RecyclerView v = (RecyclerView) findViewById(R.id.invoice_list);
        assert v != null;
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(v.getContext(), DividerItemDecoration.VERTICAL);
        v.addItemDecoration(dividerItemDecoration);
        v.setAdapter(iRVAdapter);
    }

    @Override
    public void onStart(){
        super.onStart();


        InvoiceContent.clear();
        for (Invoice i : Service.Invoices.getAll()) {
            InvoiceContent.addItem(new InvoiceContent.InvoiceItem(i));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        TextView tvGST = findViewById(R.id.tvMainGST);
        tvGST.setText((Settings.GSTName.equals("")?getString(R.string.gst):Settings.GSTName));
        TextView tvPST = findViewById(R.id.tvMainPST);
        tvPST.setText((Settings.PSTName.equals("")?getString(R.string.pst):Settings.PSTName));
        TextView tvHST = findViewById(R.id.tvMainHST);
        tvHST.setText((Settings.HSTName.equals("")?getString(R.string.hst):Settings.PSTName));

        iRVAdapter.notifyDataSetChanged();
        List<String> companys = new ArrayList<>();
        companys.add(this.getResources().getString(R.string.all));
        for (Company c : Service.Companys.getAll()) {
            companys.add(c.Name);
        }
        sCAdapter = new SpinnerCompanyAdapter(this,companys);
        sCompany.setAdapter(sCAdapter);
        sCompany.setOnItemSelectedListener(this);

        refresh();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent i;
        switch (id) {

            case R.id.action_settings:

                i = new Intent(this, SettingsActivity.class);
                startActivity(i);

                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mCompanys.clear();
        if (i==0) {
            mCompanys.addAll(Service.Companys.getAll());
        } else {
            for (Company c : Service.Companys.getAll()) {
                if (c.Name.equals(sCompany.getItemAtPosition(i))) {
                    mCompanys.add(c);
                    break;
                }
            }
        }
        InvoiceContent.clear();
        for (Invoice invoice : Service.Invoices.getAll()) {
            if (mCompanys.contains(invoice.Company)) {
                InvoiceContent.addItem(new InvoiceContent.InvoiceItem(invoice));
            }
        }
        iRVAdapter.notifyDataSetChanged();
        refresh();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case PackageManager.PERMISSION_GRANTED:

                for (int index=0; index<permissions.length && index<grantResults.length; index++) {
                    if ((permissions[index].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) || permissions[index].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) && grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        final boolean sRequestRES = shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE);
                        final boolean sRequestWES = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (sRequestRES && sRequestWES) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},PackageManager.PERMISSION_GRANTED);
                            break;
                        } else
                            finish();
                    }
                }

                break;

        }
    }

    private class InvoiceRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<InvoiceContent.InvoiceItem> mValues;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView tvDescription;
            public final TextView tvDate;
            public final TextView tvId;
            public final TextView tvCompanyName;
            public final TextView tvClientName;
            public final TextView tvTotal;
            public InvoiceContent.InvoiceItem mItem;

            public ViewHolder(View view) {
                super(view);
                this.mView = view;
                this.tvId = (TextView) mView.findViewById(R.id.tvILCID);
                this.tvDescription = (TextView) mView.findViewById(R.id.tvILCDescription);
                this.tvDate = (TextView) mView.findViewById(R.id.tvILCDate);
                this.tvCompanyName = (TextView) mView.findViewById(R.id.tvILCCompanyName);
                this.tvClientName = (TextView) mView.findViewById(R.id.tvILCClientName);
                this.tvTotal = (TextView) mView.findViewById(R.id.tvILCTotal);
             }
        }

        public InvoiceRecyclerViewAdapter(List<InvoiceContent.InvoiceItem> pList) {
            mValues = pList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.invoice_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ViewHolder content = (ViewHolder) holder;
            content.mItem = mValues.get(position);
            content.tvId.setText((content.mItem.Invoice.Id != null)?String.valueOf(content.mItem.Invoice.Id):"");
            if (content.mItem.Invoice.Details.equals("")) {
                content.tvDescription.setVisibility(View.GONE);
            } else {
                content.tvDescription.setText(content.mItem.Invoice.Details);
            }
            content.tvDate.setText(new DateTime(content.mItem.Invoice.Date).toString("yyyy-MM-dd HH:mm:ss"));
            content.tvCompanyName.setText(content.mItem.Invoice.Company.Name);
            if (content.mItem.Invoice.Client.Name.equals("")) {
                content.tvClientName.setText(content.mItem.Invoice.Client.EMail);
            } else {
                content.tvClientName.setText(content.mItem.Invoice.Client.Name);
            }
            content.tvTotal.setText(new DecimalFormat("0.00").format(content.mItem.Invoice.Total));
            content.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage(v.getResources().getString(R.string.do_you_want_delete_or_edit_this_invoice));
                    builder.setPositiveButton(v.getResources().getString(R.string.edit), new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(v.getContext(), InvoiceActivity.class);
                            intent.putExtra(InvoiceActivity.EXTRA_INVOICE, content.mItem);
                            startActivity(intent);
                        }
                    });

                    builder.setNegativeButton(v.getResources().getString(R.string.delete), new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            builder.setMessage(v.getResources().getString(R.string.delete_message) + " / " + getResources().getString(R.string.invoice) + "#" + content.mItem.Invoice.Id + " - " + new DateTime(content.mItem.Invoice.Date).toString("yyyy-MM-dd HH:mm:ss") + " - " + String.valueOf(content.mItem.Invoice.Total));
                            builder.setPositiveButton(v.getResources().getString(R.string.yes), new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        File pdf = new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"My Invoices/PDF"),"Invoice-Facture#" + content.mItem.Id + ".pdf");
                                        File csv = new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"My Invoices/CSV"),"Invoice-Facture#" + content.mItem.Id + ".csv");
                                        if (pdf.exists()) if (!pdf.delete()) throw new RuntimeException(new IOException("Delete pdf"));
                                        if (csv.exists()) if (!csv.delete()) throw new RuntimeException(new IOException("Delete csv"));
                                        Service.Invoices.delete(content.mItem.Invoice.Id);
                                        InvoiceContent.removeItem(content.mItem.Id);
                                        iRVAdapter.notifyDataSetChanged();
                                        refresh();
                                        dialog.dismiss();
                                    } catch (KeyException e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    } catch (DeleteException e) {
                                        e.printStackTrace();
                                        throw new RuntimeException(e);
                                    }
                                }
                            });

                            builder.setNegativeButton(v.getResources().getString(R.string.no), new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                            builder.show();
                        }
                    });

                    builder.show();
                    return true;
                }
            });
            content.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    File folderPDF = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"My Invoices/PDF");
                    folderPDF.mkdirs();
                    File pdf = new File(folderPDF, "Invoice-Facture#" + content.mItem.Id + ".pdf");
                    if (!pdf.exists()) try {
                        ca.qc.bergeron.marcantoine.crammeur.android.utils.itext.pdf.Invoice.createPDF(getResources(),pdf,content.mItem.Invoice);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    // Get URI and MIME type of file
                    Uri uri = Uri.fromFile(pdf);
                    String mime = getContentResolver().getType(uri);

                    // Open file with user selected app
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, mime);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }

    private class SpinnerCompanyAdapter extends ArrayAdapter<String> {

        public SpinnerCompanyAdapter(@NonNull Context context, @NonNull List<String> objects) {
            super(context, android.R.layout.simple_spinner_dropdown_item, objects);
        }
    }
}
