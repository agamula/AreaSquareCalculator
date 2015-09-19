package com.proggroup.areasquarecalculator.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.proggroup.areasquarecalculator.InterpolationCalculator;
import com.proggroup.areasquarecalculator.R;
import com.proggroup.areasquarecalculator.adapters.CalculatePpmSimpleAdapter;
import com.proggroup.areasquarecalculator.data.PrefConstants;
import com.proggroup.areasquarecalculator.utils.FloatFormatter;

public class CalculatePpmSimpleFragment extends Fragment{

    private GridView mGridView;
    private View calculatePpmLayout, solveEquationLayout;
    private float lineCoefficient;
    private TextView solvedFormula;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculate_simple_ppm, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridView = (GridView) view.findViewById(R.id.grid);

        mGridView.setAdapter(new CalculatePpmSimpleAdapter());



        solveEquationLayout = view.findViewById(R.id.solve_line_equation_layout);
        solvedFormula = (TextView) view.findViewById(R.id.solved_formula);

        calculatePpmLayout = view.findViewById(R.id.calculate_ppm_simple_layout);

        SharedPreferences prefs = InterpolationCalculator.getInstance().getSharedPreferences();

        if(prefs.contains(PrefConstants.LINE_KOEF)) {
            solveEquationLayout.setVisibility(View.VISIBLE);
            lineCoefficient = prefs.getFloat(PrefConstants.LINE_KOEF, 0f);
            solvedFormula.setText("y = " + FloatFormatter.format(lineCoefficient) + "*x");
            calculatePpmLayout.setVisibility(View.VISIBLE);
        } else {
            solveEquationLayout.setVisibility(View.GONE);
        }
    }
}
