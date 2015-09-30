package ppitol.exd.app;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import ppitol.exd.app.model.CurrencyModel;

/**
 * Let the user choose a currency.
 */
public class CurrencyListActivity extends ListActivity {

    static final String CURRENCY_LIST = "CurrencyListActivity.currencyList";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<CurrencyModel> currencies = (List<CurrencyModel>) getIntent().getSerializableExtra(CURRENCY_LIST);
        CurrencyAdapter adapter = new CurrencyAdapter(this, currencies);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        CurrencyModel currency = (CurrencyModel) l.getItemAtPosition(position);
        Intent result = new Intent();
        result.putExtra(CurrencyModel.class.getSimpleName(), currency);
        setResult(RESULT_OK, result);
        finish();
    }
}
