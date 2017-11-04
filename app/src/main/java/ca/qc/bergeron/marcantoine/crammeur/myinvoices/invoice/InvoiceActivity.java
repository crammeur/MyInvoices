package ca.qc.bergeron.marcantoine.crammeur.myinvoices.invoice;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ca.qc.bergeron.marcantoine.crammeur.android.models.Client;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Company;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Product;
import ca.qc.bergeron.marcantoine.crammeur.android.models.data.ShopProduct;
import ca.qc.bergeron.marcantoine.crammeur.android.service.Service;
import ca.qc.bergeron.marcantoine.crammeur.librairy.exceptions.KeyException;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.MainActivity;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.R;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.invoice.product.ProductContent;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.product.ProductActivity;

/**
 * Created by Marc-Antoine on 2017-03-28.
 */

public class InvoiceActivity extends AppCompatActivity {

    public static final String EXTRA_INVOICE = "item_invoice";

    private static Service service;
    private ProductRecyclerViewAdapter pRVAdapter;

    private EditText etId;
    private EditText etDetails;
    private AutoCompleteTextView actvCompanyName;
    private AutoCompleteTextView actvCompanyEMail;
    private AutoCompleteTextView actvCompanyAddress;
    private AutoCompleteTextView actvCompanyPhone;
    private AutoCompleteTextView actvCompanyTPSCode;
    private AutoCompleteTextView actvCompanyTVQCode;
    private AutoCompleteTextView actvCompanyTVHCode;
    private AutoCompleteTextView actvClientName;
    private AutoCompleteTextView actvClientEMail;
    private TextView tvSubtotal;
    private TextView tvTVH;
    private TextView tvTPS;
    private TextView tvTVQ;
    private TextView tvTotal;

    private double mSubTotal;
    private double mHST;
    private double mGST;
    private double mPST;
    private double mTotal;

