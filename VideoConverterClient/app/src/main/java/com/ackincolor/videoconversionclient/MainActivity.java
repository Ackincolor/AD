package com.ackincolor.videoconversionclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ackincolor.videoconversionclient.controller.ConvertController;

public class MainActivity extends AppCompatActivity {

    private TextView status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //appel de l'api
        final ConvertController cc = new ConvertController(this);
        Button start = findViewById(R.id.convert);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cc.start("Game.of.Thrones.S07E07.1080p-intro.mkv");
            }
        });
    }
    public void setStatusConversion(String status){
        this.status.setText(status);
    }
}
