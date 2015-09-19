package com.proggroup.areasquarecalculator.data;

import android.provider.BaseColumns;

public class Project {
    public static final int SIMPLE_MEASURE_AVG_POINTS_COUNT = 4;

    public static final String TABLE_NAME = "line_data";
    public static final String IS_SIMPLE_MEASURE = "_is_simple";
    public static final String ID = BaseColumns._ID;

    private boolean isSimpleMeasure;
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setIsSimpleMeasure(boolean isSimpleMeasure) {
        this.isSimpleMeasure = isSimpleMeasure;
    }

    public boolean isSimpleMeasure() {
        return isSimpleMeasure;
    }
}
