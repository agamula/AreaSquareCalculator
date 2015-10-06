package com.proggroup.areasquarecalculator.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import com.proggroup.areasquarecalculator.InterpolationCalculator;
import com.proggroup.areasquarecalculator.R;
import com.proggroup.areasquarecalculator.adapters.CalculatePpmSimpleAdapter;
import com.proggroup.areasquarecalculator.data.PrefConstants;
import com.proggroup.areasquarecalculator.data.Project;
import com.proggroup.areasquarecalculator.db.AvgPointHelper;
import com.proggroup.areasquarecalculator.db.ProjectHelper;
import com.proggroup.areasquarecalculator.db.SQLiteHelper;
import com.proggroup.areasquarecalculator.db.SquarePointHelper;
import com.proggroup.areasquarecalculator.utils.CalculatePpmUtils;
import com.proggroup.areasquarecalculator.utils.FloatFormatter;
import com.proggroup.areasquarecalculator.utils.ReportCreator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CalculatePpmSimpleFragment extends Fragment implements CalculatePpmSimpleAdapter.OnInfoFilledListener {

    private static final int LOAD_PPM_AVG_VALUES = 103;
    private static final int SAVE_PPM_AVG_VALUES = 104;

    private GridView mGridView;
    private View calculatePpmLayout;
    private TextView resultPpm;
    private EditText avgValue;
    private CalculatePpmSimpleAdapter adapter;
    private View calculatePpmSimple;
    private Button btnAddRow, btnGenerateTable;
    private View buttonsLayout;
    private View loadPpmCurve, savePpmCurve;
    private List<Float> ppmPoints, avgSquarePoints;
    private LinearLayout avgPointsLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculate_ppm, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridView = (GridView) view.findViewById(R.id.grid);

        calculatePpmLayout = view.findViewById(R.id.calculate_ppm_layout);

        loadPpmCurve = view.findViewById(R.id.load_ppm_curve);

        savePpmCurve = view.findViewById(R.id.save_ppm_curve);

        avgPointsLayout = (LinearLayout) view.findViewById(R.id.avg_points);

        ppmPoints = new ArrayList<>();
        avgSquarePoints = new ArrayList<>();

        calculatePpmSimple = view.findViewById(R.id.calculate_ppm);
        calculatePpmSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (avgValue.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), R.string.input_avg_value, Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                float avgValueY = Float.parseFloat(avgValue.getText().toString());
                float value = -1;
                try {
                    List<Float> ppmPoints = new ArrayList<>();
                    List<Float> avgSquarePoints = new ArrayList<>();
                    if (ppmPoints.isEmpty() || avgSquarePoints.isEmpty()) {
                        fillPpmAndSquaresFromDatabase(ppmPoints, avgSquarePoints);
                    } else {
                        ppmPoints.addAll(CalculatePpmSimpleFragment.this.ppmPoints);
                        avgSquarePoints.addAll(CalculatePpmSimpleFragment.this.avgSquarePoints);
                    }
                    value = findPpmBySquare(avgValueY, ppmPoints, avgSquarePoints);
                } catch (Exception e) {
                    value = -1;
                }

                if (value == -1) {
                    Toast.makeText(getActivity(), R.string.wrong_data, Toast.LENGTH_LONG).show();
                } else {
                    resultPpm.setText(FloatFormatter.format(value));
                }
            }
        });

        resultPpm = (TextView) view.findViewById(R.id.result_ppm);

        avgValue = (EditText) view.findViewById(R.id.avg_value);

        SharedPreferences prefs = InterpolationCalculator.getInstance().getSharedPreferences();

        if (prefs.contains(PrefConstants.INFO_IS_READY)) {
            calculatePpmLayout.setVisibility(View.VISIBLE);
            savePpmCurve.setVisibility(View.VISIBLE);
        } else {
            calculatePpmLayout.setVisibility(View.GONE);
            savePpmCurve.setVisibility(View.GONE);
        }

        SQLiteHelper helper = InterpolationCalculator.getInstance().getSqLiteHelper();
        SQLiteDatabase mDatabase = helper.getWritableDatabase();
        Project project = new ProjectHelper(mDatabase).getProjects().get(0);

        final AvgPointHelper avgPointHelper = new AvgPointHelper(mDatabase, project);

        boolean isFirstInit = avgPointHelper.getAvgPoints().isEmpty();

        if (isFirstInit) {
            for (int i = 0; i < Project.TABLE_MIN_ROWS_COUNT; i++) {
                avgPointHelper.addAvgPoint();
            }
        }
        List<Long> avgPointIds = avgPointHelper.getAvgPoints();

        final SquarePointHelper squarePointHelper = new SquarePointHelper(mDatabase);

        if (isFirstInit) {
            for (long avgPoint : avgPointIds) {
                for (int i = 0; i < Project.TABLE_MAX_COLS_COUNT; i++) {
                    squarePointHelper.addSquarePointIdSimpleMeasure(avgPoint);
                }
            }
        }

        buttonsLayout = view.findViewById(R.id.buttons_layout);

        btnAddRow = (Button) view.findViewById(R.id.simple_ppm_btn_addRow);

        adapter = new CalculatePpmSimpleAdapter(this, this, avgPointHelper, squarePointHelper, avgPointIds);

        mGridView.setAdapter(adapter);

        btnAddRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = avgPointHelper.addAvgPoint();
                for (int i = 0; i < Project.TABLE_MAX_COLS_COUNT; i++) {
                    squarePointHelper.addSquarePointIdSimpleMeasure(id);
                }

                CalculatePpmSimpleAdapter adapter = ((CalculatePpmSimpleAdapter) mGridView
                        .getAdapter());
                adapter.addAvgPoint(id);
                buttonsLayout.setVisibility(View.GONE);
            }
        });

        loadPpmCurve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avgPointsLayout.removeAllViews();
                Intent intent = new Intent(getActivity().getBaseContext(), FileDialog
                        .class);
                intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory()
                        .getAbsolutePath());
                intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

                intent.putExtra(FileDialog.FORMAT_FILTER, new String[]{"csv"});
                startActivityForResult(intent, LOAD_PPM_AVG_VALUES);
            }
        });

        savePpmCurve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getBaseContext(), FileDialog
                        .class);
                intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory()
                        .getAbsolutePath());
                intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
                intent.putExtra(FileDialog.CAN_SELECT_DIR, true);

                startActivityForResult(intent, SAVE_PPM_AVG_VALUES);
            }
        });

        btnGenerateTable = (Button) view.findViewById(R.id.generate_csv);
        btnGenerateTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = "table";
                File f = new File(Environment.getExternalStorageDirectory(), fileName + ".csv");
                if (!f.exists()) {
                    try {
                        f.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (ReportCreator.createReport((CalculatePpmSimpleAdapter) mGridView
                        .getAdapter(), 6, "table")) {
                    Toast.makeText(getActivity(), "Write success", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void fillPpmAndSquaresFromDatabase(List<Float> ppmPoints, List<Float> squarePoints) {
        SQLiteHelper helper = InterpolationCalculator.getInstance().getSqLiteHelper();
        SQLiteDatabase writeDb = helper.getWritableDatabase();
        Project project = new ProjectHelper(writeDb).getProjects().get(0);
        AvgPointHelper helper1 = new AvgPointHelper(writeDb, project);
        List<Long> avgids = helper1.getAvgPoints();

        CalculatePpmSimpleAdapter adapter = ((CalculatePpmSimpleAdapter) mGridView
                .getAdapter());

        List<Float> avgValues = adapter.getAvgValues();

        Map<Float, Float> linkedMap = new TreeMap<>();

        for (int i = 0; i < avgids.size(); i++) {
            long avgId = avgids.get(i);
            linkedMap.put(helper1.getPpmValue(avgId), avgValues.get(i));
        }

        ppmPoints.add(0f);
        squarePoints.add(0f);

        for (Map.Entry<Float, Float> entry : linkedMap.entrySet()) {
            ppmPoints.add(entry.getKey());
            squarePoints.add(entry.getValue());
        }
    }

    private float findPpmBySquare(float square, List<Float> ppmPoints, List<Float> avgSquarePoints) {
        for (int i = 0; i < avgSquarePoints.size() - 1; i++) {
            //check whether the point belongs to the line
            if (square >= avgSquarePoints.get(i) && square <= avgSquarePoints.get(i + 1)) {
                //getting x value
                float x1 = ppmPoints.get(i);
                float x2 = ppmPoints.get(i + 1);
                float y1 = avgSquarePoints.get(i);
                float y2 = avgSquarePoints.get(i + 1);

                return ((square - y1) * (x2 - x1)) / (y2 - y1) + x1;
            }
        }

        return -1;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case LOAD_PPM_AVG_VALUES:
                    Pair<List<Float>, List<Float>> res = CalculatePpmUtils.parseAvgValuesFromFile
                            (data.getStringExtra(FileDialog.RESULT_PATH));

                    ppmPoints.clear();
                    ppmPoints.addAll(res.first);
                    avgSquarePoints.clear();
                    avgSquarePoints.addAll(res.second);
                    fillAvgPointsLayout();
                    break;
                case SAVE_PPM_AVG_VALUES:
                    ppmPoints.clear();
                    avgSquarePoints.clear();
                    fillPpmAndSquaresFromDatabase(ppmPoints, avgSquarePoints);

                    Calendar calendar = Calendar.getInstance();

                    String name = "CAL_" + formatAddLeadingZero(calendar.get(Calendar
                            .DAY_OF_MONTH)) + formatAddLeadingZero(calendar.get
                            (Calendar.MONTH)) + formatAddLeadingZero(calendar.get(Calendar.YEAR))
                            + "_" + formatAddLeadingZero(calendar.get
                            (Calendar.HOUR_OF_DAY)) + formatAddLeadingZero(calendar.get(Calendar
                            .MINUTE)) + formatAddLeadingZero(calendar.get(Calendar.SECOND)) + "" +
                            ".csv";
                    File pathFile = new File(data
                            .getStringExtra(FileDialog.RESULT_PATH), name);
                    try {
                        pathFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (CalculatePpmUtils.saveAvgValuesToFile(ppmPoints, avgSquarePoints, pathFile
                            .getAbsolutePath())) {
                        avgPointsLayout.removeAllViews();
                        fillAvgPointsLayout();
                        Toast.makeText(getActivity(), "Save success as" + name, Toast.LENGTH_LONG)
                                .show();
                    }
                    break;
                default:
                    int row = requestCode / Project.TABLE_MAX_COLS_COUNT;
                    int col = requestCode % Project.TABLE_MAX_COLS_COUNT;
                    adapter.updateSquare(row, col, data.getStringExtra(FileDialog.RESULT_PATH));
                    adapter.calculateAvg(row);
            }
        }
    }

    private String formatAddLeadingZero(int value) {
        return (value < 10 ? "0" : "") + value;
    }

    private void fillAvgPointsLayout() {
        for (int i = 0; i < ppmPoints.size(); i++) {
            TextView tv = new TextView(getActivity());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen
                    .edit_text_size_default));
            tv.setText(FloatFormatter.format(ppmPoints.get(i)) + " " + FloatFormatter.format
                    (avgSquarePoints.get(i)) + "    ");

            avgPointsLayout.addView(tv);
        }
    }

    @Override
    public void onInfoFilled() {
        InterpolationCalculator.getInstance().getSharedPreferences().edit().putBoolean
                (PrefConstants.INFO_IS_READY, true).apply();
        calculatePpmLayout.setVisibility(View.VISIBLE);
        buttonsLayout.setVisibility(View.VISIBLE);
        savePpmCurve.setVisibility(View.VISIBLE);
    }
}
