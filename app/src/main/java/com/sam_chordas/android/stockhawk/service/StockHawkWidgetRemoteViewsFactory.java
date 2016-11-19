package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Quote;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.StockHawkWidgetProvider;

import java.util.ArrayList;

/**
 * Created by stuartwhitcombe on 13/11/2016.
 */
public class StockHawkWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Cursor mCursor;
    private ArrayList<Quote> quotes = new ArrayList<Quote>();
    private String LOG_TAG = "SHWRVF";
    private Context mContext;

    public StockHawkWidgetRemoteViewsFactory(Context context) {
        this.mContext = context;
    }
    @Override
    public void onCreate() {
        // do nothing as done in onDataSetChanged
//        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, null, QuoteColumns.ISCURRENT + "=1", null, null);
    }

    @Override
    public void onDataSetChanged() {
        final long identity = Binder.clearCallingIdentity();
        Log.i(LOG_TAG, "onDataSetChanged");
        if(mCursor != null) {
            mCursor.close();
        }
        quotes.clear();
        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, null, QuoteColumns.ISCURRENT + "=1", null, null);
        while(mCursor.moveToNext()) {
            Quote quote = new Quote(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)),
                mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)),
                mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE)),
                mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE)),
                mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CREATED)),
                mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISCURRENT)),
                mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP))
                );
            quotes.add(quote);
        }
        Binder.restoreCallingIdentity(identity);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return quotes.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.i(LOG_TAG, "getViewAt " + position);
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.rv_item_quote);
        Quote quote = quotes.get(position);
        rv.setTextViewText(R.id.stock_symbol, quote.getSymbol());
        rv.setTextColor(R.id.stock_symbol, quote.isUp() ? Color.GREEN : Color.RED);
        rv.setTextViewText(R.id.bid_price, quote.getBid());
        rv.setTextColor(R.id.bid_price, quote.isUp() ? Color.GREEN : Color.RED);
        // Next, set a fill-intent, which will be used to fill in the pending intent template
        // that is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putString(StockHawkWidgetProvider.EXTRA_STOCK, quote.getSymbol());
        extras.putString(StockHawkWidgetProvider.EXTRA_BID, quote.getBid());
        extras.putString(StockHawkWidgetProvider.EXTRA_CHANGE, quote.getChange());
        extras.putBoolean(StockHawkWidgetProvider.EXTRA_ISUP, quote.isUp());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        // Make it possible to distinguish the individual on-click
        // action of a given item

        // When you call setOnClickFillInIntent(...), the id should be that of the root View of the item layout
        rv.setOnClickFillInIntent(R.id.widget_quote_item, fillInIntent);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
