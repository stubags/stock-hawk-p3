package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

public class StockDetailActivity extends AppCompatActivity {

    private boolean isConnected;
    private static final String LOG_TAG = "StockDetailActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (savedInstanceState == null) {
            Log.w(LOG_TAG, "savedInstanceState is null");
            Bundle args = new Bundle();
            args.putParcelable(StockDetailActivityFragment.DETAIL_URI, getIntent().getData());
            StockDetailActivityFragment detailFragment = new StockDetailActivityFragment();
            detailFragment.setArguments(args);
            detailFragment.setStock(getIntent().getStringExtra(getString(R.string.extras_stock)));
            detailFragment.setBid(getIntent().getStringExtra(getString(R.string.extras_bid)));
            detailFragment.setIsUp(getIntent().getBooleanExtra(getString(R.string.extras_isup), false));
            detailFragment.setChange(getIntent().getStringExtra(getString(R.string.extras_change)));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.stock_detail_container, detailFragment)
                    .commit();

        }
        else
            Log.w(LOG_TAG, "savedInstanceState is not null");

    }

    @Override
    public void onResume() {
        Log.w(LOG_TAG, "onResume");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stock_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
