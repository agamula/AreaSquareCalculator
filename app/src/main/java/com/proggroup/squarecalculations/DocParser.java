package com.proggroup.squarecalculations;

import android.graphics.PointF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DocParser {

    public static List<PointF> parse(File f) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        List<PointF> points = new ArrayList<>();

        if (reader != null) {
            String s = null;
            try {

                long lastTime = -1l;

                while ((s = reader.readLine()) != null) {
                    String[] values = s.split(",");
                    if(values.length >= 2) {
                        long parseTime = parseTime(values[0]);
                        if(lastTime != parseTime) {
                            points.add(new PointF(parseTime, Float.parseFloat(values[1])));
                            lastTime = parseTime;
                        }
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return points;
    }

    private static long parseTime(String time) {
        String[] splitValues = time.split(":");

        int minutes = Integer.parseInt(splitValues[0]);

        String secondMillisVal = splitValues[1];

        int seconds = Integer.parseInt(secondMillisVal.substring(0, secondMillisVal.indexOf('.')));

        return TimeUnit.MINUTES.toSeconds(minutes) + TimeUnit.SECONDS.toSeconds(seconds);
    }
}
