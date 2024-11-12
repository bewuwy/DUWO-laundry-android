package dev.bewu.duwolaundry;

import android.app.Application;

public class LaundryApplication extends Application {
    private MultiPossScraper multiPossScraper = null;

    public MultiPossScraper getMultiPossScraper() {
        return multiPossScraper;
    }

    public void setMultiPossScraper(MultiPossScraper multiPossScraper) {
        this.multiPossScraper = multiPossScraper;
    }
}
