package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.HistoricQuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.service.ConnectivityService;
import com.sam_chordas.android.stockhawk.service.ConnectivitySlave;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ConnectivitySlave {
    private static final String LOG_TAG = "SDAF";
    public static final int STOCK_LOADER = 1;
    public static final String DETAIL_URI = "STOCKDETAILURI";
    private Uri mUri;
    private FragmentHolder fragmentHolder;
    private Intent mServiceIntent;

    private static final String[] STOCK_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            QuoteColumns.BIDPRICE,
            QuoteColumns.CREATED
    };

    private static final int COL_SYMBOL = 0;
    private static final int COL_BIDPRICE = 1;
    private static final int COL_CHANGE = 2;
    private static final int COL_CREATED = 3;
    private static final int COL_ISCURRENT = 4;
    private static final int COL_ISUP = 5;
    private static final int COL_PERCENT_CHANGE = 6;

    private String stock;
    private boolean isUp;
    private String change;
    private String bid;
    private boolean isConnected;
    private boolean mServiceInitialised = false;

    public StockDetailActivityFragment() {
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public void setIsUp(boolean isUp) {
        this.isUp = isUp;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public void setChange(String change) {
        this.change = change;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("stock", stock);
        outState.putString("bid", bid);
        outState.putString("change", change);
        outState.putBoolean("isUp", isUp);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w(LOG_TAG, "onCreateView");
        Bundle args = getArguments();
        if(args != null) {
            mUri = args.getParcelable(DETAIL_URI);
            Log.i(LOG_TAG, "uri found of " + mUri.toString());
        }

        ConnectivityService.addSlave(this);
        isConnected = ConnectivityService.isConnected(getContext());
        if(!isConnected) {
            Log.w(LOG_TAG, "Not connected so starting connectivity listener");
            ConnectivityService.startListening(getContext());
        }
        else
            Log.w(LOG_TAG, "Connected");

        View rootView = inflater.inflate(R.layout.fragment_stock_detail, container, false);
        if(fragmentHolder == null)
            fragmentHolder = new FragmentHolder(rootView, getContext());

        mServiceIntent = new Intent(getContext(), StockIntentService.class);
        if(savedInstanceState != null) {
            stock = savedInstanceState.getString("stock");
            bid = savedInstanceState.getString("bid");
            change = savedInstanceState.getString("change");
            isUp = savedInstanceState.getBoolean("isUp");
            Log.i(LOG_TAG, "restoring values " + stock + ":" + bid);
        }
        else {
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "historical");
            mServiceIntent.putExtra("symbol", stock);
            if (isConnected){
                mServiceInitialised = true;
                getContext().startService(mServiceIntent);
            } else{
                networkToast();
            }
        }
        if(stock != null) {
            fragmentHolder.setBid(bid);
            fragmentHolder.setChange(change);
            fragmentHolder.setText(stock);

            Log.i(LOG_TAG, "stock set to " + stock);
            int sdk = Build.VERSION.SDK_INT;
            if (isUp){
                if (sdk < Build.VERSION_CODES.JELLY_BEAN){
                    fragmentHolder.setChangeBackgroundDrawable(
                            getActivity().getResources().getDrawable(R.drawable.percent_change_pill_green));
                }else {
                    fragmentHolder.setChangeBackground(
                            getActivity().getResources().getDrawable(R.drawable.percent_change_pill_green));
                }
            } else{
                if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                    fragmentHolder.setChangeBackgroundDrawable(
                            getActivity().getResources().getDrawable(R.drawable.percent_change_pill_red));
                } else{
                    fragmentHolder.setChangeBackground(
                            getActivity().getResources().getDrawable(R.drawable.percent_change_pill_red));
                }
            }


        }
        return rootView;
    }

    public void networkToast(){
        Toast.makeText(getActivity(), getActivity().getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        Log.w(LOG_TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Inside onActivityCreated");
        getLoaderManager().initLoader(STOCK_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id == STOCK_LOADER) {
            if (mUri != null) {
                return new CursorLoader(getActivity(), mUri,
                        null,
                        null,
                        null,
                        HistoricQuoteColumns.DATE + " asc");
            }
        }
        return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        while(data.moveToNext()) {
//            for(int i = 0; i < data.getColumnCount(); ++i) {
//                Log.i(LOG_TAG, "Column " + data.getColumnName(i) + " value " + data.getString(i));
//            }
//        }
        Log.w(LOG_TAG, "in onLoadFinished with " + loader.getId() + " and " + data.getCount() + " data entries");
        if(loader.getId() == STOCK_LOADER)
            fragmentHolder.tryLineChart(data, bid, stock);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void wakeup() {
        Log.w(LOG_TAG, "Waking up");
        isConnected = true;
        getLoaderManager().restartLoader(STOCK_LOADER, null, this);
        if(!mServiceInitialised) {
            mServiceInitialised = true;
            getContext().startService(mServiceIntent);
        }
    }
}
