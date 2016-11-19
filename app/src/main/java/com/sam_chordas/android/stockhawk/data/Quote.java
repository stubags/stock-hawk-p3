package com.sam_chordas.android.stockhawk.data;

/**
 * Created by stuartwhitcombe on 13/11/2016.
 */
public class Quote {
    private final String symbol;
    private final String bid;
    private final String change;
    private final String percentageChange;
    private final String created;
    private final boolean isUp;
    private final boolean isCurrent;

    public Quote(String symbol, String bid, String change, String percentChange, String created, int isCurrent, int isUp) {
        this.symbol = symbol;
        this.bid = bid;
        this.change = change;
        this.percentageChange = percentChange;
        this.created = created;
        this.isUp = isUp == 1;
        this.isCurrent = isCurrent == 1;
    }

    public String getBid() {
        return bid;
    }

    public String getChange() {
        return change;
    }

    public String getCreated() {
        return created;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public boolean isUp() {
        return isUp;
    }

    public String getPercentageChange() {
        return percentageChange;
    }

    public String getSymbol() {
        return symbol;
    }


}
