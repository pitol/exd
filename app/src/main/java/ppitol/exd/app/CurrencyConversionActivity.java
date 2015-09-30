package ppitol.exd.app;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import ppitol.exd.app.model.CurrencyModel;
import ppitol.exd.app.model.CurrencyRepository;
import ppitol.exd.app.rate.RateImport;


public class CurrencyConversionActivity extends Activity implements LoaderManager.LoaderCallbacks<List<CurrencyModel>> {

    private static final String TAG = CurrencyConversionActivity.class.getSimpleName();

    @RequiredArgsConstructor(suppressConstructorProperties = true)
    static class Conversion {
        final CurrencyView sourceView;
        final CurrencyView targetView;
        final EditText source;
        final EditText target;

        private double rate() {
            BigDecimal sourceRate = sourceView.getCurrency() != null ? sourceView.getCurrency().referenceRate : BigDecimal.ONE;
            BigDecimal targetRate = targetView.getCurrency() != null ? targetView.getCurrency().referenceRate : BigDecimal.ONE;
            return sourceRate.doubleValue() / targetRate.doubleValue();
        }

        void convert() {
            // Pretty much no l10n on EditText, see https://code.google.com/p/android/issues/detail?id=2626
            NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
            String val = source.getText().toString();
            if (Strings.isNullOrEmpty(val)) {
                target.setText(val);
            } else {
                try {
                    double sourceValue = Double.valueOf(val);
                    double targetValue = sourceValue * rate();
                    target.setText(fmt.format(targetValue));
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    class CurrencySelectionClickListener implements View.OnClickListener {
        private final int requestCode;

        CurrencySelectionClickListener(int requestCode) {
            this.requestCode = requestCode;
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(CurrencyConversionActivity.this, CurrencyListActivity.class);
            intent.putExtra(CurrencyListActivity.CURRENCY_LIST, (Serializable) currencies);
            CurrencyConversionActivity.this.startActivityForResult(intent, requestCode);
        }
    }

    @RequiredArgsConstructor(suppressConstructorProperties = true)
    class ConversionTextWatcher implements TextWatcher {
        final EditText source;
        final Conversion conversion;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Nothing to do
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Nothing to do
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (source.isFocused()) {
                conversion.convert();
            }
        }
    }

    private static final int SELECT_TOP_CURRENCY = 10;
    private static final int SELECT_BOTTOM_CURRENCY = 20;

    private RateImport rateImport;
    private CurrencyRepository currencyRepository;
    private List<CurrencyModel> currencies;

    private CurrencyView topCurrency;
    private EditText editTextTopValue;
    private CurrencyView bottomCurrency;
    private EditText editTextBottomValue;

    private Conversion topToBottom() {
        return new Conversion(topCurrency, bottomCurrency, editTextTopValue, editTextBottomValue);
    }

    private Conversion bottomToTop() {
        return new Conversion(bottomCurrency, topCurrency, editTextBottomValue, editTextTopValue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_currency_conversion);

        currencyRepository = new CurrencyRepository(this);
        rateImport = new RateImport(this, currencyRepository);
        rateImport.scheduleImportRates();

        getLoaderManager().initLoader(0, null, this);

        topCurrency = (CurrencyView) findViewById(R.id.sourceCurrency);
        editTextTopValue = (EditText) findViewById(R.id.editTextSourceValue);
        bottomCurrency = (CurrencyView) findViewById(R.id.targetCurrency);
        editTextBottomValue = (EditText) findViewById(R.id.editTextTargetValue);

        topCurrency.setOnClickListener(new CurrencySelectionClickListener(SELECT_TOP_CURRENCY));
        bottomCurrency.setOnClickListener(new CurrencySelectionClickListener(SELECT_BOTTOM_CURRENCY));

        topCurrency.setEnabled(false);
        bottomCurrency.setEnabled(false);

        editTextTopValue.addTextChangedListener(new ConversionTextWatcher(editTextTopValue, topToBottom()));
        editTextBottomValue.addTextChangedListener(new ConversionTextWatcher(editTextBottomValue, bottomToTop()));
    }

    @Override
    public Loader<List<CurrencyModel>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");
        return new CurrencyLoader(
                CurrencyConversionActivity.this,
                currencyRepository,
                rateImport);
    }

    @Override
    public void onLoadFinished(Loader<List<CurrencyModel>> loader, List<CurrencyModel> data) {
        Log.d(TAG, "onLoadFinished()");
        currencies = data;
        resetCurrencies();
        topCurrency.setEnabled(true);
        bottomCurrency.setEnabled(true);
    }

    @Override
    public void onLoaderReset(Loader<List<CurrencyModel>> loader) {
        Log.d(TAG, "onLoaderReset()");
        currencies = new ArrayList<>();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            CurrencyModel currency = (CurrencyModel) data.getSerializableExtra(CurrencyModel.class.getSimpleName());
            CurrencyView currencyView;
            Conversion conversion;
            if (requestCode == SELECT_TOP_CURRENCY) {
                currencyView = topCurrency;
                conversion = bottomToTop();
            } else {
                currencyView = bottomCurrency;
                conversion = topToBottom();
            }
            currencyView.setCurrency(currency);
            conversion.convert();
        }
    }

    private void resetCurrencies() {
        resetCurrency(topCurrency, "US");
        resetCurrency(bottomCurrency, "BR");
        topToBottom().convert();
    }

    private void resetCurrency(CurrencyView currencyView, String defaultCountry) {
        String countryCode = currencyView.getCurrency() != null ?
                currencyView.getCurrency().countryCode : defaultCountry;
        Optional<CurrencyModel> currency = Iterables.tryFind(currencies, CurrencyModel.findByCountry(countryCode));
        currencyView.setCurrency(currency.orNull());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.currency_conversion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
