package ppitol.exd.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import lombok.Getter;
import ppitol.exd.app.model.CurrencyModel;

/**
 * Custom component view for a currency.
 */
public class CurrencyView extends RelativeLayout {

    private static final String TAG = CurrencyView.class.getSimpleName();

    private final ImageView flagView;
    private final TextView currencyView;
    private final TextView countryView;

    @Getter
    private CurrencyModel currency;

    public CurrencyView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_currency, this, true);

        flagView = (ImageView) findViewById(R.id.imageViewFlag);
        currencyView = (TextView) findViewById(R.id.textViewCurrency);
        countryView = (TextView) findViewById(R.id.textViewCountry);
    }

    public CurrencyView(Context context) {
        this(context, null);
    }

    public void setCurrency(CurrencyModel currency) {
        this.currency = currency;
        String currencyText = "";
        String countryName = "";
        Drawable flag = null;
        if (currency != null) {
            currencyText = currency.currencyText;
            countryName = currency.countryName + " (" + currency.countryCode + ")";
            try {
                String flagName = currency.countryCode.toLowerCase() + ".png";
                InputStream flagStream = getContext().getAssets().open(flagName);
                if (flagStream != null) {
                    flag = Drawable.createFromStream(flagStream, flagName);
                }
            } catch (IOException e) {
                Log.d(TAG, "Can't find flag for " + currency.countryName + " (" + currency.countryCode + ")");
            }
        }
        currencyView.setText(currencyText);
        countryView.setText(countryName);
        flagView.setBackground(flag);
    }
}
