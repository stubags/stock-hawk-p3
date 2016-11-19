package com.sam_chordas.android.stockhawk.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockHawkWidgetService;

/**
 * Created by stuartwhitcombe on 11/11/2016.
 */
public class StockHawkWidgetProvider extends AppWidgetProvider {
    public static final String LAUNCH_SH = "com.sam_chordas.android.stockhawk.LAUNCH_SH_ACTION";
    public static final String EXTRA_STOCK = "com.sam_chordas.android.stockhawk.EXTRA_STOCK";
    public static final String EXTRA_CHANGE = "com.sam_chordas.android.stockhawk.EXTRA_CHANGE";
    public static final String EXTRA_BID = "com.sam_chordas.android.stockhawk.EXTRA_BID";
    public static final String EXTRA_ISUP = "com.sam_chordas.android.stockhawk.EXTRA_ISUP";

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(LAUNCH_SH)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String stock = intent.getStringExtra(EXTRA_STOCK);
            String change = intent.getStringExtra(EXTRA_CHANGE);
            String bid = intent.getStringExtra(EXTRA_BID);
            boolean isUp = intent.getBooleanExtra(EXTRA_ISUP, false);
            Intent newIntent = new Intent(context, StockDetailActivity.class);
            newIntent.setData(QuoteProvider.HistoricQuotes.CONTENT_URI.buildUpon().appendPath(stock.toString()).build());
            newIntent.putExtra(context.getString(R.string.extras_stock), stock);
            newIntent.putExtra(context.getString(R.string.extras_change), change);
            newIntent.putExtra(context.getString(R.string.extras_bid), bid);
            newIntent.putExtra(context.getString(R.string.extras_isup), isUp);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(newIntent);
            //Toast.makeText(context, "Touched view " + viewIndex, Toast.LENGTH_SHORT).show();
        }
        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // update each of the app widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            // Set up the intent that starts the StackViewService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, StockHawkWidgetService.class);
            // Add the app widget ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.stockhawk_appwidget);
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects
            // to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            rv.setRemoteAdapter(appWidgetIds[i], R.id.stocklist_view, intent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            rv.setEmptyView(R.id.stocklist_view, R.id.empty_view);

            // This section makes it possible for items to have individualized behavior.
            // It does this by setting up a pending intent template. Individuals items of a collection
            // cannot set up their own pending intents. Instead, the collection as a whole sets
            // up a pending intent template, and the individual items set a fillInIntent
            // to create unique behavior on an item-by-item basis.
            Intent toastIntent = new Intent(context, StockHawkWidgetProvider.class);
            // Set the action for the intent.
            // When the user touches a particular view, it will have the effect of
            // broadcasting TOAST_ACTION.
            toastIntent.setAction(LAUNCH_SH);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            // When you call setPendingIntentTemplate(...), the id should be that of the AdapterView (in this case the ListView)
            rv.setPendingIntentTemplate(R.id.stocklist_view, toastPendingIntent);

            //
            // Do additional processing specific to this app widget...
            //

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
