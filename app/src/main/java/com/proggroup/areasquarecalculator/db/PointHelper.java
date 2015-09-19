package com.proggroup.areasquarecalculator.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.proggroup.approximatecalcs.data.Point;

import java.util.ArrayList;
import java.util.List;

public class PointHelper {
    private static final String TABLE_NAME = "points";
    private static final String POINT_X = "_x";
    private static final String POINT_Y = "_y";

    public static final String CREATE_REQUEST = "create table " + TABLE_NAME +
            " ( " + BaseColumns._ID + " integer primary key autoincrement, "
            + POINT_X + " integer not null, "
            + POINT_Y + " integer not null, "
            + SquarePointHelper.ID + " integer not null);";
    public static final String DROP_REQUEST = "drop table if exists" + TABLE_NAME;

    private SQLiteDatabase writeDb;

    public PointHelper(SQLiteDatabase writeDb) {
        this.writeDb = writeDb;
    }

    public List<Point> getPoints(int squarePointId) {
        Cursor cursor = writeDb/*readDb*/.query(TABLE_NAME, new String[]{POINT_X, POINT_Y},
                SquarePointHelper.ID + " = ?", new String[]{"" + squarePointId}, null, null, null);

        if (cursor.moveToFirst()) {
            List<Point> res = new ArrayList<>(cursor.getCount());

            do {
                Point point = new Point(cursor.getInt(0), cursor.getInt(1));
                res.add(point);

            } while (cursor.moveToNext());
            cursor.close();

            return res;
        } else {
            cursor.close();
            return new ArrayList<>(0);
        }
    }

    public void addPoints(int squarePointId, List<Point> points) {
        for (Point point : points) {
            ContentValues cv = new ContentValues(3);
            cv.put(POINT_X, (int)point.x);
            cv.put(POINT_Y, point.y);
            cv.put(SquarePointHelper.ID, squarePointId);
            writeDb.insert(TABLE_NAME, null, cv);
        }
    }

    public void updatePoints(int squarePointId, List<Point> points) {
        deletePoints(squarePointId);
        addPoints(squarePointId, points);
    }

    public void deletePoints(int squarePointId) {
        writeDb.delete(TABLE_NAME, SquarePointHelper.ID + " = ?", new String[] {"" + squarePointId});
    }
}
