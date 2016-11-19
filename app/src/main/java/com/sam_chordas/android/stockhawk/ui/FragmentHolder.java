package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.sam_chordas.android.stockhawk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stuartwhitcombe on 21/10/2016.
 */
public class FragmentHolder {
    private static final String LOG_TAG = "Frag";
    private TextView textV;
    private TextView bidV;
    private TextView changeV;

    private LineChart chart;
    private Context context;

    public FragmentHolder(View rootView, Context context) {
        chart = (LineChart)rootView.findViewById(R.id.linechart);
        chart.setNoDataText("");
        textV = (TextView)rootView.findViewById(R.id.stock_symbol);
        bidV = (TextView)rootView.findViewById(R.id.bid_price);
        changeV = (TextView)rootView.findViewById(R.id.change);
        this.context = context;
    }

    public void setText(String text) {
        textV.setText(text);

        chart.setContentDescription(context.getString(R.string.graphCD) + text);
    }

    public void setBid(String bid) {
        bidV.setText(bid);
    }

    public void setChange(String change) {
        changeV.setText(change);
    }

    public void setChangeBackground(Drawable d) {
        changeV.setBackground(d);
    }

    public void setChangeBackgroundDrawable(Drawable d) {
        changeV.setBackgroundDrawable(d);
    }

    public void tryLineChart(Cursor data, String currentBid, String stock) {

//        Data LineSet;
        //lineChartView.setAxisBorderValues(0,100,1);
        List<Entry> entries = new ArrayList<Entry>();
        XAxisValueFormatter vf = new XAxisValueFormatter();
        chart.clear();

        if(data.getCount() > 0) {
            int dayCount = 0;
            data.moveToFirst();
            vf.setValue(dayCount, data.getString(3));
            // turn your data into Entry objects
            entries.add(new Entry((float)dayCount, Float.parseFloat(data.getString(2))));
            dayCount++;

            while (data.moveToNext() && dayCount < 7) {
                vf.setValue(dayCount, data.getString(3));
                // turn your data into Entry objects
                entries.add(new Entry((float)dayCount, Float.parseFloat(data.getString(2))));
                dayCount++;
            }
            // add today's entry...
            if(currentBid != null) {
                try {
                    entries.add(new Entry((float) dayCount, Float.parseFloat(currentBid)));
                    vf.setValue(dayCount, "Today");
                } catch (NumberFormatException nfe) {
                    Log.e(LOG_TAG, "Current bid duff, ignoring");
                }
            }
            LineDataSet lds = new LineDataSet(entries, stock);
            lds.setColor(Color.RED);
            lds.setValueTextColor(Color.RED);
            LineData ld = new LineData(lds);
            XAxis xAxis = chart.getXAxis();
            xAxis.setValueFormatter(vf);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setLabelRotationAngle(45);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            YAxis lAxis = chart.getAxisLeft();
            lAxis.setTextColor(Color.WHITE);
            YAxis rAxis = chart.getAxisRight();
            rAxis.setTextColor(Color.WHITE);

            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(true);
            chart.getLegend().setEnabled(false);
            chart.setData(ld);
        }
        chart.invalidate();

    }

    public class XAxisValueFormatter implements IAxisValueFormatter {

        private String[] mValues = new String[8];

        public void setValue(int index, String value) {
            mValues[index] = value;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return mValues[(int) value];
        }

        /** this is only needed if numbers are returned, else return 0 */
        @Override
        public int getDecimalDigits() { return 0; }
    }
}
