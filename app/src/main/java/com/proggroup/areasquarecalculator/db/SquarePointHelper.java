package com.proggroup.areasquarecalculator.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.proggroup.areasquarecalculator.data.Project;

import java.util.ArrayList;
import java.util.List;

public class SquarePointHelper {
    private static final String TABLE_NAME = "square_points";
    public static final String ID = "_square_point_id";

    public static final String CREATE_REQUEST = "create table if not exists " + TABLE_NAME +
            "(" + ID + " integer primary key autoincrement, "
            + AvgPointHelper.ID + " integer not null);";
    public static final String DROP_REQUEST = "drop table if exists" + TABLE_NAME;

    private SQLiteDatabase writeDb, readDb;
    private SQLiteHelper helper;

    public SquarePointHelper(SQLiteHelper helper) {
        this.helper = helper;
        writeDb = helper.getWritableDatabase();
        readDb = helper.getReadableDatabase();
    }

    public List<Integer> getSquarePointIds(int avgPointId) {
        Cursor cursor = readDb.query(TABLE_NAME, new String[]{ID},
                AvgPointHelper.ID + " = ?", new String[]{"" + avgPointId}, null, null, null);

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

    public void addSquarePointId(int avgPointId) {
        ContentValues cv = new ContentValues(1);
        cv.put(AvgPointHelper.ID, avgPointId);
        writeDb.insert(TABLE_NAME, null, cv);
    }

    public void addSquarePointIdSimpleMeasure(int avgPointId) {
        List<Integer> squarePointIds = getSquarePointIds(avgPointId);
        if (squarePointIds.size() == Project.SIMPLE_MEASURE_AVG_POINTS_COUNT) {
            return;
        }
        addSquarePointId(avgPointId);
    }

    private int getAvgPointId(int squarePointId) {
        Cursor cursor = readDb.query(TABLE_NAME, new String[]{AvgPointHelper.ID},
                ID + " = ?", new String[]{"" + squarePointId}, null, null, null);

        if (cursor.moveToFirst()) {
            cursor.close();

            return cursor.getInt(0);
        } else {
            cursor.close();
            return -1;
        }
    }

    public void deleteSquarePointId(int id, boolean isSimpleMeasure) {
        if (isSimpleMeasure) {
            int avgPointId = getAvgPointId(id);
            if (avgPointId != -1) {
                return;
            }
        }
        new PointHelper(helper).deletePoints(id);
        writeDb.delete(TABLE_NAME, ID + " = ?", new String[]{id + ""});
    }
}
