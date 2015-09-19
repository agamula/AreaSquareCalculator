package com.proggroup.areasquarecalculator.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.proggroup.approximatecalcs.data.Point;
import com.proggroup.areasquarecalculator.data.Project;

import java.util.ArrayList;
import java.util.List;

public class AvgPointHelper {
    public static final String TABLE_NAME = "avg_points";
    public static final String ID = "_avg_id";
    public static final String PPM_VALUE = "_ppm";

    public static final String CREATE_REQUEST = "create table " + TABLE_NAME +
            " ( " + BaseColumns._ID + " integer primary key autoincrement, " +
            PPM_VALUE + " real not null, " +
            Project.ID + " integer not null);";
    public static final String DROP_REQUEST = "drop table if exists" + TABLE_NAME;

    private SQLiteDatabase writeDb;
    private Project project;

    public AvgPointHelper(SQLiteDatabase writeDb, Project project) {
        this.writeDb = writeDb;
        this.project = project;
    }

    public void addAvgPoint() {
        if(project.isSimpleMeasure()) {
            List<Integer> avgPoints = getAvgPoints();
            if(avgPoints.size() == Project.SIMPLE_MEASURE_AVG_POINTS_COUNT) {
                return;
            }
        }
        ContentValues cv = new ContentValues(2);
        cv.put(Project.ID, project.getId());
        cv.put(PPM_VALUE, 0);
        writeDb.insert(TABLE_NAME, null, cv);
    }

    public void updatePpm(int avgPointId, float ppm) {
        ContentValues cv = new ContentValues(1);
        cv.put(PPM_VALUE, ppm);
        writeDb.update(TABLE_NAME, cv, BaseColumns._ID + " = ?", new String[] {avgPointId + ""});
    }

    public float getPpmValue(int pointId) {
        Cursor cursor = writeDb/*readDb*/.query(TABLE_NAME, new String[]{PPM_VALUE},
                BaseColumns._ID + " = ?", new String[]{"" + pointId}, null, null, null);

        if (cursor.moveToFirst()) {
            float ppm = cursor.getFloat(0);
            cursor.close();

            return ppm;
        } else {
            cursor.close();
            return -1;
        }
    }

    public List<Integer> getAvgPoints() {
        Cursor cursor = writeDb/*readDb*/.query(TABLE_NAME, new String[]{BaseColumns._ID},
                Project.ID + " = ?", new String[]{"" + project.getId()}, null, null, null);

        if (cursor.moveToFirst()) {
            List<Integer> res = new ArrayList<>(cursor.getCount());

            do {
                res.add(cursor.getInt(0));

            } while (cursor.moveToNext());
            cursor.close();

            return res;
        } else {
            cursor.close();
            return new ArrayList<>(0);
        }
    }

    public void deleteAvgPoint(int avgPointId) {
        if(project.isSimpleMeasure()) {
            return;
        }
        SquarePointHelper squarePointHelper = new SquarePointHelper(writeDb);
        List<Integer> squarePointIds = squarePointHelper.getSquarePointIds(avgPointId);
        for (int squarePointId : squarePointIds) {
            squarePointHelper.deleteSquarePointId(squarePointId, project.isSimpleMeasure());
        }
        writeDb.delete(TABLE_NAME, BaseColumns._ID + " = ?", new String[] {"" + avgPointId});
    }
}
