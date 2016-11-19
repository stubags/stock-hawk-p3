package com.sam_chordas.android.stockhawk.ui;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.ConnectivityService;
import com.sam_chordas.android.stockhawk.service.ConnectivitySlave;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class StocksActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ConnectivitySlave {
    public static final String LOG_TAG = "StocksFragment";
    private static final int CURSOR_LOADER_ID = 0;
    private QuoteCursorAdapter mCursorAdapter;
    private Intent mServiceIntent;
    private ItemTouchHelper mItemTouchHelper;
    private Cursor mCursor;
    boolean mIsConnected;
    Map<String, Boolean> isUpByStock = new HashMap<>();
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private boolean mIntentServiceInitialised = false;
    private boolean mStartedPeriodicTask = false;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    public StocksActivityFragment() {
    }

    public void networkToast(){
        Toast.makeText(getContext(), getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    public void restoreActionBar() {
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.my_stocks, menu);
        restoreActionBar();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stocks, container, false);

        setHasOptionsMenu(true);
        ConnectivityService.addSlave(this);
        mIsConnected = ConnectivityService.isConnected(getContext());
        if(!mIsConnected) {
            Log.w(LOG_TAG, "Not connected so starting connectivity listener");
            ConnectivityService.startListening(getContext());
        }
        else
            Log.w(LOG_TAG, "Connected");

        // The intent service is for executing immediate pulls from the Yahoo API
        // GCMTaskService can only schedule tasks, they cannot execute immediately
        mServiceIntent = new Intent(getActivity(), StockIntentService.class);
        if (savedInstanceState == null){
            // Run the initialize task service so that some stocks appear upon an empty database
            mServiceIntent.putExtra("tag", "init");
            if (mIsConnected){
                mIntentServiceInitialised = true;
                getActivity().startService(mServiceIntent);
            } else{
                networkToast();
            }
        }
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        mEmptyView = (TextView)rootView.findViewById(R.id.empty_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        mCursorAdapter = new QuoteCursorAdapter(getActivity(), null);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(),
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View v, int position) {
                        //TODO:
                        if (((Callback)getActivity()).isTwoPane()) {
                            String stock = ((TextView)v.findViewById(R.id.stock_symbol)).getText().toString();
                            String change = ((TextView)v.findViewById(R.id.change)).getText().toString();
                            String bidPrice = ((TextView)v.findViewById(R.id.bid_price)).getText().toString();

                            createStockDetailFragment(stock, bidPrice, change);
                        }
                        else {
                            Intent intent = new Intent(getActivity(), StockDetailActivity.class);
                            mCursorAdapter.getItemId(position);
                            TextView stockView = (TextView)v.findViewById(R.id.stock_symbol);
                            TextView changeView = (TextView)v.findViewById(R.id.change);
                            TextView bidPriceView = (TextView)v.findViewById(R.id.bid_price);

                            if(stockView != null) {
                                // pass historical query string, plus all the non-historical stuff...
                                CharSequence stock = stockView.getText();
                                intent.setData(QuoteProvider.HistoricQuotes.CONTENT_URI.buildUpon().appendPath(stock.toString()).build());
                                intent.putExtra(getString(R.string.extras_stock), stock.toString());
                                intent.putExtra(getString(R.string.extras_change), changeView.getText().toString());
                                intent.putExtra(getString(R.string.extras_bid), bidPriceView.getText().toString());
                                intent.putExtra(getString(R.string.extras_isup), isUpByStock.get(stock.toString()));
                                startActivity(intent);
                            }
                        }

                    }
                }));
        mRecyclerView.setAdapter(mCursorAdapter);


        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.attachToRecyclerView(mRecyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mIsConnected){
                    new MaterialDialog.Builder(getActivity()).title(R.string.symbol_search)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
                                    Cursor c = getActivity().getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                                            new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ?",
                                            new String[] { input.toString() }, null);
                                    if (c.getCount() != 0) {
                                        Toast toast =
                                                Toast.makeText(getActivity(), "This stock is already saved!",
                                                        Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                                        toast.show();
                                        return;
                                    } else {
                                        // Add the stock to DB
                                        mServiceIntent.putExtra("tag", "add");
                                        mServiceIntent.putExtra("symbol", input.toString());
                                        getActivity().startService(mServiceIntent);
                                    }
                                }
                            })
                            .show();
                } else {
                    networkToast();
                }

            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mTitle = getActivity().getTitle();
        if (mIsConnected){
            mStartedPeriodicTask = true;
            startPeriodicWidgetUpdate();
        }

        return rootView;
    }

    private void startPeriodicWidgetUpdate() {
        long period = 60L;
        long flex = 10L;
        String periodicTag = "periodic";

        // create a periodic task to pull stocks once every hour after the app has been opened. This
        // is so Widget data stays up to date.
        PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setService(StockTaskService.class)
                .setPeriod(period)
                .setFlex(flex)
                .setTag(periodicTag)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .build();
        // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
        // are updated.
        GcmNetworkManager.getInstance(getActivity()).schedule(periodicTask);
    }

    private void createStockDetailFragment(String stock, String bidPrice, String change) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(StockDetailActivityFragment.DETAIL_URI, QuoteProvider.HistoricQuotes.CONTENT_URI.buildUpon().appendPath(stock.toString()).build());

        StockDetailActivityFragment detailFragment = new StockDetailActivityFragment();
        detailFragment.setArguments(bundle);
        detailFragment.setStock(stock);
        detailFragment.setBid(bidPrice);
        detailFragment.setChange(change);
        detailFragment.setIsUp(isUpByStock.get(stock));
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.stock_detail_container, detailFragment, StocksActivity.STOCK_DETAIL_FRAGMENT_TAG).commitAllowingStateLoss();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        // This narrows the return to only the stocks that are most current.
        if(id == CURSOR_LOADER_ID)
            return new CursorLoader(getActivity(), QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        Log.w(LOG_TAG, "Load finished with " + data.getCount());
        if(loader.getId() == CURSOR_LOADER_ID) {
            mCursorAdapter.swapCursor(data);
            mCursor = data;
            if (mCursor.moveToFirst()) {
                mRecyclerView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
                String stock = mCursor.getString(1);
                String bidPrice = mCursor.getString(2);
                String change = mCursor.getString(Utils.showPercent ? 3 : 4);

                boolean isUp = mCursor.getInt(5) == 1;
                isUpByStock.put(stock, isUp);

                if (((Callback) getActivity()).isTwoPane())
                    createStockDetailFragment(stock, bidPrice, change);
                while (mCursor.moveToNext()) {
                    // get the stock information for all entries returned...
                    stock = mCursor.getString(1);
                    isUp = mCursor.getInt(5) == 1;
                    isUpByStock.put(stock, isUp);
                }
                ComponentName name = new ComponentName(getContext(), StockHawkWidgetProvider.class);
                int[] appWidgetIds = AppWidgetManager.getInstance(getContext()).getAppWidgetIds(name);
                AppWidgetManager.getInstance(getContext()).notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stocklist_view);
            } else {
                mRecyclerView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                ConnectivityManager cm =
                        (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                mIsConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();
                if (!mIsConnected) {
                    mEmptyView.setText(getString(R.string.empty_view_text_no_internet));
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean isConnected() {
        return mIsConnected;
    }

    @Override
    public void wakeup() {
        Log.w(LOG_TAG, "Waking up");
        mIsConnected = true;
        if(!mIntentServiceInitialised) {
            getActivity().startService(mServiceIntent);
            mIntentServiceInitialised = true;
        }
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        if(!mStartedPeriodicTask) {
            mStartedPeriodicTask = true;
            startPeriodicWidgetUpdate();
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public boolean isTwoPane();
    }

}
