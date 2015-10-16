package com.proggroup.areasquarecalculator.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import com.proggroup.areasquarecalculator.activities.IActivityCallback;
import com.proggroup.areasquarecalculator.adapters.CalculatePpmSimpleAdapter;
import com.proggroup.areasquarecalculator.data.Constants;
import com.proggroup.areasquarecalculator.data.PrefConstants;
import com.proggroup.areasquarecalculator.data.Project;
import com.proggroup.areasquarecalculator.db.AvgPointHelper;
import com.proggroup.areasquarecalculator.db.PointHelper;
import com.proggroup.areasquarecalculator.db.ProjectHelper;
import com.proggroup.areasquarecalculator.db.SQLiteHelper;
import com.proggroup.areasquarecalculator.db.SquarePointHelper;
import com.proggroup.areasquarecalculator.utils.CalculatePpmUtils;
import com.proggroup.areasquarecalculator.utils.FloatFormatter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CalculatePpmSimpleFragment extends Fragment implements CalculatePpmSimpleAdapter.OnInfoFilledListener {

    /**
     * Request code for start load ppm curve file dialog.
     */
    private static final int LOAD_PPM_AVG_VALUES_REQUEST_CODE = 103;

    /**
     * Request code for start save ppm curve file dialog.
     */
    private static final int SAVE_PPM_AVG_VALUES = 104;

    private GridView mGridView;
    private View calculatePpmLayout, calculatePpmLayoutLoaded;
    private TextView resultPpm, resultPpmLoaded;
    private EditText avgValue, avgValueLoaded;
    private CalculatePpmSimpleAdapter adapter;

    private View calculatePpmSimple, calculatePpmSimpleLoaded;
    private View graph;
    private Button btnAddRow;
    private View buttonsLayout;
    private View loadPpmCurve, savePpmCurve;
    private List<Float> ppmPoints, avgSquarePoints;
    private LinearLayout avgPointsLayout;
    private View resetDatabase;

    private ProjectHelper mProjectHelper;
    private AvgPointHelper mAvgPointHelper;
    private SquarePointHelper mSquarePointHelper;
    private PointHelper mPointHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculate_ppm, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridView = (GridView) view.findViewById(R.id.grid);

        graph = view.findViewById(R.id.graph);

        graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                IActivityCallback callback = activity instanceof IActivityCallback ? (IActivityCallback)
                        activity : null;

                List<Float> ppmPoints = new ArrayList<>();
                List<Float> avgSquarePoints = new ArrayList<>();
                fillPpmAndSquaresFromDatabase(ppmPoints, avgSquarePoints);

                ArrayList<String> ppmStrings = new ArrayList<>(ppmPoints.size());
                ArrayList<String> squareStrings = new ArrayList<>(avgSquarePoints.size());

                for (Float ppm : ppmPoints) {
                    ppmStrings.add(ppm.intValue() + "");
                }
                for (Float square : avgSquarePoints) {
                    squareStrings.add(FloatFormatter.format(square));
                }

                callback.startFragmentToDefaultContainer(CurveFragment.newInstance(ppmStrings,
                                 squareStrings), true);
            }
        });

        calculatePpmLayout = view.findViewById(R.id.calculate_ppm_layout);

        calculatePpmLayoutLoaded = view.findViewById(R.id.calculate_ppm_layout_loaded);

        loadPpmCurve = view.findViewById(R.id.load_ppm_curve);

        resetDatabase = view.findViewById(R.id.simple_ppm_btn_reset);

        savePpmCurve = view.findViewById(R.id.save_ppm_curve);

        avgPointsLayout = (LinearLayout) view.findViewById(R.id.avg_points);

        avgPointsLayout.removeAllViews();
        TextView tv = new TextView(getActivity());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen
                .edit_text_size_default));
        tv.setText("");
        avgPointsLayout.addView(tv);

        ppmPoints = new ArrayList<>();
        avgSquarePoints = new ArrayList<>();

        calculatePpmSimpleLoaded = view.findViewById(R.id.calculate_ppm_loaded);

        calculatePpmSimpleLoaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (avgValueLoaded.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), R.string.input_avg_value, Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                float avgValueY = Float.parseFloat(avgValueLoaded.getText().toString());
                float value;
                try {
                    List<Float> ppmPoints = new ArrayList<>();
                    List<Float> avgSquarePoints = new ArrayList<>();
                    //ppmPoints.add(0f);
                    //avgSquarePoints.add(0f);
                    ppmPoints.addAll(CalculatePpmSimpleFragment.this.ppmPoints);
                    avgSquarePoints.addAll(CalculatePpmSimpleFragment.this.avgSquarePoints);
                    value = findPpmBySquare(avgValueY, ppmPoints, avgSquarePoints);
                } catch (Exception e) {
                    value = -1;
                }

                if (value == -1) {
                    Toast.makeText(getActivity(), R.string.wrong_data, Toast.LENGTH_LONG).show();
                } else {
                    resultPpmLoaded.setText(FloatFormatter.format(value));
                }
            }
        });

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
                float value;
                try {
                    List<Float> ppmPoints = new ArrayList<>();
                    List<Float> avgSquarePoints = new ArrayList<>();
                    fillPpmAndSquaresFromDatabase(ppmPoints, avgSquarePoints);
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

        resetDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase database = InterpolationCalculator.getInstance().getSqLiteHelper()
                        .getWritableDatabase();
                mAvgPointHelper.clear();
                mSquarePointHelper.clear();
                new PointHelper(database).clear();
                mProjectHelper.clear();
                mProjectHelper.startInit(database);
                adapter = new CalculatePpmSimpleAdapter(CalculatePpmSimpleFragment.this,
                        CalculatePpmSimpleFragment.this,
                        mAvgPointHelper,
                        mSquarePointHelper,
                        mPointHelper,
                        initAdapterDataAndHelpersFromDatabase(false));

                SharedPreferences prefs = InterpolationCalculator.getInstance().getSharedPreferences();
                prefs.edit().remove(PrefConstants.INFO_IS_READY).apply();

                initLayouts();

                mGridView.setAdapter(adapter);
            }
        });

        resultPpm = (TextView) view.findViewById(R.id.result_ppm);

        resultPpmLoaded = (TextView) view.findViewById(R.id.result_ppm_loaded);

        avgValue = (EditText) view.findViewById(R.id.avg_value);

        avgValueLoaded = (EditText) view.findViewById(R.id.avg_value_loaded);

        buttonsLayout = view.findViewById(R.id.buttons_layout);

        initLayouts();

        btnAddRow = (Button) view.findViewById(R.id.simple_ppm_btn_addRow);

        List<Long> avgPointIds = initAdapterDataAndHelpersFromDatabase(true);

        adapter = new CalculatePpmSimpleAdapter(this, this, mAvgPointHelper, mSquarePointHelper,
                mPointHelper, avgPointIds);

        mGridView.setAdapter(adapter);

        btnAddRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = mAvgPointHelper.addAvgPoint();
                for (int i = 0; i < Project.TABLE_MAX_COLS_COUNT; i++) {
                    mSquarePointHelper.addSquarePointIdSimpleMeasure(id);
                }

                CalculatePpmSimpleAdapter adapter = ((CalculatePpmSimpleAdapter) mGridView
                        .getAdapter());
                adapter.notifyAvgPointAdded(id);
                buttonsLayout.setVisibility(View.GONE);
            }
        });

        loadPpmCurve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getBaseContext(), FileDialog
                        .class);
                intent.putExtra(FileDialog.START_PATH, Constants.BASE_DIRECTORY
                        .getAbsolutePath());
                intent.putExtra(FileDialog.ROOT_PATH, Constants.BASE_DIRECTORY
                        .getAbsolutePath());
                intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

                intent.putExtra(FileDialog.FORMAT_FILTER, new String[]{"csv"});
                startActivityForResult(intent, LOAD_PPM_AVG_VALUES_REQUEST_CODE);
            }
        });

        savePpmCurve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getBaseContext(), FileDialog
                        .class);
                intent.putExtra(FileDialog.START_PATH, Constants.BASE_DIRECTORY
                        .getAbsolutePath());
                intent.putExtra(FileDialog.ROOT_PATH, Constants.BASE_DIRECTORY
                        .getAbsolutePath());
                intent.putExtra(FileDialog.CAN_SELECT_DIR, true);

                startActivityForResult(intent, SAVE_PPM_AVG_VALUES);
            }
        });
    }

    /**
     * Init layout accord to data.
     */
    private void initLayouts() {
        SharedPreferences prefs = InterpolationCalculator.getInstance().getSharedPreferences();

        if (prefs.contains(PrefConstants.INFO_IS_READY)) {
            calculatePpmLayout.setVisibility(View.VISIBLE);
            savePpmCurve.setVisibility(View.VISIBLE);
            buttonsLayout.setVisibility(View.VISIBLE);
        } else {
            calculatePpmLayout.setVisibility(View.GONE);
            savePpmCurve.setVisibility(View.GONE);
            buttonsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Init database if it's empty.
     *
     * @param isCreatingHelpers true if helpers are null and need to be created.
     * @return List of id's average square points.
     */
    private List<Long> initAdapterDataAndHelpersFromDatabase(boolean isCreatingHelpers) {
        SQLiteHelper helper = InterpolationCalculator.getInstance().getSqLiteHelper();
        SQLiteDatabase mDatabase = helper.getWritableDatabase();

        if (isCreatingHelpers) {
            mProjectHelper = new ProjectHelper(mDatabase);
        }

        Project project = mProjectHelper.getProjects().get(0);

        if (isCreatingHelpers) {
            mAvgPointHelper = new AvgPointHelper(mDatabase, project);
        }

        boolean isFirstInit = mAvgPointHelper.getAvgPoints().isEmpty();

        if (isFirstInit) {
            for (int i = 0; i < Project.TABLE_MIN_ROWS_COUNT; i++) {
                mAvgPointHelper.addAvgPoint();
            }
        }
        List<Long> avgPointIds = mAvgPointHelper.getAvgPoints();

        if (isCreatingHelpers) {
            mSquarePointHelper = new SquarePointHelper(mDatabase);
        }

        if (isCreatingHelpers) {
            mPointHelper = new PointHelper(mDatabase);
        }

        if (isFirstInit) {
            for (long avgPoint : avgPointIds) {
                for (int i = 0; i < Project.TABLE_MAX_COLS_COUNT; i++) {
                    mSquarePointHelper.addSquarePointIdSimpleMeasure(avgPoint);
                }
            }
        }
        return avgPointIds;
    }

    /**
     * Fill ppmPoints and avgSquarePoints from database.
     *
     * @param ppmPoints       PpmPoints, that will be filled from database.
     * @param avgSquarePoints Average square points, that will be filled from database
     */
    private void fillPpmAndSquaresFromDatabase(List<Float> ppmPoints, List<Float> avgSquarePoints) {
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

        //ppmPoints.add(0f);
        //avgSquarePoints.add(0f);

        for (Map.Entry<Float, Float> entry : linkedMap.entrySet()) {
            ppmPoints.add(entry.getKey());
            avgSquarePoints.add(entry.getValue());
        }
    }


    /**
     * Search ppm value from square and saved data of ppmPoints and avgSquarePoints.
     *
     * @param square          Square, ppm for which is searching.
     * @param ppmPoints       Ppm values, which will be used for approximation.
     * @param avgSquarePoints Average square values, which will be used for approximation.
     * @return Searched ppm value.
     */
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
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case LOAD_PPM_AVG_VALUES_REQUEST_CODE:
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

                    final String timeName = "CAL_" + formatAddLeadingZero(calendar.get(Calendar
                            .YEAR)) + formatAddLeadingZero(calendar.get
                            (Calendar.MONTH)) + formatAddLeadingZero(calendar.get(Calendar
                            .DAY_OF_MONTH)) + "_" + formatAddLeadingZero(calendar.get
                            (Calendar.HOUR_OF_DAY)) + formatAddLeadingZero(calendar.get(Calendar
                            .MINUTE)) + formatAddLeadingZero(calendar.get(Calendar.SECOND));

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    View contentView = LayoutInflater.from(getActivity()).inflate(R.layout
                            .save_additional_options_layout, null);

                    final EditText editFileName = (EditText) contentView.findViewById(R.id
                            .edit_file_name);

                    builder.setView(contentView);
                    builder.setCancelable(true);

                    final AlertDialog dialog = builder.show();

                    contentView.findViewById(R.id.save_curve).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = timeName + "_" + editFileName.getText().toString() +
                                    ".csv";

                            File pathFile = new File(data
                                    .getStringExtra(FileDialog.RESULT_PATH), name);

                            pathFile.getParentFile().mkdirs();
                            try {
                                pathFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (CalculatePpmUtils.saveAvgValuesToFile((CalculatePpmSimpleAdapter) mGridView
                                    .getAdapter(), 6, pathFile.getAbsolutePath())) {
                                fillAvgPointsLayout();
                                Toast.makeText(getActivity(), "Save success as" + name, Toast.LENGTH_LONG)
                                        .show();
                            }

                            dialog.dismiss();
                        }
                    });

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

    /**
     * Fill layout with actual data.
     */
    private void fillAvgPointsLayout() {
        avgPointsLayout.removeAllViews();

        for (int i = 0; i < ppmPoints.size(); i++) {
            TextView tv = new TextView(getActivity());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen
                    .edit_text_size_default));
            tv.setText(FloatFormatter.format(ppmPoints.get(i)) + " " + FloatFormatter.format
                    (avgSquarePoints.get(i)) + "    ");

            avgPointsLayout.addView(tv);
        }
        calculatePpmLayoutLoaded.setVisibility(View.VISIBLE);
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
