package com.proggroup.areasquarecalculator.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.proggroup.areasquarecalculator.R;
import com.proggroup.areasquarecalculator.utils.FloatFormatter;
import com.proggroup.areasquarecalculator.views.ChartMarkerView;

import java.util.ArrayList;
import java.util.List;

public class CurveFragment extends Fragment implements OnChartValueSelectedListener{

    private static final String PPMS_TAG = "ppms";
    private static final String SQUARES_TAG = "squares";

    private GridView mDisplayGrid;
    private LineChart mLineChart;
    private ArrayList<String> ppmStrings, squareStrings;
    private SparseArray<Float> squares;

    public CurveFragment() {
    }

    public static CurveFragment newInstance(ArrayList<String> ppmStrings, ArrayList<String>
            squareStrings) {
        CurveFragment fragment = new CurveFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(PPMS_TAG, ppmStrings);
        args.putStringArrayList(SQUARES_TAG, squareStrings);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chart, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDisplayGrid = (GridView) view.findViewById(R.id.display_values);
        mLineChart = (LineChart) view.findViewById(R.id.chart);

        Bundle args = getArguments();
        ppmStrings = args.getStringArrayList(PPMS_TAG);
        squareStrings = args.getStringArrayList(SQUARES_TAG);

        int countPoints = squareStrings.size();

        squares = new SparseArray<>(countPoints);

        for (int i = 0; i < countPoints; i++) {
            squares.put(Integer.parseInt(ppmStrings.get(i)), Float.parseFloat(squareStrings.get
                    (i)));
        }

        initLine();
    }

    private void initLine() {
        mLineChart.setOnChartValueSelectedListener(this);
        mLineChart.setExtraOffsets(60, 60, 60, 60);
        mLineChart.setDrawGridBackground(false);
        mLineChart.setDoubleTapToZoomEnabled(false);

        // no description text
        mLineChart.setDescription("");
        mLineChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mLineChart.setTouchEnabled(true);

        // enable scaling and dragging
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(true);


        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        ChartMarkerView mv = new ChartMarkerView(getActivity(), R.layout.chart_marer_view);

        // set the marker to the chart
        mLineChart.setMarkerView(mv);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelsToSkip(0);

        setData();

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setEnabled(false);
        mLineChart.getAxisRight().setEnabled(false);

        leftAxis.setAxisMaxValue(squares.valueAt(squares.size() - 1) + 100);
        leftAxis.setAxisMinValue(squares.valueAt(0) - 100);
        leftAxis.setStartAtZero(true);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mLineChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mLineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
    }

    private static int gcd(int a, int b, int less){
        while (b > 0) {
            int temp = b;
            b = a % b;
            a = temp;
            if(a < less) {
                a = less;
                break;
            }
        }
        return a;
    }

    private void setData() {

        ArrayList<String> xVals = new ArrayList<>();

        int xMin = squares.keyAt(0);
        int xMax = squares.keyAt(squares.size() - 1);

        int j = 0;
        int nextKey = squares.keyAt(j);

        List<Integer> indexes = new ArrayList<>(squares.size());

        int gcd = squares.keyAt(1) - squares.keyAt(0);

        int minGcd = (squares.keyAt(squares.size() - 1) - squares.keyAt(0)) / (5 * squares.size());

        for (int i = 1; i < squares.size() - 1; i++) {
            gcd = gcd(gcd, squares.keyAt(i + 1) - squares.keyAt(i), minGcd);
        }

        for (int i = xMin; i < xMax; i+= gcd) {
            if(i >= nextKey) {
                j++;
                xVals.add(i + "");
                nextKey = squares.keyAt(j);
                indexes.add(xVals.size() - 1);
            } else {
                xVals.add("");
            }
        }

        xVals.add(xMax + "");
        indexes.add(xVals.size() - 1);

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        for (int i = 0; i < squares.size(); i++) {
            yVals.add(new Entry(squares.valueAt(i), indexes.get(i)));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "Square values");
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(2f);
        set1.setCircleSize(4f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(13f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.BLACK);
        set1.setDrawFilled(true);
        set1.setDrawCircleHole(true);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        mLineChart.setData(data);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        int a = 10;
        int b = a + 1;
    }

    @Override
    public void onNothingSelected() {

    }
}
