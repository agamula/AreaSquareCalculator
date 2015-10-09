package com.proggroup.areasquarecalculator.data;

import android.os.Environment;

import java.io.File;

public class Constants {
    public static final File BASE_DIRECTORY = new File(Environment.getExternalStorageDirectory(),
            "AEToC_CAL_FILES");

    static {
        BASE_DIRECTORY.mkdirs();
    }
}
