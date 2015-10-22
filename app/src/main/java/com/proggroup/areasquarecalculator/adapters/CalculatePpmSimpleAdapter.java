package com.proggroup.areasquarecalculator.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import com.proggroup.areasquarecalculator.InterpolationCalculator;
import com.proggroup.areasquarecalculator.R;
import com.proggroup.areasquarecalculator.data.AvgPoint;
import com.proggroup.areasquarecalculator.data.Constants;
import com.proggroup.areasquarecalculator.data.Project;
import com.proggroup.areasquarecalculator.db.AvgPointHelper;
import com.proggroup.areasquarecalculator.db.PointHelper;
import com.proggroup.areasquarecalculator.db.ProjectHelper;
import com.proggroup.areasquarecalculator.db.SQLiteHelper;
import com.proggroup.areasquarecalculator.db.SquarePointHelper;
import com.proggroup.areasquarecalculator.utils.FloatFormatter;
import com.proggroup.squarecalculations.CalculateUtils;
import com.proggroup.squarecalculations.DocParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CalculatePpmSimpleAdapter extends BaseAdapter {

    /*
     * Field is for actual data.
     * Can be loaded from csv file, or database if it's filled
     */
    public static final int ITEM_ID_DATA = 0;

    /*
     * Field is for header
     * Loading from array resource.
     */
    public static final int ITEM_ID_HEADER = 1;

    /*
     * Field is for result of avg calculations of available data.
     */
    public static final int ITEM_ID_CALC_AVG_RESULT = 3;

    /*
     * Field is for input ppm of value
     */
    public static final int ITEM_ID_KNOWN_PPM = 4;

    public static final int ITEM_ID_DELETE_ROW = 5;

    private SquarePointHelper squarePointHelper;
    private AvgPointHelper avgPointHelper;
    private List<Long> avgPointIds;
    private List<List<Float>> squareValues;
    private List<Float> avgValues;
    private List<List<String>> paths;
    private PointHelper mPointHelper;
    private final Fragment fragment;
    private int ppmIndex;
    private final OnInfoFilledListener onInfoFilledListener;

    public CalculatePpmSimpleAdapter(Fragment fragment, OnInfoFilledListener
            onInfoFilledListener, AvgPointHelper avgPointHelper, SquarePointHelper
                                             mSquarePointHelper, PointHelper mPointHelper,
                                     List<Long> avgPointIds) {
        this.fragment = fragment;
        this.onInfoFilledListener = onInfoFilledListener;

        this.avgPointIds = avgPointIds;
        this.avgPointHelper = avgPointHelper;

        squarePointHelper = mSquarePointHelper;

        squareValues = new ArrayList<>(Project.TABLE_MAX_COLS_COUNT);
        for (int i = 0; i < avgPointIds.size(); i++) {
            List<Float> squares = new ArrayList<>(Project.TABLE_MAX_COLS_COUNT);
            for (int j = 0; j < Project.TABLE_MAX_COLS_COUNT; j++) {
                squares.add(0f);
            }
            squareValues.add(squares);
        }

        this.mPointHelper = mPointHelper;

        for (int i = 0; i < avgPointIds.size(); i++) {
            List<Long> squareIds = squarePointHelper.getSquarePointIds(avgPointIds.get(i));
            for (int j = 0; j < squareIds.size(); j++) {
                long squareId = squareIds.get(j);

                List<PointF> points = mPointHelper.getPoints(squareId);
                if (!points.isEmpty()) {
                    squareValues.get(i).set(j, CalculateUtils.calculateSquare(points));
                }
            }
        }

        avgValues = new ArrayList<>(avgPointIds.size());

        for (int i = 0; i < avgPointIds.size(); i++) {
            List<Float> squares = squareValues.get(i);
            avgValues.add(new AvgPoint(remove0List(squares)).avg());
        }

        paths = new ArrayList<>(avgPointIds.size());
        for (int i = 0; i < avgPointIds.size(); i++) {
            List<String> pathes = new ArrayList<>(Project.TABLE_MAX_COLS_COUNT);
            for (int j = 0; j < Project.TABLE_MAX_COLS_COUNT; j++) {
                pathes.add(null);
            }
            paths.add(pathes);
        }

        ppmIndex = -1;

        checkAvgValues();
    }


    /**
     *
     * @return Square values, calculated from all loaded csv files.
     */
    public List<List<Float>> getSquareValues() {
        return squareValues;
    }

    /**
     *
     * @return Average square values, calculated for each of rows.
     */
    public List<Float> getAvgValues() {
        return avgValues;
    }

    /**
     * Notify of adding new avgPoint to database.
     *
     * @param avgPointId Id of new avgPoint is added to database.
     */
    public void notifyAvgPointAdded(long avgPointId) {
        avgPointIds.add(avgPointId);
        List<Float> points = new ArrayList<>(Project.TABLE_MAX_COLS_COUNT);
        for (int i = 0; i < Project.TABLE_MAX_COLS_COUNT; i++) {
            points.add(0f);
        }
        squareValues.add(points);
        avgValues.add(new AvgPoint(remove0List(points)).avg());
        List<String> pathes = new ArrayList<>(Project.TABLE_MAX_COLS_COUNT);
        for (int j = 0; j < Project.TABLE_MAX_COLS_COUNT; j++) {
            pathes.add(null);
        }
        paths.add(pathes);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (avgValues.size() + 1) * (Project
                .TABLE_MAX_COLS_COUNT + 3);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position < Project.TABLE_MAX_COLS_COUNT + 3) {
            return ITEM_ID_HEADER;
        } else if (position % (Project.TABLE_MAX_COLS_COUNT + 3) == Project
                .TABLE_MAX_COLS_COUNT + 1) {
            return ITEM_ID_CALC_AVG_RESULT;
        } else if (position % (Project.TABLE_MAX_COLS_COUNT + 3) == 0) {
            return ITEM_ID_KNOWN_PPM;
        } else if(position % (Project.TABLE_MAX_COLS_COUNT + 3) == Project
                .TABLE_MAX_COLS_COUNT + 2) {
            return ITEM_ID_DELETE_ROW;
        } else {
            return ITEM_ID_DATA;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int itemId = (int) getItemId(position);

        if (convertView == null || convertView.getTag() != Integer.valueOf(itemId)) {
            LayoutInflater inflater = (LayoutInflater) InterpolationCalculator.getInstance().getApplicationContext
                    ().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (itemId) {
                case ITEM_ID_HEADER:
                    convertView = inflater.inflate(R.layout.layout_table_header, parent, false);
                    break;
                case ITEM_ID_KNOWN_PPM:
                    convertView = inflater.inflate(R.layout.layout_table_ppm_edit, parent, false);
                    break;
                case ITEM_ID_CALC_AVG_RESULT:
                    convertView = inflater.inflate(R.layout.layout_table_edit_text, parent, false);
                    convertView.findViewById(R.id.edit).setEnabled(false);
                    break;
                case ITEM_ID_DATA:
                    convertView = inflater.inflate(R.layout.layout_table_item, parent, false);
                    break;
                case ITEM_ID_DELETE_ROW:
                    convertView = inflater.inflate(R.layout.layout_table_delete, parent, false);
                    break;
                default:
                    convertView = inflater.inflate(R.layout.layout_table_header, parent, false);
            }

            convertView.setTag(Integer.valueOf(itemId));
        }

        switch (itemId) {
            case ITEM_ID_HEADER:
                ((TextView) convertView.findViewById(R.id.header_name)).setText(convertView
                        .getResources().getStringArray(R.array.headers)[position]);
                break;
            case ITEM_ID_KNOWN_PPM:
                final int index = position / (Project.TABLE_MAX_COLS_COUNT + 3) - 1;

                EditText ppmText = (EditText) convertView.findViewById(R.id.edit);
                ppmText.setGravity(Gravity.NO_GRAVITY);

                if (ppmText.getTag() != null) {
                    ppmText.removeTextChangedListener(new PpmWatcher((Integer) ppmText.getTag()));
                }

                ppmText.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                ppmIndex = (Integer) v.getTag();
                                break;
                        }
                        return false;
                    }
                });

                ppmText.setTag(index);
                ppmText.addTextChangedListener(new PpmWatcher(index));

                float ppmValue = avgPointHelper.getPpmValue(avgPointIds.get(index));
                if (ppmValue != 0 && index != ppmIndex) {
                    ppmText.setText((int)ppmValue + "");
                }
                break;
            case ITEM_ID_CALC_AVG_RESULT:
                int index1 = position / (Project.TABLE_MAX_COLS_COUNT + 3) - 1;

                ppmText = (EditText) convertView.findViewById(R.id.edit);
                ppmText.setTextColor(Color.BLACK);
                ppmText.setGravity(Gravity.CENTER);

                List<Float> squares = squareValues.get(index1);
                boolean itemsInited = false;

                for (Float square : squares) {
                    if (square != 0f) {
                        itemsInited = true;
                        break;
                    }
                }

                if (itemsInited) {
                    ppmText.setText(FloatFormatter.format(avgValues.get(index1)));
                } else {
                    ppmText.setText("");
                }
                break;
            case ITEM_ID_DATA:
                index1 = position / (Project.TABLE_MAX_COLS_COUNT + 3) - 1;

                int pointNumber = position % (Project.TABLE_MAX_COLS_COUNT + 3) - 1;

                TextView squareVal = (TextView) convertView.findViewById(R.id.square_value);
                TextView path = (TextView) convertView.findViewById(R.id.csv_path);
                if (path.getTag() != null) {
                    Integer val = (Integer) path.getTag();
                    int row = val / Project.TABLE_MAX_COLS_COUNT;
                    int col = val % Project.TABLE_MAX_COLS_COUNT;
                    path.removeTextChangedListener(new PathChangeWatcher(row, col));
                }

                int pathTag = index1 * Project.TABLE_MAX_COLS_COUNT + pointNumber;

                path.setTag(pathTag);

                path.addTextChangedListener(new PathChangeWatcher(index1, pointNumber));

                if (paths.get(index1).get(pointNumber) != null) {
                    path.setText(paths.get(index1).get(pointNumber));
                } else {
                    path.setText("");
                }

                if (squareValues.get(index1).get(pointNumber) == 0f) {
                    squareVal.setText("");
                } else {
                    squareVal.setText(FloatFormatter.format(squareValues.get(index1).get
                            (pointNumber)));
                }

                if (!path.getText().toString().isEmpty()) {
                    float val1 = CalculateUtils.calculateSquare(new File(path.getText().toString()));
                    if (val1 > 0) {
                        squareVal.setText(FloatFormatter.format(val1));
                    }
                }

                View csvView = convertView.findViewById(R.id.csv);

                csvView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer tag = (Integer) v.getTag();

                        if (tag < 0) {
                            Toast.makeText(fragment.getActivity(), R.string.input_ppm_first,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Intent intent = new Intent(fragment.getActivity().getBaseContext(), FileDialog
                                .class);
                        intent.putExtra(FileDialog.START_PATH, Constants.BASE_DIRECTORY
                                .getAbsolutePath());
                        intent.putExtra(FileDialog.ROOT_PATH, Constants.BASE_DIRECTORY
                                .getAbsolutePath());
                        intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

                        intent.putExtra(FileDialog.FORMAT_FILTER, new String[]{"csv"});
                        fragment.startActivityForResult(intent, tag);
                    }
                });

                ppmValue = avgPointHelper.getPpmValue(avgPointIds.get(index1));

                if (ppmValue == 0f) {
                    csvView.setTag(-pathTag - 1);
                } else {
                    csvView.setTag(pathTag);
                }

                break;
            case ITEM_ID_DELETE_ROW:
                convertView.findViewById(R.id.delete_row).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index1 = position / (Project.TABLE_MAX_COLS_COUNT + 3) - 1;
                        avgPointHelper.deleteAvgPoint(avgPointIds.get(index1));
                        avgPointIds.remove(index1);
                        squareValues.remove(index1);
                        paths.remove(index1);
                        avgValues.remove(index1);
                        notifyDataSetChanged();
                    }
                });
                break;
        }

        return convertView;
    }

    /**
     * Calculate avg value for row index
     *
     * @param rowNumber row index
     */
    public void calculateAvg(int rowNumber) {
        List<String> rowPaths = paths.get(rowNumber);
        for (int i = 0; i < rowPaths.size(); i++) {
            if (rowPaths.get(i) != null) {
                File f = new File(rowPaths.get(i));
                if (f.exists()) {
                    float val = CalculateUtils.calculateSquare(f);
                    if (val > 0f) {
                        squareValues.get(rowNumber).set(i, val);
                    }
                }
            }
        }
        List<Float> values = squareValues.get(rowNumber);
        avgValues.set(rowNumber, new AvgPoint(remove0List(values)).avg());
        notifyDataSetChanged();
    }

    /**
     * Remove 0 values from list.
     */
    private List<Float> remove0List(List<Float> values) {
        List<Float> res = new ArrayList<>(values.size());
        for (float val : values) {
            if (val != 0f) {
                res.add(val);
            }
        }
        return res;
    }

    /**
     * Update adapter values accord to actual data
     *
     * @param row    Row index
     * @param column Column index
     * @param path   Path to file csv is loaded from
     */
    public void updateSquare(int row, int column, String path) {
        List<Float> squares = squareValues.get(row);
        File f = new File(path);
        squares.set(column, CalculateUtils.calculateSquare(f));

        boolean inited = false;

        for (float square : squares) {
            if (square != 0f) {
                inited = true;
                break;
            }
        }

        if (inited) {
            List<Float> res = new ArrayList<>(squares.size());
            for (float val : squares) {
                if (val != 0f) {
                    res.add(val);
                }
            }
            avgValues.set(row, new AvgPoint(res).avg());
        }

        checkAvgValues();

        long squareId = squarePointHelper.getSquarePointIds(avgPointIds.get(row)).get(column);

        List<PointF> points = DocParser.parse(f);

        List<PointF> dbPoints = mPointHelper.getPoints(squareId);
        if (dbPoints.isEmpty()) {
            mPointHelper.addPoints(squareId, points);
        } else {
            mPointHelper.updatePoints(squareId, points);
        }

        paths.get(row).set(column, path);
    }

    /**
     * Check if all values are filled, and invoke ready listener if it is.
     */
    private void checkAvgValues() {
        for (float avgValue : avgValues) {
            if (avgValue == 0f) {
                return;
            }
        }
        onInfoFilledListener.onInfoFilled();
    }

    public interface OnInfoFilledListener {
        void onInfoFilled();
    }

    /**
     * Watcher for ppm value changed.
     */
    private class PpmWatcher implements TextWatcher {

        private final int index;

        private PpmWatcher(int index) {
            this.index = index;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            long avgPointId = avgPointIds.get(index);
            if (!s.toString().isEmpty()) {
                avgPointHelper.updatePpm(avgPointId, Integer.parseInt(s.toString()));
            } else {
                avgPointHelper.updatePpm(avgPointId, 0);
            }

            notifyDataSetChanged();
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof PpmWatcher && ((PpmWatcher) o).index == index;
        }
    }

    /**
     * Watcher for path changed.
     */
    private class PathChangeWatcher implements TextWatcher {

        private final int row, column;

        private PathChangeWatcher(int row, int column) {
            this.row = row;
            this.column = column;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(s.length() != 0) {
                paths.get(row).set(column, s.toString());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof PathChangeWatcher)) {
                return false;
            }
            PathChangeWatcher watcher = (PathChangeWatcher) o;
            return watcher.column == column && watcher.row == row;
        }
    }
}
