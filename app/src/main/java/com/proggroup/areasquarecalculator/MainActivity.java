package com.proggroup.areasquarecalculator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.proggroup.approximatecalcs.CalculateUtils;

import java.io.File;
import java.nio.channels.Pipe;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText mResultEdit;
    private Button mSetPointsButton;
    private ArrayList<Point> points;
    private File[] tables = new File[16];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        points = new ArrayList<>();

        mResultEdit = (EditText)findViewById(R.id.result);
        mSetPointsButton = (Button)findViewById(R.id.main_btn_setPoints);
        mSetPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetPintsClick();
            }
        });
    }

    private void onSetPintsClick(){

        View v = getLayoutInflater().inflate(R.layout.dialog_tables, null, false);
        ArrayList<Button> buttons = new ArrayList<>();
        ArrayList<TextView> labels = new ArrayList<>();

        initButtons(buttons, v);

        new AlertDialog.Builder(this)
                .setTitle("Choose tables")
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tables[requestCode] = new File(data.getData().getPath());
    }

    private void initButtons(final ArrayList<Button> buttons, View v){
        buttons.add((Button)v.findViewById(R.id.btn_table1));
        buttons.add((Button)v.findViewById(R.id.btn_table2));
        buttons.add((Button)v.findViewById(R.id.btn_table3));
        buttons.add((Button)v.findViewById(R.id.btn_table4));
        buttons.add((Button)v.findViewById(R.id.btn_table5));
        buttons.add((Button)v.findViewById(R.id.btn_table6));
        buttons.add((Button)v.findViewById(R.id.btn_table7));
        buttons.add((Button)v.findViewById(R.id.btn_table8));
        buttons.add((Button)v.findViewById(R.id.btn_table9));
        buttons.add((Button)v.findViewById(R.id.btn_table10));
        buttons.add((Button)v.findViewById(R.id.btn_table11));
        buttons.add((Button)v.findViewById(R.id.btn_table12));
        buttons.add((Button)v.findViewById(R.id.btn_table13));
        buttons.add((Button)v.findViewById(R.id.btn_table14));
        buttons.add((Button)v.findViewById(R.id.btn_table15));
        buttons.add((Button)v.findViewById(R.id.btn_table16));

        View.OnClickListener ocl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };

        for(Button b : buttons)
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    startActivityForResult(intent, buttons.indexOf(v));
                }
            });
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
