package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockDetailActivity;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

/**
 * Created by dh on 17-2-10.
 */

public class LargeWidgetRemoteViewsService extends RemoteViewsService {

    private final String TAG = "RemoteViewsService";
    private DecimalFormat dollarFormatWithPlus;
    private DecimalFormat dollarFormat;
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {


        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                //Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI, Contract.Quote.QUOTE_COLUMNS, null, null, Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return ((data == null) ? 0 : data.getCount());
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION || data == null ||
                        !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_large_item);

                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                float price = data.getFloat(Contract.Quote.POSITION_PRICE);
                float change = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");

                /*
                if (price > 0) {
                    views.setTextColor(R.id.widget_change, getResources().getColor(R.color.material_green_700));
                } else {
                    views.setTextColor(R.id.widget_change, getResources().getColor(R.color.material_red_700));
                }*/

                views.setTextViewText(R.id.widget_symbol, symbol);
                views.setTextViewText(R.id.widget_price, dollarFormat.format(price));
                views.setTextViewText(R.id.widget_change, dollarFormatWithPlus.format(change));

                final Intent fillIntent = new Intent(getApplicationContext(), StockDetailActivity.class);
                fillIntent.putExtra("symbol", symbol);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_large_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position)) {
                    return data.getInt(Contract.Quote.POSITION_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
