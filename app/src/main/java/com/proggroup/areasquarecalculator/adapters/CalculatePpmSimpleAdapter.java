package com.proggroup.areasquarecalculator.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PointF;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import com.proggroup.areasquarecalculator.InterpolationCalculator;
import com.proggroup.areasquarecalculator.R;
import com.proggroup.areasquarecalculator.data.AvgPoint;
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

    public static final int ITEM_ID_DATA = 0;
    public static final int ITEM_ID_HEADER = 1;
    public static final int ITEM_ID_CALCULATE_AVG = 2;
    public static final int ITEM_ID_CALC_AVG_RESULT = 3;
    public static final int ITEM_ID_KNOWN_PPM = 4;

    private Project project;
    private AvgPointHelper avgPointHelper;
    private SquarePointHelper squarePointHelper;
    private List<Integer> avgPointIds;
    private List<List<Float>> squareValues;
    private List<Float> avgValues;
    private List<Boolean> indexesClicked;
    private List<List<String>> paths;
    private final SQLiteDatabase mDatabase;
    private final Fragment fragment;
    private int ppmIndex;
    private final OnInfoFilledListener onInfoFilledListener;

    public CalculatePpmSimpleAdapter(Fragment fragment, OnInfoFilledListener onInfoFilledListener) {
        this.fragment = fragment;
        this.onInfoFilledListener = onInfoFilledListener;
        SQLiteHelper helper = InterpolationCalculator.getInstance().getSqLiteHelper();
        mDatabase = helper.getWritableDatabase();
        project = new ProjectHelper(mDatabase).getProjects().get(0);
        avgPointHelper = new AvgPointHelper(mDatabase, project);

        boolean isFirstInit = avgPointHelper.getAvgPoints().isEmpty();

        if (isFirstInit) {
            for (int i = 0; i < Project.SIMPLE_MEASURE_AVG_POINTS_COUNT; i++) {
                avgPointHelper.addAvgPoint();
            }
        }
        avgPointIds = avgPointHelper.getAvgPoints();

        squarePointHelper = new SquarePointHelper(mDatabase);

        if (isFirstInit) {
            for (int avgPoint : avgPointIds) {
                for (int i = 0; i < Project.SIMPLE_MEASURE_AVG_POINTS_COUNT; i++) {
                    squarePointHelper.addSquarePointIdSimpleMeasure(avgPoint);
                }
            }
        }

        squareValues = new ArrayList<>(Project.SIMPLE_MEASURE_AVG_POINTS_COUNT);
        for (int i = 0; i < Project.SIMPLE_MEASURE_AVG_POINTS_COUNT; i++) {
            List<Float> squares = new ArrayList<>();
            for (int j = 0; j < Project.SIMPLE_MEASURE_AVG_POINTS_COUNT; j++) {
                squares.add(0f);
            }
            squareValues.add(squares);
        }

        PointHelper pointHelper = new PointHelper(mDatabase);

        for (int i = 0; i < Project.SIMPLE_MEASURE_AVG_POINTS_COUNT; i++) {
            List<Integer> squareIds = squarePointHelper.getSquarePointIds(avgPointIds.get(i));
            for (int j = 0; j < squareIds.size(); j++) {
                int squareId = squareIds.get(j);

                List<PointF> points = pointHelper.getPoints(squareId);
                if (!points.isEmpty()) {
                    squareValues.get(i).set(j, CalculateUtils.calculateSquare(points));
                }
            }
        }

        avgValues = new ArrayList<>(Project.SIMPLE_MEASURE_AVG_POINTS_COUNT);
        indexesClicked = new ArrayList<>(Project.SIMPLE_MEASURE_AVG_POINTS_COUNT);

        for (int i = 0; i < Project.SIMPLE_MEASURE_AVG_POINTS_COUNT; i++) {
            List<Float> squares = squareValues.get(i);
            avgValues.add(new AvgPoint(squares).avg());
            indexesClicked.add(false);
        }

        paths = new ArrayList<>(Project.SIMPLE_MEASURE_AVG_POINTS_COUNT);
        for (int i = 0; i < Project.SIMPLE_MEASURE_AVG_POINTS_COUNT; i++) {
            List<String> pathes = new ArrayList<>(Project.SIMPLE_MEASURE_AVG_POINTS_COUNT);
            for (int j = 0; j < Project.SIMPLE_MEASURE_AVG_POINTS_COUNT; j++) {
                pathes.add(null);
            }
            paths.add(pathes);
        }

        ppmIndex = -1;

        checkAvgValues();
    }

    @Override
    public int getCount() {
        return (Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 1) * (Project
                .SIMPLE_MEASURE_AVG_POINTS_COUNT + 3);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (position < Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 3) {
            return ITEM_ID_HEADER;
        } else if (position % (Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 3) == Project
                .SIMPLE_MEASURE_AVG_POINTS_COUNT + 1) {
            return ITEM_ID_CALCULATE_AVG;
        } else if (position % (Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 3) == Project
                .SIMPLE_MEASURE_AVG_POINTS_COUNT + 2) {
            return ITEM_ID_CALC_AVG_RESULT;
        } else if (position % (Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 3) == 0) {
            return ITEM_ID_KNOWN_PPM;
        } else {
            return ITEM_ID_DATA;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemId = (int) getItemId(position);

        if (convertView == null || convertView.getTag() != Integer.valueOf(itemId)) {
            LayoutInflater inflater = (LayoutInflater) InterpolationCalculator.getInstance().getApplicationContext
                    ().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (itemId) {
                case ITEM_ID_HEADER:
                    convertView = inflater.inflate(R.layout.layout_table_header, parent, false);
                    break;
                case ITEM_ID_KNOWN_PPM:
                    convertView = inflater.inflate(R.layout.layout_table_edit_text, parent, false);
                    break;
                case ITEM_ID_CALC_AVG_RESULT:
                    convertView = inflater.inflate(R.layout.layout_table_edit_text, parent, false);
                    convertView.findViewById(R.id.edit).setEnabled(false);
                    break;
                case ITEM_ID_CALCULATE_AVG:
                    convertView = inflater.inflate(R.layout.layout_table_avg_calculate, parent,
                            false);
                    break;
                case ITEM_ID_DATA:
                    convertView = inflater.inflate(R.layout.layout_table_item, parent, false);
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
                final int index = position / (Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 3) - 1;

                EditText ppmText = (EditText) convertView.findViewById(R.id.edit);
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
                    ppmText.setText(FloatFormatter.format(ppmValue));
                }
                break;
            case ITEM_ID_CALC_AVG_RESULT:
                int index1 = position / (Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 3) - 1;

                ppmText = (EditText) convertView.findViewById(R.id.edit);

                List<Float> squares = squareValues.get(index1);
                boolean allItemInited = true;

                for (Float square : squares) {
                    if (square == 0f) {
                        allItemInited = false;
                        break;
                    }
                }

                if (allItemInited || indexesClicked.get(index1)) {
                    ppmText.setText(FloatFormatter.format(avgValues.get(index1)));
                } else {
                    ppmText.setText("");
                }
                break;
            case ITEM_ID_CALCULATE_AVG:
                final int indexAvg = position / (Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 3) - 1;
                int avgPointId = avgPointIds.get(indexAvg);

                List<Integer> squarePointIds = squarePointHelper.getSquarePointIds(avgPointId);

                Button avgCalcButton = (Button) convertView.findViewById(R.id.calculate_avg);
                avgCalcButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < paths.size(); i++) {
                            if (paths.get(indexAvg).get(i) != null) {
                                File f = new File(paths.get(indexAvg).get(i));
                                if(f.exists()) {
                                    float val = CalculateUtils.calculateSquare(f);
                                    if (val > 0f) {
                                        squareValues.get(indexAvg).set(i, val);
                                    }
                                }
                            }
                        }
                        List<Float> values = squareValues.get(indexAvg);
                        List<Float> res = new ArrayList<>(values.size());
                        for (float val : values) {
                            if(val != 0f) {
                                res.add(val);
                            }
                        }

                        avgValues.set(indexAvg, new AvgPoint(res).avg());
                        indexesClicked.set(indexAvg, true);
                        notifyDataSetChanged();
                    }
                });

                avgCalcButton.setEnabled(true);

                break;
            case ITEM_ID_DATA:
                index1 = position / (Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 3) - 1;

                int pointNumber = position % (Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 3) - 1;

                TextView squareVal = (TextView) convertView.findViewById(R.id.square_value);
                EditText path = (EditText) convertView.findViewById(R.id.csv_path);
                if (path.getTag() != null) {
                    Integer val = (Integer) path.getTag();
                    int row = val / Project.SIMPLE_MEASURE_AVG_POINTS_COUNT;
                    int col = val % Project.SIMPLE_MEASURE_AVG_POINTS_COUNT;
                    path.removeTextChangedListener(new PathChangeWatcher(row, col));
                }

                int pathTag = index1 * Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + pointNumber;

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

                        if(tag < 0) {
                            Toast.makeText(fragment.getActivity(), R.string.input_ppm_first,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Intent intent = new Intent(fragment.getActivity().getBaseContext(), FileDialog
                                .class);
                        intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory()
                                .getAbsolutePath());
                        intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

                        intent.putExtra(FileDialog.FORMAT_FILTER, new String[]{"csv"});
                        fragment.startActivityForResult(intent, tag);
                    }
                });

                ppmValue = avgPointHelper.getPpmValue(avgPointIds.get(index1));

                if(ppmValue == 0f) {
                    csvView.setTag(-pathTag - 1);
                } else {
                    csvView.setTag(pathTag);
                }

                break;
        }

        return convertView;
    }

    public void updateSquare(int row, int column, String path) {
        List<Float> squares = squareValues.get(row);
        File f = new File(path);
        squares.set(column, CalculateUtils.calculateSquare(f));

        boolean inited = true;

        for (float square : squares) {
            if(square == 0f) {
                inited = false;
                break;
            }
        }

        if(inited) {
            List<Float> res = new ArrayList<>(squares.size());
            for (float val : squares) {
                if(val != 0f) {
                    res.add(val);
                }
            }
            avgValues.set(row, new AvgPoint(res).avg());
        }

        checkAvgValues();

        int squareId = squarePointHelper.getSquarePointIds(avgPointIds.get(row)).get(column);

        PointHelper pointHelper = new PointHelper(mDatabase);

        List<PointF> points = DocParser.parse(f);

        List<PointF> dbPoints = pointHelper.getPoints(squareId);
        if(dbPoints.isEmpty()) {
            pointHelper.addPoints(squareId, points);
        } else {
            pointHelper.updatePoints(squareId, points);
        }

        paths.get(row).set(column, path);
        notifyDataSetChanged();
    }

    private void checkAvgValues() {
        for (float avgValue : avgValues) {
            if(avgValue == 0f) {
                return;
            }
        }
        onInfoFilledListener.onInfoFilled();
    }

    public interface OnInfoFilledListener {
        void onInfoFilled();
    }

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
            int avgPointId = avgPointIds.get(index);
            if (!s.toString().isEmpty()) {
                avgPointHelper.updatePpm(avgPointId, Float.parseFloat(s.toString()));
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
            paths.get(row).set(column, s.toString());
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
