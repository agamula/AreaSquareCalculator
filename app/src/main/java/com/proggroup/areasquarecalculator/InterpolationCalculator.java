package com.proggroup.areasquarecalculator;

import android.app.Application;

public class InterpolationCalculator extends Application {

    private static InterpolationCalculator instance;

    public static InterpolationCalculator getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
