package com.ackincolor.videoconversionclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.ackincolor.videoconversionclient.controller.ConvertController;
import com.ackincolor.videoconversionclient.utils.FileAdapter;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<TextView> status;
    private ArrayList<ProgressBar> progressBar;
    private Spinner spinner;
    private String[] items = {};
    private FileAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final LinearLayout rl = (LinearLayout) findViewById(R.id.ll);
        this.status = new ArrayList<>();
        this.progressBar = new ArrayList<>();

        //appel de l'api
        final ConvertController cc = new ConvertController(this);
        final Button start = findViewById(R.id.convert);
        this.status.add((TextView) findViewById(R.id.status));
        this.progressBar.add((ProgressBar) findViewById(R.id.progressBar));
        this.spinner = findViewById(R.id.spinner);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cc.start(adapter.getItem(spinner.getSelectedItemPosition()),progressBar.get(progressBar.size()-1),status.get(status.size()-1),status.size()-1);
                ProgressBar pb = new ProgressBar(getApplicationContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(339, 30);
                pb.setLayoutParams(params );
                TextView tv = new TextView(getApplicationContext());
                progressBar.add(pb);
                status.add(tv);
                rl.addView(status.get(status.size()-1));
                rl.addView(progressBar.get(progressBar.size()-1));
            }
        });

        this.adapter = new FileAdapter(this,R.layout.my_widget,this.items);
        this.spinner.setAdapter(this.adapter);
        cc.directories(this);
    }
    public void setStatusConversion(String status, int number){
        this.status.get(number).setText(status);
    }
    public void setListSpinner(String[] array){
        adapter.replaceValues(array);
        adapter.notifyDataSetChanged();
    }
}
