package ppitol.exd.app;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import ppitol.exd.app.model.CurrencyModel;

/**
 * Adapter for the list of currencies.
 */
public class CurrencyAdapter extends ArrayAdapter<CurrencyModel> {

    public CurrencyAdapter(Context context, List<CurrencyModel> currencies) {
        super(context, R.layout.currency_layout, currencies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CurrencyView currencyView = (CurrencyView) convertView;
        if (currencyView == null) {
            currencyView = new CurrencyView(parent.getContext());
        }

        currencyView.setCurrency(getItem(position));
        return currencyView;
    }
}
