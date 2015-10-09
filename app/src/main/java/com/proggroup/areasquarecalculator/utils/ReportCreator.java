package com.proggroup.areasquarecalculator.utils;

import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.GridView;
import android.widget.Toast;

import com.proggroup.areasquarecalculator.InterpolationCalculator;
import com.proggroup.areasquarecalculator.adapters.CalculatePpmSimpleAdapter;
import com.proggroup.areasquarecalculator.data.Constants;
import com.proggroup.areasquarecalculator.data.Project;
import com.proggroup.areasquarecalculator.db.AvgPointHelper;
import com.proggroup.areasquarecalculator.db.ProjectHelper;
import com.proggroup.areasquarecalculator.db.SQLiteHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class ReportCreator {

    public static final String CSV_COL_DELiM = ",";

    /**
     *
     * @param adapter Adapter, from which report is creating
     * @param numColumns Count columns in table.
     * @param tableName Name of table for saving report.
     * @return Result: true - for success, false - for fail.
     */
    public static boolean createReport(CalculatePpmSimpleAdapter adapter, int numColumns, String
                                    tableName){
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream
                    (new File(Constants.BASE_DIRECTORY, tableName + ".csv"))));

            SQLiteHelper helper = InterpolationCalculator.getInstance().getSqLiteHelper();
            int numRows = adapter.getCount() / numColumns - 1;
            SQLiteDatabase writeDb = helper.getWritableDatabase();
            Project project = new ProjectHelper(writeDb).getProjects().get(0);
            AvgPointHelper helper1 = new AvgPointHelper(writeDb, project);
            List<Long> avgids = helper1.getAvgPoints();

            List<List<Float>> squareValues = adapter.getSquareValues();
            List<Float> avgValues = adapter.getAvgValues();

            for (int i = 0; i < numRows; i++) {
                writer.write(FloatFormatter.format(helper1.getPpmValue(avgids.get(i))));
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
