package com.proggroup.areasquarecalculator.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.lamerman.FileDialog;
import com.proggroup.approximatecalcs.CalculateUtils;
import com.proggroup.areasquarecalculator.InterpolationCalculator;
import com.proggroup.areasquarecalculator.R;
import com.proggroup.areasquarecalculator.adapters.CalculatePpmSimpleAdapter;
import com.proggroup.areasquarecalculator.data.AvgPoint;
import com.proggroup.areasquarecalculator.data.PrefConstants;
import com.proggroup.areasquarecalculator.data.Project;
import com.proggroup.areasquarecalculator.db.AvgPointHelper;
import com.proggroup.areasquarecalculator.db.PointHelper;
import com.proggroup.areasquarecalculator.db.ProjectHelper;
import com.proggroup.areasquarecalculator.db.SQLiteHelper;
import com.proggroup.areasquarecalculator.db.SquarePointHelper;
import com.proggroup.areasquarecalculator.utils.FloatFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalculatePpmSimpleFragment extends Fragment implements CalculatePpmSimpleAdapter.OnInfoFilledListener {

    private GridView mGridView;
    private View calculatePpmLayout, solveEquationLayout;
    private TextView solvedFormula, solvedPpm;
    private EditText avgValue;
    private CalculatePpmSimpleAdapter adapter;
    private View solveLineEquation, calculatePpmSimple;
    private View progressLayout;
    private float lineKoef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculate_simple_ppm, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridView = (GridView) view.findViewById(R.id.grid);

        solveEquationLayout = view.findViewById(R.id.solve_line_equation_layout);

        progressLayout = view.findViewById(R.id.solve_progress);

        solveLineEquation = view.findViewById(R.id.solve_line_equation);
        solveLineEquation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressLayout.setVisibility(View.VISIBLE);
                SQLiteHelper helper = InterpolationCalculator.getInstance().getSqLiteHelper();
                SQLiteDatabase writeDb = helper.getWritableDatabase();
                Project project = new ProjectHelper(writeDb).getProjects().get(0);

                AvgPointHelper helper1 = new AvgPointHelper(writeDb, project);
                List<Integer> avgids = helper1.getAvgPoints();

                SquarePointHelper pointHelper = new SquarePointHelper(writeDb);
                PointHelper pHelper = new PointHelper(writeDb);

                float maxKoef = -1f, minKoef = -1f;

                for (int avgId : avgids) {
                    float ppm = helper1.getPpmValue(avgId);
                    List<Integer> sqIds = pointHelper.getSquarePointIds(avgId);

                    List<Float> squares = new ArrayList<>(sqIds.size());

                    for (int i = 0; i < sqIds.size(); i++) {
                        squares.add(CalculateUtils.calculateSquare(pHelper.getPoints(sqIds.get(i)
                        )));
                    }
                    float y = new AvgPoint(squares).avg();

                    if (maxKoef == -1f) {
                        maxKoef = minKoef = y / ppm;
                    } else {
                        float curKoef = y / ppm;
                        if (maxKoef < curKoef) {
                            maxKoef = curKoef;
                        } else if (minKoef > curKoef) {
                            minKoef = curKoef;
                        }
                    }
                }

                double minAngle = Math.atan(minKoef);
                double maxAngle = Math.atan(maxKoef);

                lineKoef = (float) Math.tan((minAngle + maxAngle) / 2);

                solvedFormula.setText(String.format(Locale.US, getString(R.string.equation_value), FloatFormatter.format(
                        lineKoef)));

                InterpolationCalculator.getInstance().getSharedPreferences().edit().putFloat
                        (PrefConstants.LINE_KOEF, lineKoef).apply();

                progressLayout.setVisibility(View.GONE);
                calculatePpmLayout.setVisibility(View.VISIBLE);
            }
        });


        solvedFormula = (TextView) view.findViewById(R.id.solved_formula);

        calculatePpmLayout = view.findViewById(R.id.calculate_ppm_simple_layout);

        calculatePpmSimple = view.findViewById(R.id.calculate_ppm_simple);
        calculatePpmSimple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (avgValue.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), R.string.input_avg_value, Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                float avgValueY = Float.parseFloat(avgValue.getText().toString());
                //solvedPpm.setText(FloatFormatter.format(avgValueY / lineKoef));
                float value = -1;
                try {
                    value = getXbyY(avgValueY);
                } catch (Exception e) {
                    value = -1;
                }

                if(value == -1) {
                    Toast.makeText(getActivity(), R.string.wrong_data, Toast.LENGTH_LONG).show();
                } else {
                    solvedPpm.setText(FloatFormatter.format(value));
                }
            }
        });

        solvedPpm = (TextView) view.findViewById(R.id.result_ppm_simple);

        avgValue = (EditText) view.findViewById(R.id.avg_value);

        SharedPreferences prefs = InterpolationCalculator.getInstance().getSharedPreferences();

        if (prefs.contains(PrefConstants.INFO_IS_READY)) {
            solveEquationLayout.setVisibility(View.VISIBLE);
        } else {
            solveEquationLayout.setVisibility(View.GONE);
        }

        if (prefs.contains(PrefConstants.LINE_KOEF)) {
            lineKoef = prefs.getFloat(PrefConstants.LINE_KOEF, 0f);
            solvedFormula.setText(String.format(Locale.US, getString(R.string.equation_value), FloatFormatter.format(lineKoef)));
            calculatePpmLayout.setVisibility(View.VISIBLE);
        }

        adapter = new CalculatePpmSimpleAdapter(this, this);

        mGridView.setAdapter(adapter);
    }

    private float getXbyY(float y) {
        List<Float> xPoints = new ArrayList<>();
        List<Float> yPoints = new ArrayList<>();

        SQLiteHelper helper = InterpolationCalculator.getInstance().getSqLiteHelper();
        SQLiteDatabase writeDb = helper.getWritableDatabase();
        Project project = new ProjectHelper(writeDb).getProjects().get(0);
        AvgPointHelper helper1 = new AvgPointHelper(writeDb, project);
        List<Integer> avgids = helper1.getAvgPoints();

        SquarePointHelper pointHelper = new SquarePointHelper(writeDb);
        PointHelper pHelper = new PointHelper(writeDb);

        for (int avgId : avgids) {
            xPoints.add(helper1.getPpmValue(avgId));
            List<Integer> sqIds = pointHelper.getSquarePointIds(avgId);

            List<Float> squares = new ArrayList<>(sqIds.size());

            for (int i = 0; i < sqIds.size(); i++) {
                squares.add(CalculateUtils.calculateSquare(pHelper.getPoints(sqIds.get(i)
                )));
            }
            yPoints.add(new AvgPoint(squares).avg());
        }

        for (int i = 0; i < yPoints.size() - 1; i++) {
            //check whether the point belongs to the line
            if (y >= yPoints.get(i) && y <= yPoints.get(i + 1)) {
                //getting x value
                float x1 = xPoints.get(i);
                float x2 = xPoints.get(i + 1);
                float y1 = yPoints.get(i);
                float y2 = yPoints.get(i + 1);

                return ((y - y1) * (x2 - x1)) / (y2 - y1) + x1;
            }
        }

        return -1;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            int row = requestCode / Project.SIMPLE_MEASURE_AVG_POINTS_COUNT;
            int col = requestCode % Project.SIMPLE_MEASURE_AVG_POINTS_COUNT;
            adapter.updateSquare(row, col, data.getStringExtra(FileDialog.RESULT_PATH));
        }
    }

    @Override
    public void onInfoFilled() {
        InterpolationCalculator.getInstance().getSharedPreferences().edit().putBoolean
                (PrefConstants.INFO_IS_READY, true).apply();
        solveEquationLayout.setVisibility(View.VISIBLE);
    }
}
