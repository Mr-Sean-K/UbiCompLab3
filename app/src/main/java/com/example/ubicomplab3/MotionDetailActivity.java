package com.example.ubicomplab3;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.TextView;


public class MotionDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_motion_detail);

        // get parameters from notification intent
        Intent intent = getIntent();
        String motionDetails = intent.getStringExtra("motion_time");

        // show details
        TextView textView = findViewById(R.id.textview1);
        if (motionDetails != null) {
            textView.setText(motionDetails);
        } else {
            textView.setText("No motion details available.");
        }
    }
}