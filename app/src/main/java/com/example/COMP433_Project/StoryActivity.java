package com.example.COMP433_Project;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StoryActivity extends AppCompatActivity {

    TextToSpeech tts = null;
    String url = "https://api.textcortex.com/v1\n" +
            "/texts/social-media-posts";
    String API_KEY = "gAAAAABnTgeTQZIthJZCQS3x5hHO5dAv9HRXX1FU0q1N7gK0FybQpDq8lBc8EllZ095kzsdORqM1aosOa8qi3j92h4RYU8RLac9oJpd2-pM693f1SJr6Di41RZPMZbhr5wQ0uPqJZJTZ";
    ArrayList<Item> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        TextView selected = findViewById(R.id.tags_list);
        TextView story = findViewById(R.id.story);

        CheckedListAdapter adapter = new CheckedListAdapter(this,R.layout.checkedlist_item, data, selected);
        ListView lv = findViewById(R.id.listview);

        SQLiteDatabase mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);
        Cursor cursor = mydb.rawQuery("SELECT COUNT(*) FROM SKETCHDATA JOIN DATA", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count != 0) {
            update();
            lv.setAdapter(adapter);
        }


        Button back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(view -> {
            tts.stop();
            Intent switchActivityIntent = new Intent(StoryActivity.this, MainActivity.class);
            startActivity(switchActivityIntent);
        });

        CheckBox check = findViewById(R.id.checkbox);
        check.setOnClickListener(view -> {
            data.clear();
            update();
            lv.setAdapter(adapter);
        });

        Button find_button = findViewById(R.id.find_button);
        find_button.setOnClickListener(view -> {
            data.clear();
            update();
            lv.setAdapter(adapter);
        });

        Button story_button = findViewById(R.id.story_button);
        story_button.setOnClickListener(view -> {
            try {
                makeHttpRequest();
            } catch (JSONException e) {
                Log.e("error", e.toString());
            }
        });
    }

    private void update() {
        CheckBox check = findViewById(R.id.checkbox);
        EditText findEdit = findViewById(R.id.find_edit);
        String find = findEdit.getText().toString().trim();

        SQLiteDatabase mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);


        Cursor c;
        if(check.isChecked()){
            if (find.isEmpty()) {
                c = mydb.rawQuery("SELECT * FROM(SELECT * FROM DATA UNION ALL SELECT * FROM SKETCHDATA) ORDER BY DATE DESC", null);

            } else {
                String[] terms = find.split("\\s*,\\s*|\\s+");

                StringBuilder whereClause = new StringBuilder();
                String[] args = new String[terms.length];

                for (int i = 0; i < terms.length; i++) {
                    if (i > 0) {
                        whereClause.append(" OR ");
                    }
                    whereClause.append("TAGS LIKE ?");
                    args[i] = "%" + terms[i] + "%";
                }

                String query = "SELECT * FROM (SELECT * FROM DATA UNION ALL SELECT * FROM SKETCHDATA) WHERE " + whereClause.toString() + " ORDER BY DATE DESC";
                c = mydb.rawQuery(query, args);
            }
        }
        else{
            if (find.isEmpty()) {
                c = mydb.rawQuery("SELECT * FROM DATA ORDER BY DATE DESC", null);

            } else {
                String[] terms = find.split("\\s*,\\s*|\\s+");

                StringBuilder whereClause = new StringBuilder();
                String[] args = new String[terms.length];

                for (int i = 0; i < terms.length; i++) {
                    if (i > 0) {
                        whereClause.append(" OR ");
                    }
                    whereClause.append("TAGS LIKE ?");
                    args[i] = "%" + terms[i] + "%";
                }

                String query = "SELECT * FROM DATA WHERE " + whereClause.toString() + " ORDER BY DATE DESC";
                c = mydb.rawQuery(query, args);
            }
        }

        while (c.moveToNext()) {
            byte[] imageBytes = c.getBlob(c.getColumnIndexOrThrow("IMAGE"));
            String imageDate = c.getString(c.getColumnIndexOrThrow("DATE"));
            String imageTag = c.getString(c.getColumnIndexOrThrow("TAGS"));
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            data.add(new Item(imageBitmap,"Tags: " + imageTag + "\nDate: " + imageDate));
        }

        c.close();
        mydb.close();
    }

    void makeHttpRequest() throws JSONException {
        JSONObject data = new JSONObject();

        TextView selected = findViewById(R.id.tags_list);
        String keyString = selected.getText().toString();
        String[] keyArray = keyString.split("\\s*,\\s*");
        TextView story = findViewById(R.id.story);

        tts = new TextToSpeech(this, status -> {
            if(status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });

        data.put("context", "Short Story");
        data.put("max_tokens", 100);
        data.put("mode", "twitter");
        data.put("model", "claude-3-haiku");

        data.put("keywords", new JSONArray(keyArray));


        JsonObjectRequest request = new  JsonObjectRequest(Request.Method.POST, url, data, response -> {
            tts.speak(extractStory(response),TextToSpeech.QUEUE_FLUSH, null, null);
            story.setText(extractStory(response));
        }, error -> Log.e("error", new String(error.networkResponse.data))){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + API_KEY);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    public static String extractStory(JSONObject jsonObject) {
        try {
            if (jsonObject.getString("status").equals("success")) {
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray outputs = data.getJSONArray("outputs");
                if (outputs.length() > 0) {
                    JSONObject firstOutput = outputs.getJSONObject(0);
                    return firstOutput.getString("text");
                }
            }
            return "Story not found.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing JSON response.";
        }
    }
}