    public void refresh() {
        mSubTotal = 0;
        mGST = 0;
        mPST = 0;
        mHST = 0;
        mTotal = 0;
        BigDecimal tps = new BigDecimal(0);
        BigDecimal tvq = new BigDecimal(0);
        BigDecimal tva = new BigDecimal(0);
        BigDecimal subTotal = new BigDecimal(0);
        for (ProductContent.ProductItem pi : ProductContent.ITEMS) {
            BigDecimal total = BigDecimal.valueOf(pi.Price)/*.divide(BigDecimal.valueOf(pi.Unit))*/.multiply(BigDecimal.valueOf(pi.Quantity));
            mSubTotal += total.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            BigDecimal bdtps = total.multiply(BigDecimal.valueOf(pi.TPS).divide(BigDecimal.valueOf(100)));
            mGST += bdtps.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            BigDecimal bdtvq = total.multiply(BigDecimal.valueOf(pi.TVQ).divide(BigDecimal.valueOf(100)));
            mPST += bdtvq.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            BigDecimal bdtva = total.multiply(BigDecimal.valueOf(pi.TVA).divide(BigDecimal.valueOf(100)));
            mHST += bdtva.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            subTotal = subTotal.add(total);
            tps = tps.add(bdtps);
            tvq = tvq.add(bdtvq);
            tva = tva.add(bdtva);
        }
        tvSubtotal.setText(new DecimalFormat("0.00").format(mSubTotal));
        tvTVH.setText(new DecimalFormat("0.00").format(mHST));
        tvTPS.setText(new DecimalFormat("0.00").format(mGST));
        tvTVQ.setText(new DecimalFormat("0.00").format(mPST));
        BigDecimal bdsum = new BigDecimal(mSubTotal)
                .add(BigDecimal.valueOf(mGST))
                .add(BigDecimal.valueOf(mPST))
                .add(BigDecimal.valueOf(mHST))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        mTotal = bdsum.doubleValue();
        tvTotal.setText(new DecimalFormat("0.00").format(mTotal));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        if (service == null) service = new Service(this);
        pRVAdapter = new ProductRecyclerViewAdapter(ProductContent.ITEMS);

        etId = (EditText) findViewById(R.id.etInvoiceId);
        etDetails = (EditText) findViewById(R.id.etInvoiceDetails);

        actvCompanyName = (AutoCompleteTextView) findViewById(R.id.actvInvoiceCompanyName);
        actvCompanyName.setThreshold(2);

        actvCompanyEMail = (AutoCompleteTextView) findViewById(R.id.actvInvoiceCompanyEMail);
        //(AccountManager) getSystemService(ACCOUNT_SERVICE);
        Account[] accounts = AccountManager.get(this).getAccounts();
        List<String> as = new ArrayList<>();
        for (int i = 0;i<accounts.length;i++) {
            if (Patterns.EMAIL_ADDRESS.matcher(accounts[i].name).matches() && !as.contains(accounts[i].name)) {
                as.add(accounts[i].name);
            }

        }
        actvCompanyEMail.setThreshold(1);
        actvCompanyEMail.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,as));

        actvCompanyAddress = (AutoCompleteTextView) findViewById(R.id.actvInvoiceCompanyAddress);
        actvCompanyAddress.setThreshold(1);
        actvCompanyPhone = (AutoCompleteTextView) findViewById(R.id.actvInvoiceCompanyPhone);
        actvCompanyPhone.setThreshold(1);
        actvCompanyTPSCode = (AutoCompleteTextView) findViewById(R.id.actvInvoiceCompanyGSTCode);
        actvCompanyTPSCode.setThreshold(1);
        actvCompanyTVQCode = (AutoCompleteTextView) findViewById(R.id.actvInvoiceCompanyPSTCode);
        actvCompanyTVQCode.setThreshold(1);
        actvCompanyTVHCode = (AutoCompleteTextView) findViewById(R.id.actvInvoiceCompanyHSTCode);
        actvCompanyTVHCode.setThreshold(1);

        List<String> lcn = new ArrayList<>();
        List<String> lca = new ArrayList<>();
        List<String> lcp = new ArrayList<>();
        List<String> lcgstc = new ArrayList<>();
        List<String> lcpstc = new ArrayList<>();
        List<String> lchstc = new ArrayList<>();
        for (Company c : service.Companys.getAll()) {
            if (!lcn.contains(c.Name))
                lcn.add(c.Name);
            if (!lca.contains(c.Address))
                lca.add(c.Address);
            if (!lcp.contains(c.Phone))
                lcp.add(c.Phone);
            if (!lcgstc.contains(c.TPSCode))
                lcgstc.add(c.TPSCode);
            if (!lcpstc.contains(c.TVPCode))
                lcpstc.add(c.TVPCode);
            if (!lchstc.contains(c.TVHCode))
                lchstc.add(c.TVHCode);
        }
        actvCompanyName.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,lcn));
        actvCompanyAddress.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,lca));
        actvCompanyPhone.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,lcp));
        actvCompanyTPSCode.setHint(getResources().getString(R.string.taxe_code, (MainActivity.Settings.GSTName.equals(""))?getString(ca.qc.bergeron.marcantoine.crammeur.R.string.gst):MainActivity.Settings.GSTName));
        actvCompanyTPSCode.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,lcgstc));
        actvCompanyTVQCode.setHint(getResources().getString(R.string.taxe_code, (MainActivity.Settings.PSTName.equals(""))?getResources().getString(ca.qc.bergeron.marcantoine.crammeur.R.string.pst):MainActivity.Settings.PSTName));
        actvCompanyTVQCode.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,lcpstc));
        actvCompanyTVHCode.setHint(getResources().getString(R.string.taxe_code, (MainActivity.Settings.HSTName.equals(""))?getResources().getString(ca.qc.bergeron.marcantoine.crammeur.R.string.hst):MainActivity.Settings.HSTName));
        actvCompanyTVHCode.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,lchstc));

        actvClientName = (AutoCompleteTextView) findViewById(R.id.actvInvoiceClientName);
        actvClientEMail = (AutoCompleteTextView) findViewById(R.id.actvInvoiceClientEMail);
        List<String> cn = new ArrayList<>();
        List<String> ce = new ArrayList<>();
        for (Client c : service.Clients.getAll()) {
            if (!cn.contains(c.Name))
                cn.add(c.Name);
            if (!ce.contains(c.EMail))
                ce.add(c.EMail);
        }

        String id = null;
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        while (cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if (!cn.contains(name)) {
                cn.add(name);
            }
            if (cursor.getExtras().containsKey(ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX) &&
                    cursor.getExtras().containsKey(ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS)) {
            }
            Cursor pCursor = cr.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    new String[]{id},
                    null
            );
            while (pCursor.moveToNext()) {
                String email = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                if (!ce.contains(email)) {
                    ce.add(email);
                }
            }
            pCursor.close();
        }
        cursor.close();


        //actvClientName.setTokenizer(new  MultiAutoCompleteTextView.CommaTokenizer());
        actvClientName.setThreshold(2);
        actvClientEMail.setThreshold(2);
        actvClientName.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,cn));
        actvClientEMail.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_dropdown_item_1line,ce));

        TextView tvProducts = findViewById(R.id.tvInvoiceProducts);
        tvProducts.setText(getString(R.string.word_plurial, getString(R.string.product)));
        Button btnAddProduct = (Button) findViewById(R.id.btnInvoiceAddProduct);
        btnAddProduct.setText(getResources().getString(ca.qc.bergeron.marcantoine.crammeur.R.string.add) + " " + getResources().getString(ca.qc.bergeron.marcantoine.crammeur.R.string.product).toLowerCase());

        tvSubtotal = (TextView) findViewById(R.id.tvInvoiceSubtotal);
        TextView tvHST = findViewById(R.id.tvInvoiceHST);
        tvHST.setText((MainActivity.Settings.HSTName.equals("")?getString(R.string.hst):MainActivity.Settings.HSTName));
        tvTVH = (TextView) findViewById(R.id.tvInvoiceTVH);
        TextView tvGST = findViewById(R.id.tvInvoiceGST);
        tvGST.setText((MainActivity.Settings.GSTName.equals("")?getString(R.string.gst):MainActivity.Settings.GSTName));
        tvTPS = (TextView) findViewById(R.id.tvInvoiceTVG);
        TextView tvPST = findViewById(R.id.tvInvoicePST);
        tvPST.setText((MainActivity.Settings.PSTName.equals("")?getString(R.string.pst):MainActivity.Settings.PSTName));
        tvTVQ = (TextView) findViewById(R.id.tvInvoiceTVP);
        tvTotal = (TextView) findViewById(R.id.tvInvoiceSum);

        Intent i = getIntent();
        if (i.hasExtra(EXTRA_INVOICE)) {
            InvoiceContent.InvoiceItem ii = (InvoiceContent.InvoiceItem) i.getExtras().get(EXTRA_INVOICE);

            etId.setEnabled(false);
            etId.setText(ii.Id);
            etDetails.setText(ii.Invoice.Details);
            actvCompanyName.setText(ii.Invoice.Company.Name);
            actvCompanyEMail.setText(ii.Invoice.Company.EMail);
            actvCompanyAddress.setText(ii.Invoice.Company.Address);
            actvCompanyPhone.setText(ii.Invoice.Company.Phone);
            actvCompanyTPSCode.setText(ii.Invoice.Company.TPSCode);
            actvCompanyTVQCode.setText(ii.Invoice.Company.TVPCode);
            actvCompanyTVHCode.setText(ii.Invoice.Company.TVHCode);
            actvClientName.setText(ii.Invoice.Client.Name);
            actvClientEMail.setText(ii.Invoice.Client.EMail);
            for (ShopProduct product : ii.Invoice.Products) {
                ProductContent.addItem(new ProductContent.ProductItem(product));
            }

        }

        RecyclerView v = (RecyclerView) findViewById(R.id.product_list);
        assert v != null;
        v.setAdapter(pRVAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        pRVAdapter.notifyDataSetChanged();
        refresh();
    }

    @Override
    public void finish(){
        super.finish();
        ProductContent.clear();
    }

    public void saveInvoice(View pView) {
        Integer id = null;
        if (!etId.getText().toString().equals("")) {
            id = Integer.parseInt(etId.getText().toString());
        }
        Pattern address = Pattern.compile("((?:[0-9]+-)?[0-9]+[-A-Z]* (?:[^\\s;,\\n]+(?: [^\\s;,\\n]+)*)[\n][^\\s;,\\n]+, [^\\s;,\\n]+, [A-Z][0-9][A-Z][ ]?[0-9][A-Z][0-9])");
        if (id != null && etId.isEnabled() && service.Invoices.contains(id)) {
            Toast.makeText(this,getResources().getString(R.string.id_already_used),Toast.LENGTH_LONG).show();
        } else if (actvCompanyName.getText().toString().equals("") || !Patterns.EMAIL_ADDRESS.matcher(actvCompanyEMail.getText()).matches() ||
                (actvClientName.getText().toString().equals("") && mTotal >= 150) || !Patterns.EMAIL_ADDRESS.matcher(actvClientEMail.getText()).matches() ||
                !Patterns.PHONE.matcher(actvCompanyPhone.getText()).matches() || !address.matcher(actvCompanyAddress.getText()).matches() ||
                (mTotal >= 30 &&
                        (
                                (mGST != 0 && actvCompanyTPSCode.getText().toString().equals("")) ||
                                (mPST != 0 && actvCompanyTVQCode.getText().toString().equals("")) ||
                                (mHST != 0 && actvCompanyTVHCode.getText().toString().equals(""))
                        )
                ) ||
                pRVAdapter.mValues.size() == 0) {
            String values = "";
            if (actvCompanyName.getText().toString().equals("")) {
                values = values.concat("\n" + actvCompanyName.getHint().toString());
                actvCompanyName.setHintTextColor(Color.RED);
            } else {
                actvCompanyName.setHintTextColor(Color.GRAY);
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(actvCompanyEMail.getText().toString()).matches()) {
                values = values.concat("\n" + actvCompanyEMail.getHint().toString());
                actvCompanyEMail.setHintTextColor(Color.RED);
            } else {
                actvCompanyEMail.setHintTextColor(Color.GRAY);
            }
            if (!address.matcher(actvCompanyAddress.getText().toString()).matches()) {
                values = values.concat("\n" + actvCompanyAddress.getHint().toString());
                actvCompanyAddress.setHintTextColor(Color.RED);
            } else {
                actvCompanyAddress.setHintTextColor(Color.GRAY);
            }
            if (!Patterns.PHONE.matcher(actvCompanyPhone.getText().toString()).matches()) {
                values = values.concat("\n" + actvCompanyPhone.getHint().toString());
                actvCompanyPhone.setHintTextColor(Color.RED);
            } else {
                actvCompanyPhone.setHintTextColor(Color.GRAY);
            }
            if (actvCompanyTPSCode.getText().toString().equals("") && mTotal >= 30 && mGST != 0) {
                values = values.concat("\n" + actvCompanyTPSCode.getHint().toString());
                actvCompanyTPSCode.setHintTextColor(Color.RED);
            } else {
                actvCompanyTPSCode.setHintTextColor(Color.GRAY);
            }
            if (actvCompanyTVQCode.getText().toString().equals("") && mTotal >= 30 && mPST != 0) {
                values = values.concat("\n" + actvCompanyTVQCode.getHint().toString());
                actvCompanyTVQCode.setHintTextColor(Color.RED);
            } else {
                actvCompanyTVQCode.setHintTextColor(Color.GRAY);
            }
            if (actvCompanyTVHCode.getText().toString().equals("") && mTotal >= 30 && mHST != 0) {
                values = values.concat("\n" + actvCompanyTVHCode.getHint().toString());
                actvCompanyTVHCode.setHintTextColor(Color.RED);
            } else {
                actvCompanyTVHCode.setHintTextColor(Color.GRAY);
            }
            if (actvClientName.getText().toString().equals("") && mTotal >= 150) {
                values = values.concat("\n" + actvClientName.getHint().toString());
                actvClientName.setHintTextColor(Color.RED);
            } else {
                actvClientName.setHintTextColor(Color.GRAY);
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(actvClientEMail.getText().toString()).matches()) {
                values = values.concat("\n" + actvClientEMail.getHint().toString());
                actvClientEMail.setHintTextColor(Color.RED);
            } else {
                actvClientEMail.setHintTextColor(Color.GRAY);
            }
            if (pRVAdapter.mValues.size() == 0) {
                values = values.concat("\n" + getString(R.string.word_plurial, getString(R.string.product)));
            }

            Toast.makeText(this,getResources().getString(R.string.missing_value) + ":" + values,Toast.LENGTH_LONG).show();
        } else {
            Invoice invoice = new Invoice();
            if (getIntent().hasExtra(EXTRA_INVOICE)) {
                invoice.Id = Integer.parseInt(((InvoiceContent.InvoiceItem) getIntent().getExtras().get(EXTRA_INVOICE)).Id);
                invoice.Date = ((InvoiceContent.InvoiceItem) getIntent().getExtras().get(EXTRA_INVOICE)).Invoice.Date;
                if (InvoiceContent.ITEM_MAP.keySet().contains(String.valueOf(invoice.Id)))
                    InvoiceContent.removeItem(invoice.Id.toString());
            } else if (id != null) {
                invoice.Id = id;
            }

            invoice.Details = etDetails.getText().toString();
            invoice.Company.Name = actvCompanyName.getText().toString();
            invoice.Company.EMail = actvCompanyEMail.getText().toString();
            invoice.Company.Address = actvCompanyAddress.getText().toString();
            invoice.Company.Phone = actvCompanyPhone.getText().toString();
            invoice.Company.TPSCode = actvCompanyTPSCode.getText().toString();
            invoice.Company.TVPCode = actvCompanyTVQCode.getText().toString();
            invoice.Company.TVHCode = actvCompanyTVHCode.getText().toString();
            invoice.Client.Name = actvClientName.getText().toString();
            invoice.Client.EMail = actvClientEMail.getText().toString();

            ProductContent.update(service);
            for (ProductContent.ProductItem pi : ProductContent.ITEMS) {
                Product p = new Product();
                p.Id = Integer.parseInt(pi.Id);
                p.Name = pi.Name;
                p.Description = pi.Description;
                p.Price = pi.Price;
                //p.Unit = pi.Unit;
                p.TVH = pi.TVA;
                p.TPS = pi.TPS;
                p.TVP = pi.TVQ;
                ShopProduct sp = ShopProduct.createFromProduct(p,pi.Quantity);
                invoice.Products.add(sp);
            }

            invoice.TVH = mHST;
            invoice.TVP = mPST;
            invoice.TPS = mGST;
            //invoice.Subtotal = mSubTotal;
            invoice.Total = mTotal;

            try {
                service.Invoices.save(invoice);
            } catch (KeyException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            InvoiceContent.addItem(new InvoiceContent.InvoiceItem(invoice));

            File directoryPDF = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"My Invoices/PDF");
            if (!directoryPDF.exists() && !directoryPDF.mkdirs()) throw new RuntimeException(new FileNotFoundException(directoryPDF.getName()));
            File directoryCSV = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"My Invoices/CSV");
            if (!directoryCSV.exists() && !directoryCSV.mkdirs()) throw new RuntimeException(new FileNotFoundException(directoryCSV.getName()));
            File fileCSV = new File(directoryCSV,"Invoice-Facture#" + String.valueOf(invoice.Id) + ".csv");
            File filePDF = new File(directoryPDF,"Invoice-Facture#" + String.valueOf(invoice.Id) + ".pdf");
            try {
                if (filePDF.exists() || filePDF.createNewFile())
                    ca.qc.bergeron.marcantoine.crammeur.android.utils.itext.pdf.Invoice.createPDF(getResources(),filePDF,invoice,
                            (!MainActivity.Settings.GSTName.equals("")? MainActivity.Settings.GSTName :getResources().getString(R.string.gst)),
                            (!MainActivity.Settings.PSTName.equals("")?MainActivity.Settings.PSTName :getResources().getString(R.string.pst)),
                            (!MainActivity.Settings.HSTName.equals("")?MainActivity.Settings.HSTName :getResources().getString(R.string.hst)));
                else
                    throw new FileNotFoundException(filePDF.getName());
                if (filePDF.exists() || fileCSV.createNewFile())
                    ca.qc.bergeron.marcantoine.crammeur.android.utils.csv.Invoice.createCSV(fileCSV,invoice);
                else
                    throw new FileNotFoundException(fileCSV.getName());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            Intent email = new Intent(Intent.ACTION_SEND_MULTIPLE);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{ actvClientEMail.getText().toString()});
            email.putExtra(Intent.EXTRA_SUBJECT, "Invoice-Facture#:" + String.valueOf(invoice.Id));
            email.putExtra(Intent.EXTRA_TEXT,"Thank you for buying\nMerci d'avoir acheté");
            ArrayList<Uri> uris = new ArrayList<>();
            uris.add(Uri.fromFile(filePDF));
            uris.add(Uri.fromFile(fileCSV));
            email.putExtra(Intent.EXTRA_STREAM, uris);

            //need this to prompts email client only
            email.setType("message/rfc822");

            startActivity(Intent.createChooser(email, getString(R.string.word_special_003A, getString(R.string.choose_an_email_client))));//Intent.createChooser(email, "Choose an Email client :")
            finish();
        }
    }

    public void addProduct(View pView) {
        Intent intent = new Intent(this,ProductActivity.class);
        startActivity(intent);
    }

    private class ProductRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<ProductContent.ProductItem> mValues;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mNameView;
            public final TextView mPriceView;
            public final TextView mQuantityView;
            public final TextView mTotalView;
            public final Button mDelete;
            public ProductContent.ProductItem mItem;

            public ViewHolder(View view) {
                super(view);
                this.mView = view;
                this.mNameView = (TextView) view.findViewById(R.id.tvPLCName);
                this.mPriceView = (TextView) view.findViewById(R.id.tvPLCPrice);
                this.mQuantityView = (TextView) view.findViewById(R.id.tvPLCQuantity);
                this.mTotalView = (TextView) view.findViewById(R.id.tvPLCSubtotal);
                this.mDelete = (Button) view.findViewById(R.id.btnPLCDelete);
                mDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Service.Products.clear(Integer.parseInt(mItem.Id));
                        ProductContent.removeItem(mItem);
                        pRVAdapter.notifyDataSetChanged();
                        refresh();

                    }
                });
            }
        }

        public ProductRecyclerViewAdapter(List<ProductContent.ProductItem> pList) {
            mValues = pList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.product_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ViewHolder content = (ViewHolder) holder;
            content.mItem = mValues.get(position);
            content.mNameView.setText(content.mItem.Name);
            content.mPriceView.setText(new DecimalFormat("0.00").format(content.mItem.Price));
            //content.mUnitView.setText(new DecimalFormat("0.0#").format(content.mItem.Unit));
            content.mQuantityView.setText(new DecimalFormat("0.0#").format(content.mItem.Quantity));
            BigDecimal bd = BigDecimal.valueOf(content.mItem.Price)
                    /*.divide(BigDecimal.valueOf(content.mItem.Unit))*/
                    .multiply(BigDecimal.valueOf(content.mItem.Quantity));
            content.mTotalView.setText(new DecimalFormat("0.00").format((bd.doubleValue())));
            content.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ProductActivity.class);
                    intent.putExtra(ProductActivity.EXTRA_PRODUCT, content.mItem);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }
    }
}
