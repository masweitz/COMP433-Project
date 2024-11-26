package com.example.assignment5;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<Item> {

    ListAdapter(Context context, int resource, ArrayList<Item> objects){
        super(context, resource, objects);
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        Item currentItem = getItem(position);

        ImageView image = convertView.findViewById(R.id.Image);
        TextView name = convertView.findViewById(R.id.Name);

        image.setImageBitmap(currentItem.imageResource);
        name.setText(currentItem.name);

        return convertView;
    }
}