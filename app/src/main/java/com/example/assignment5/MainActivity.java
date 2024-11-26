package com.example.assignment5;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sketch = findViewById(R.id.sketch_tagger);
        Button photo = findViewById(R.id.photo_tagger);
        Button story = findViewById(R.id.story_teller);

        sketch.setOnClickListener(view -> {
            Intent switchActivityIntent = new Intent(MainActivity.this,SketchActivity.class);
            startActivity(switchActivityIntent);
        });

        photo.setOnClickListener(view -> {
            Intent switchActivityIntent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(switchActivityIntent);
        });

        story.setOnClickListener(view -> {
            Intent switchActivityIntent = new Intent(MainActivity.this, StoryActivity.class);
            startActivity(switchActivityIntent);
        });

    }
}