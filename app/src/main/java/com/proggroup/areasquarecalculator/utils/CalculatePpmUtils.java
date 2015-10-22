package com.proggroup.areasquarecalculator.utils;

import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;
import android.widget.Toast;

import com.proggroup.areasquarecalculator.InterpolationCalculator;
import com.proggroup.areasquarecalculator.adapters.CalculatePpmSimpleAdapter;
import com.proggroup.areasquarecalculator.data.Constants;
import com.proggroup.areasquarecalculator.data.Project;
import com.proggroup.areasquarecalculator.db.AvgPointHelper;
import com.proggroup.areasquarecalculator.db.ProjectHelper;
import com.proggroup.areasquarecalculator.db.SQLiteHelper;

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

    public static final String CSV_COL_DELiM = ",";

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
                String splitValues[] = s.split(CSV_COL_DELiM);
                ppmValues.add(Float.parseFloat(splitValues[0]));
                avgSquareValues.add(Float.parseFloat(splitValues[splitValues.length - 1]));
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
     * Save data into csv.
     *
     * @param adapter Adapter, from which report is creating
     * @param numColumns Count columns in table.
     * @param path Path to folder for save values.
     * @return Result: true - for success, false - for fail.
     */
    public static boolean saveAvgValuesToFile(CalculatePpmSimpleAdapter adapter, int numColumns, String
            path, boolean save0Ppm) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                    (path)));

            SQLiteHelper helper = InterpolationCalculator.getInstance().getSqLiteHelper();
            int numRows = adapter.getCount() / numColumns - 1;
            SQLiteDatabase writeDb = helper.getWritableDatabase();
            Project project = new ProjectHelper(writeDb).getProjects().get(0);
            AvgPointHelper helper1 = new AvgPointHelper(writeDb, project);
            List<Long> avgids = helper1.getAvgPoints();

            List<List<Float>> squareValues = adapter.getSquareValues();
            List<Float> avgValues = adapter.getAvgValues();

            if(save0Ppm) {
                writer.write("0");
                writer.write(CSV_COL_DELiM);
                writer.write("0");
                writer.write(CSV_COL_DELiM);
                writer.write("0");
                writer.newLine();
            }

            for (int i = 0; i < numRows; i++) {
                writer.write((int)helper1.getPpmValue(avgids.get(i)) + "");
                writer.write(CSV_COL_DELiM);
                List<Float> squareVas = squareValues.get(i);
                for (Float squareVal : squareVas) {
                    if(squareVal != 0f) {
                        writer.write(FloatFormatter.format(squareVal));
                    }
                    writer.write(CSV_COL_DELiM);
                }
                writer.write(FloatFormatter.format(avgValues.get(i)));
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
