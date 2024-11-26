package com.example.assignment5;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private final String API_KEY = "AIzaSyA5SHfxXQwJq-vaxJMUoVaidt9IbI4yoQY";
    private ImageView largeView;
    private Bitmap image;
    ArrayList<Item> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        largeView = findViewById(R.id.imageMain);
        TextView tag = findViewById(R.id.Tag);
        ListAdapter adapter = new ListAdapter(this,R.layout.list_item, data);
        ListView lv = findViewById(R.id.listview);

        SQLiteDatabase mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);
        mydb.execSQL("CREATE TABLE IF NOT EXISTS DATA (" +
                "IMAGE BLOB PRIMARY KEY, " +
                "DATE TEXT, " +
                "TAGS TEXT)");
        Cursor cursor = mydb.rawQuery("SELECT COUNT(*) FROM DATA", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count != 0) {
            update();
            lv.setAdapter(adapter);
        }

        Button save_button = findViewById(R.id.save_button);
        save_button.setOnClickListener(view -> {
            byte[] ba = blobHelper(image);
            String currentDate = getDate();
            String tags = tag.getText().toString();

            mydb.execSQL("INSERT INTO DATA (IMAGE, DATE, TAGS) VALUES (?, ?, ?)",
                    new Object[]{ba,currentDate,tags});

        });

        Button back_button = findViewById(R.id.back);
        back_button.setOnClickListener(view -> {
            Intent switchActivityIntent = new Intent(CameraActivity.this, MainActivity.class);
            startActivity(switchActivityIntent);
        });

        Button tag_button = findViewById(R.id.tag_button);
        tag_button.setOnClickListener(view ->{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        tag.setText(myVisionTester());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        });

        Button find_button = findViewById(R.id.find_button);
        find_button.setOnClickListener(view -> {
            data.clear();
            update();
            lv.setAdapter(adapter);
        });
    }

    public void startCamera(View view) {
        Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camIntent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            image = (Bitmap) extras.get("data");
            largeView.setImageBitmap(image);
        }
    }


    String myVisionTester() throws IOException {
        //1. ENCODE image.
        Bitmap bitmap = image;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bout);
        Image myimage = new Image();
        myimage.encodeContent(bout.toByteArray());

        //2. PREPARE AnnotateImageRequest
        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
        annotateImageRequest.setImage(myimage);
        Feature f = new Feature();
        f.setType("LABEL_DETECTION");
        f.setMaxResults(5);
        List<Feature> lf = new ArrayList<Feature>();
        lf.add(f);
        annotateImageRequest.setFeatures(lf);

        //3.BUILD the Vision
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(new VisionRequestInitializer(API_KEY));
        Vision vision = builder.build();

        //4. CALL Vision.Images.Annotate
        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
        List<AnnotateImageRequest> list = new ArrayList<AnnotateImageRequest>();
        list.add(annotateImageRequest);
        batchAnnotateImagesRequest.setRequests(list);
        Vision.Images.Annotate task = vision.images().annotate(batchAnnotateImagesRequest);
        BatchAnnotateImagesResponse response = task.execute();

        List<String> tags = new ArrayList<>();
        for (AnnotateImageResponse res : response.getResponses()){
            if(res.getLabelAnnotations() != null){
                for(EntityAnnotation label : res.getLabelAnnotations()){
                    if(label.getScore() > 0.85){
                        tags.add(label.getDescription());
                    }
                }

                if(tags.isEmpty() && !res.getLabelAnnotations().isEmpty()) {
                    tags.add(res.getLabelAnnotations().get(0).getDescription());
                }
            }
        }
        Log.v("MYTAG", response.toPrettyString());
        return ""+tags;
    }

    private void update() {

        EditText findEdit = findViewById(R.id.FindEdit);
        String find = findEdit.getText().toString().trim();

        SQLiteDatabase mydb = this.openOrCreateDatabase("mydb", Context.MODE_PRIVATE, null);


        Cursor c;
        if (find.isEmpty()) {
            c = mydb.rawQuery("SELECT * FROM DATA ORDER BY DATE DESC", null);

        } else {
            c = mydb.rawQuery("SELECT * FROM DATA WHERE TAGS LIKE ? ORDER BY DATE DESC",
                    new String[]{"%" + find + "%"});
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

    private byte[] blobHelper(Bitmap b){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private String getDate(){
        SimpleDateFormat date = new SimpleDateFormat("MMM-d HH:mm:ss a", Locale.getDefault());
        return date.format(new Date());
    }
}