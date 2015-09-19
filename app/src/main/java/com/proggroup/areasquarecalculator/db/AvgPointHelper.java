package com.proggroup.areasquarecalculator.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.proggroup.approximatecalcs.data.Point;
import com.proggroup.areasquarecalculator.data.Project;

import java.util.ArrayList;
import java.util.List;

public class AvgPointHelper {
    public static final String TABLE_NAME = "avg_points";
    public static final String ID = "_avg_id";

    public static final String CREATE_REQUEST = "create table if not exists " + TABLE_NAME +
            "(" + ID + " integer primary key autoincrement, "
            + Project.ID + " integer not null);";
    public static final String DROP_REQUEST = "drop table if exists" + TABLE_NAME;

    private SQLiteDatabase writeDb, readDb;
    private SQLiteHelper helper;
    private Project project;

    public AvgPointHelper(SQLiteHelper helper, Project project) {
        this.helper = helper;
        writeDb = helper.getWritableDatabase();
        readDb = helper.getReadableDatabase();
        this.project = project;
    }

    public void addAvgPoint(List<Point> points) {
        if(project.isSimpleMeasure()) {
            List<Integer> avgPoints = getAvgPoints();
            if(avgPoints.size() == Project.SIMPLE_MEASURE_AVG_POINTS_COUNT) {
                return;
            }
        }
        ContentValues cv = new ContentValues(1);
        cv.put(Project.ID, project.getId());
        int id = (int) writeDb.insert(TABLE_NAME, null, cv);
        SquarePointHelper squarePointHelper = new SquarePointHelper(helper);
        squarePointHelper.addSquarePointId(id);
        int squarePointId = squarePointHelper.getSquarePointIds(id).get(0);
        PointHelper pointHelper = new PointHelper(helper);
        pointHelper.addPoints(squarePointId, points);
    }

    public List<Integer> getAvgPoints() {
        Cursor cursor = readDb.query(TABLE_NAME, new String[]{ID},
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
        SquarePointHelper squarePointHelper = new SquarePointHelper(helper);
        List<Integer> squarePointIds = squarePointHelper.getSquarePointIds(avgPointId);
        for (int squarePointId : squarePointIds) {
            squarePointHelper.deleteSquarePointId(squarePointId, project.isSimpleMeasure());
        }
        writeDb.delete(TABLE_NAME, ID + " = ?", new String[] {"" + avgPointId});
    }
}
