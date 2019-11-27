package com.ackincolor.videoconversionclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.ackincolor.videoconversionclient.controller.ConvertController;
import com.ackincolor.videoconversionclient.utils.FileAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView status;
    private ProgressBar progressBar;
    private Spinner spinner;
    private String[] items = {};
    private FileAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //appel de l'api
        final ConvertController cc = new ConvertController(this);
        Button start = findViewById(R.id.convert);
        this.status = findViewById(R.id.status);
        this.progressBar = findViewById(R.id.progressBar);
        this.spinner = findViewById(R.id.spinner);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cc.start(adapter.getItem(spinner.getSelectedItemPosition()),progressBar);
            }
        });

        this.adapter = new FileAdapter(this,R.layout.my_widget,this.items);
        this.spinner.setAdapter(this.adapter);
        cc.directories(this);
    }
    public void setStatusConversion(String status){
        this.status.setText(status);
    }
    public void setListSpinner(String[] array){
        adapter.replaceValues(array);
        adapter.notifyDataSetChanged();
    }
}
