package com.proggroup.areasquarecalculator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.proggroup.areasquarecalculator.InterpolationCalculator;
import com.proggroup.areasquarecalculator.R;
import com.proggroup.areasquarecalculator.data.Project;

public class CalculatePpmSimpleAdapter extends BaseAdapter {

    public static final int ITEM_ID_DATA = 0;
    public static final int ITEM_ID_HEADER = 1;
    public static final int ITEM_ID_CALCULATE_AVG = 2;
    public static final int ITEM_ID_CALC_AVG_RESULT = 3;
    public static final int ITEM_ID_KNOWN_PPM = 4;

    public CalculatePpmSimpleAdapter() {

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
        } else if(position % (Project.SIMPLE_MEASURE_AVG_POINTS_COUNT + 3) == 0) {
            return ITEM_ID_KNOWN_PPM;
        } else {
            return ITEM_ID_DATA;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemId = (int)getItemId(position);

        if (convertView == null || convertView.getTag() != Integer.valueOf(itemId)) {
            LayoutInflater inflater = (LayoutInflater) InterpolationCalculator.getInstance().getApplicationContext
                    ().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_table_header, parent, false);

            convertView.setTag(Integer.valueOf(itemId));
        }

        switch (itemId) {
            case ITEM_ID_HEADER:
                ((TextView)convertView.findViewById(R.id.header_name)).setText(convertView
                        .getResources().getStringArray(R.array.headers)[position]);
                break;
            case ITEM_ID_KNOWN_PPM:
                break;
            case ITEM_ID_CALCULATE_AVG:
                break;
            case ITEM_ID_CALC_AVG_RESULT:
                break;
            case ITEM_ID_DATA:
                break;
        }

        return convertView;
    }
}
