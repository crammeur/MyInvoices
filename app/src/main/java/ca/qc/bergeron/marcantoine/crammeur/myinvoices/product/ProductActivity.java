package ca.qc.bergeron.marcantoine.crammeur.myinvoices.product;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ca.qc.bergeron.marcantoine.crammeur.android.models.Invoice;
import ca.qc.bergeron.marcantoine.crammeur.android.models.Product;
import ca.qc.bergeron.marcantoine.crammeur.android.models.data.ShopProduct;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.MainActivity;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.R;
import ca.qc.bergeron.marcantoine.crammeur.myinvoices.invoice.product.ProductContent;
import ca.qc.bergeron.marcantoine.crammeur.android.service.Service;

/**
 * Created by Marc-Antoine on 2017-03-28.
 */

public class ProductActivity extends AppCompatActivity {

    @NotNull
    public static final String EXTRA_PRODUCT = "item_product";
    private boolean mEdit = false;
    private int mScale = 5;
    private static Service service;

    private EditText etName;
    private EditText etDescription;
    private EditText etPrice;
    private EditText etQuantity;
    private EditText etSubtotal;
    private EditText etTVH;
    private EditText etTPS;
    private EditText etTVQ;
    private EditText etTotal;

    private TextWatcher mNSubtotalWatcher = new TextWatcher() {
        CharSequence mCharSequence;
        boolean edit;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mCharSequence = s;
            edit = mEdit;
            if (!edit) mEdit = true;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!edit) {
                if (s.length() > 0 && !mCharSequence.equals(s)) {
                    BigDecimal subtotal = BigDecimal.valueOf(Double.parseDouble(s.toString()));
                    if (etQuantity.getText().length() == 0) {
                        etQuantity.setText("1");
                    }
                    etPrice.setText(String.valueOf(
                            subtotal
                                    .divide(BigDecimal.valueOf(Double.parseDouble(etQuantity.getText().toString())),mScale,BigDecimal.ROUND_HALF_UP)));
                    etTotal.setText(String.valueOf(subtotal
                            .add(subtotal.multiply(BigDecimal.valueOf(Double.parseDouble(etTPS.getText().toString())).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP)))
                            .add(subtotal.multiply(BigDecimal.valueOf(Double.parseDouble(etTVQ.getText().toString())).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP)))
                            .add(subtotal.multiply(BigDecimal.valueOf(Double.parseDouble(etTVH.getText().toString())).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP)))
                            .doubleValue()
                    ));
                } else {
                    etPrice.setText("");
                    etTotal.setText("");
                }
                mEdit = edit;
            }
        }
    };

    private TextWatcher mNTotalWatcher = new TextWatcher() {
        boolean edit;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            edit = mEdit;
            if (!edit) mEdit = true;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!edit) {
                if (s.length() > 0) {
                    BigDecimal total = BigDecimal.valueOf(Double.parseDouble(s.toString()));
                    BigDecimal subtotal;
                    if (etTPS.getText().length() > 0 && etTVQ.getText().length() > 0 && etTVH.getText().length() > 0) {
                        subtotal = total
                                .divide(BigDecimal.valueOf(1)
                                        .add(BigDecimal.valueOf(Double.parseDouble(etTPS.getText().toString())).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP))
                                        .add(BigDecimal.valueOf(Double.parseDouble(etTVQ.getText().toString())).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP))
                                        .add(BigDecimal.valueOf(Double.parseDouble(etTVH.getText().toString())).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP)),mScale,BigDecimal.ROUND_HALF_UP);
                        etSubtotal.setText(String.valueOf(subtotal.doubleValue()));
                        if (etQuantity.getText().length() == 0) {
                            etQuantity.setText("1");
                        }
                        etPrice.setText(String.valueOf(subtotal
                                .divide(BigDecimal.valueOf(Double.parseDouble(etQuantity.getText().toString())),mScale,BigDecimal.ROUND_HALF_UP).doubleValue()));
                    }
                }
                mEdit = edit;
            }
        }
    };

    private TextWatcher mSubtotalTotalWatcher = new TextWatcher() {
        CharSequence mCharSequence;
        boolean edit;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mCharSequence = s;
            edit = mEdit;
            if (!edit) mEdit = true;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!edit) {
                BigDecimal subtotal = null;
                if (s.length() > 0 && !mCharSequence.equals(s)) {
                    if (etPrice.getText().length() > 0 && etQuantity.getText().length() > 0) {
                        subtotal = BigDecimal.valueOf(Double.parseDouble(etPrice.getText().toString()))
                                .multiply(BigDecimal.valueOf(Double.parseDouble(etQuantity.getText().toString())));
                    } else if (etSubtotal.getText().length() > 0) {
                        subtotal = BigDecimal.valueOf(Double.parseDouble(etSubtotal.getText().toString()));
                    }

                    if (subtotal != null && s.length() > 0) {
                        etSubtotal.setText(String.valueOf(subtotal.doubleValue()));
                        if (etQuantity.getText().length() == 0) {
                            etQuantity.setText("1");
                        }
                        if (etPrice.getText().length() == 0) {
                            etPrice.setText(String.valueOf(subtotal.divide(BigDecimal.valueOf(Double.parseDouble(etQuantity.getText().toString())),mScale,BigDecimal.ROUND_HALF_UP)));
                        }
                    }

                    if (subtotal != null && etTPS.getText().length() > 0 && etTVQ.getText().length() > 0 && etTVH.getText().length() > 0) {
                        etTotal.setText(String.valueOf(
                                subtotal
                                        .add(subtotal.multiply(BigDecimal.valueOf(Double.parseDouble(etTPS.getText().toString())).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP)))
                                        .add(subtotal.multiply(BigDecimal.valueOf(Double.parseDouble(etTVQ.getText().toString())).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP)))
                                        .add(subtotal.multiply(BigDecimal.valueOf(Double.parseDouble(etTVH.getText().toString())).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP)))
                                        .doubleValue()
                        ));
                    }
                } else {
                    if (etPrice.getText().length() == 0 || etQuantity.getText().length() == 0)
                        etSubtotal.setText("");
                    etTotal.setText("");
                }
                mEdit = edit;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        if (service == null) service = new Service(this);

        etName = (EditText) findViewById(R.id.etProductName);
        etDescription = (EditText) findViewById(R.id.etProductDescription);
        etPrice = (EditText) findViewById(R.id.etProductPrice);
        etQuantity = (EditText) findViewById(R.id.etProductQuantity);
        etSubtotal = (EditText) findViewById(R.id.etProductSubtotal);
        etTVH = (EditText) findViewById(R.id.etProductTVA);
        etTVH.setHint(((MainActivity.Settings.HSTName.equals(""))?getString(R.string.hst):MainActivity.Settings.HSTName) + " %");
        etTPS = (EditText) findViewById(R.id.etProductTPS);
        etTPS.setHint(((MainActivity.Settings.GSTName.equals(""))?getString(R.string.gst):MainActivity.Settings.GSTName) + " %");
        etTVQ = (EditText) findViewById(R.id.etProductTVQ);
        etTVQ.setHint(((MainActivity.Settings.PSTName.equals(""))?getString(R.string.pst):MainActivity.Settings.PSTName) + " %");
        etTotal = (EditText) findViewById(R.id.etProductTotal);

        if (MainActivity.Settings.GST != null)
            etTPS.setText(String.valueOf(MainActivity.Settings.GST));
        if (MainActivity.Settings.PST != null)
            etTVQ.setText(String.valueOf(MainActivity.Settings.PST));
        if (MainActivity.Settings.HST != null)
            etTVH.setText(String.valueOf(MainActivity.Settings.HST));

        etPrice.addTextChangedListener(mSubtotalTotalWatcher);
        etQuantity.addTextChangedListener(mSubtotalTotalWatcher);
        etSubtotal.addTextChangedListener(mNSubtotalWatcher);
        etTPS.addTextChangedListener(mSubtotalTotalWatcher);
        etTVQ.addTextChangedListener(mSubtotalTotalWatcher);
        etTVH.addTextChangedListener(mSubtotalTotalWatcher);
        etTotal.addTextChangedListener(mNTotalWatcher);

        Intent i = getIntent();
        if (i.hasExtra(EXTRA_PRODUCT)) {
            ProductContent.ProductItem pi = (ProductContent.ProductItem) i.getExtras().get(EXTRA_PRODUCT);

            etName.setText(pi.Name);
            etDescription.setText(pi.Description);
            etPrice.setText(String.valueOf(pi.Price));
            etQuantity.setText(String.valueOf(pi.Quantity));
            etTVH.setText(String.valueOf(pi.TVA));
            etTPS.setText(String.valueOf(pi.TPS));
            etTVQ.setText(String.valueOf(pi.TVQ));
            BigDecimal subtotal = BigDecimal.valueOf(pi.Price).multiply(BigDecimal.valueOf(pi.Quantity));
            BigDecimal total = subtotal
                    .add(subtotal.multiply(BigDecimal.valueOf(pi.TPS)).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP))
                    .add(subtotal.multiply(BigDecimal.valueOf(pi.TVQ)).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP))
                    .add(subtotal.multiply(BigDecimal.valueOf(pi.TVA)).divide(BigDecimal.valueOf(100),mScale,BigDecimal.ROUND_HALF_UP));
            etTotal.setText(String.valueOf(total.doubleValue()));
        }
    }

    public void saveProduct(View pView) {
        if (etName.getText().toString().equals("") || etPrice.getText().toString().equals("")|| etQuantity.getText().toString().equals("") ||
                etTVH.getText().toString().equals("") || etTPS.getText().toString().equals("") || etTVQ.getText().toString().equals("")){
            if (etName.getText().length() == 0) {
                etName.setHintTextColor(Color.RED);
            } else {
                etName.setHintTextColor(Color.GRAY);
            }
            if (etPrice.getText().length() == 0) {
                etPrice.setHintTextColor(Color.RED);
            } else {
                etPrice.setHintTextColor(Color.GRAY);
            }
            if (etQuantity.getText().length() == 0) {
                etQuantity.setHintTextColor(Color.RED);
            } else {
                etQuantity.setHintTextColor(Color.GRAY);
            }
            if (etTVH.getText().length() == 0) {
                etTVH.setHintTextColor(Color.RED);
            } else {
                etTVH.setHintTextColor(Color.GRAY);
            }
            if (etTPS.getText().length() == 0) {
                etTPS.setHintTextColor(Color.RED);
            } else {
                etTPS.setHintTextColor(Color.GRAY);
            }
            if (etTVQ.getText().length() == 0) {
                etTVQ.setHintTextColor(Color.RED);
            } else {
                etTVQ.setHintTextColor(Color.GRAY);
            }

            Toast.makeText(this.getApplicationContext(),getString(R.string.missing_value),Toast.LENGTH_LONG).show();
        } else {
            Product product = new Product();
            double quantity = Double.parseDouble(etQuantity.getText().toString());
            if (getIntent().hasExtra(EXTRA_PRODUCT) && getIntent().getExtras().get(EXTRA_PRODUCT) != null) {
                int containsCount = 0;
                ProductContent.ProductItem pi = (ProductContent.ProductItem) getIntent().getExtras().get(EXTRA_PRODUCT);
                product.Id = (pi.Id != null)?Integer.parseInt(pi.Id):null;
                if (product.Id != null) {
                    ProductContent.removeItem(String.valueOf(product.Id));
                    Product p = service.Products.getByKey(product.Id);
                    for (Invoice i : service.Invoices.getAll()) {
                        for (ShopProduct sp : i.Products) {
                            if (sp.Product.Id.equals(p.Id)) containsCount++;
                        }
                    }
                    if (containsCount > 1) {
                        product.Id = null;
                    }
                }
                else
                    ProductContent.ITEMS.remove(pi);
            }
            product.Name = etName.getText().toString();
            product.Description = etDescription.getText().toString();
            product.Price = Double.parseDouble(etPrice.getText().toString());
            product.TVH = Double.parseDouble(etTVH.getText().toString());
            product.TPS = Double.parseDouble(etTPS.getText().toString());
            product.TVP = Double.parseDouble(etTVQ.getText().toString());

            //Check if contains this product
            Integer key = product.Id;
            product.Id = null;
            if (ProductContent.contains(product)) {
                List<ProductContent.ProductItem> delete = new ArrayList<>();
                for (ProductContent.ProductItem pi2 : ProductContent.ITEMS) {
                    Product p2 = ProductContent.ProductItem.convert(pi2);
                    p2.Id = null;
                    if (product.equals(p2)) {
                        quantity += pi2.Quantity;
                        delete.add(pi2);
                    }
                }
                for (ProductContent.ProductItem pi2 : delete) {
                    ProductContent.removeItem(pi2);
                }
            }
            Integer key2;
            if ((key2 = service.Products.getKey(product)) != null) {
                product.Id = key2;
            } else {
                product.Id = key;
            }
            ShopProduct sp = ShopProduct.createFromProduct(product, quantity);
            ProductContent.addItem(new ProductContent.ProductItem(sp));
            finish();
        }
    }
}
