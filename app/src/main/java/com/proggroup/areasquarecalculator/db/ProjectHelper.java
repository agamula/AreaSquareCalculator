package com.proggroup.areasquarecalculator.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.proggroup.areasquarecalculator.data.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectHelper {
    public static final String CREATE_REQUEST = "create table if not exists " + Project.TABLE_NAME +
            "(" + Project.ID + " integer primary key autoincrement, "
            + Project.IS_SIMPLE_MEASURE + " integer not null);";
    public static final String DROP_REQUEST = "drop table if exists" + Project.TABLE_NAME;

    private SQLiteDatabase writeDb, readDb;
    private SQLiteHelper helper;

    public ProjectHelper(SQLiteHelper helper) {
        this.helper = helper;
        writeDb = helper.getWritableDatabase();
        readDb = helper.getReadableDatabase();
    }

    public void startInit() {
        addProject(true);
        addProject(false);
    }

    public Project addProject(boolean isSimpleMeasure) {
        ContentValues cv = new ContentValues(1);
        cv.put(Project.IS_SIMPLE_MEASURE, isSimpleMeasure ? 1 : 0);
        int id = (int) writeDb.insert(Project.TABLE_NAME, null, cv);
        Project project = new Project();
        project.setIsSimpleMeasure(isSimpleMeasure);
        project.setId(id);
        return project;
    }

    public void deleteProject(Project project) {
        AvgPointHelper avgPointHelper = new AvgPointHelper(helper, project);
        for (int id : avgPointHelper.getAvgPoints()) {
            avgPointHelper.deleteAvgPoint(id);
        }
        writeDb.delete(Project.TABLE_NAME, Project.ID + " = ?", new String[] {project.getId() +
                ""});
    }

    public List<Project> getProjects() {
        Cursor cursor = readDb.query(Project.TABLE_NAME, new String[]{Project.ID, Project
                         .IS_SIMPLE_MEASURE}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            List<Project> res = new ArrayList<>(cursor.getCount());

            do {
                Project project = new Project();
                project.setId(cursor.getInt(0));
                project.setIsSimpleMeasure(cursor.getInt(1) == 1);

                res.add(project);
            } while (cursor.moveToNext());
            cursor.close();

            return res;
        } else {
            cursor.close();
            return new ArrayList<>(0);
        }
    }
}
