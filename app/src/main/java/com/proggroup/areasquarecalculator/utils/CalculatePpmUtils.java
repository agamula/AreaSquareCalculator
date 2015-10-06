package com.proggroup.areasquarecalculator.utils;

import android.util.Pair;
import android.widget.Toast;

import com.proggroup.areasquarecalculator.InterpolationCalculator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class CalculatePpmUtils {

    private CalculatePpmUtils() {
    }

    /**
     * Parse ppm and average square values.
     *
     * @param path Path to csv file average values of ppm, square must be loaded from.
     * @return List of ppm, and list of average square values in pair.
     */
    public static Pair<List<Float>, List<Float>> parseAvgValuesFromFile(String path) {
        List<Float> ppmValues = new ArrayList<>();
        List<Float> avgSquareValues = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                    (new File(path)))));

            for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                String splitValues[] = s.split(ReportCreator.CSV_COL_DELiM);
                ppmValues.add(Float.parseFloat(splitValues[0]));
                avgSquareValues.add(Float.parseFloat(splitValues[1]));
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(InterpolationCalculator.getInstance().getApplicationContext(), "Write " +
                    "failed", Toast.LENGTH_LONG).show();
        }
        return new Pair<>(ppmValues, avgSquareValues);
    }

    /**
     *
     * @param ppmValues PPm known values, is actually filled from table.
     * @param avgSquareValues Average square values, is filled from table.
     * @param path Path to folder for save values.
     * @return Result: true - for success, false - for fail.
     */
    public static boolean saveAvgValuesToFile(List<Float> ppmValues, List<Float> avgSquareValues, String
            path) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                    (new File(path))));

            for (int i = 0; i < ppmValues.size(); i++) {
                writer.write(FloatFormatter.format(ppmValues.get(i)));
                writer.write(ReportCreator.CSV_COL_DELiM);
                writer.write(FloatFormatter.format(avgSquareValues.get(i)));
                writer.newLine();
            }
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(InterpolationCalculator.getInstance().getApplicationContext(), "Write " +
                    "failed", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
