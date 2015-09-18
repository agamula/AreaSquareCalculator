package com.proggroup.areasquarecalculator;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.proggroup.approximatecalcs.CalculateUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private EditText mResultEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mResultEdit = (EditText)findViewById(R.id.result);
    }

    public void calculateSquare(View v) {
        File mInputFile = new File(Environment.getExternalStorageDirectory(), "approximate_data" +
                ".csv");

        if(mInputFile.exists()) {
            float res = CalculateUtils.calculateSquare(mInputFile);
            //res = CalculateUtils.calculateSquareDeterminant(mInputFile);
            //res = CalculateUtils.calculateSquareDeterminantParallel(mInputFile);
            mResultEdit.setText(String.format("%.4f", res));
        }
    }
}
