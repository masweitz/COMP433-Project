package com.example.COMP433_Project;

import android.graphics.Bitmap;

public class Item {
    Bitmap imageResource;
    String name;
    Boolean check;
    Item(Bitmap imageResource, String name) {
        this.imageResource = imageResource;
        this.name = name;
        this.check = false;
    }

    public void setChecked(boolean checked) {
        check = checked;
    }
}
