package com.proggroup.areasquarecalculator;

import android.app.Application;
import android.content.SharedPreferences;

import com.proggroup.areasquarecalculator.db.SQLiteHelper;

public class InterpolationCalculator extends Application {

    private static InterpolationCalculator instance;

    public static InterpolationCalculator getInstance() {
        return instance;
    }

    private SQLiteHelper SQLiteHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        SQLiteHelper = new SQLiteHelper(getApplicationContext());
    }

    public SQLiteHelper getSqLiteHelper() {
        return SQLiteHelper;
    }

    public SharedPreferences getSharedPreferences() {
        return instance.getSharedPreferences("prefs", MODE_PRIVATE);
    }
}
