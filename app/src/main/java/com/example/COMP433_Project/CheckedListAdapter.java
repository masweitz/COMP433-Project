package com.example.COMP433_Project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckedListAdapter extends ArrayAdapter<Item> {

    private Context context;
    private ArrayList<Item> objects;
    private TextView text;
    private int selectedCount = 0;

    CheckedListAdapter(Context context, int resource, ArrayList<Item> objects, TextView text){
        super(context, resource, objects);
        this.objects = objects;
        this.text = text;
        this.context = context;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.checkedlist_item, parent, false);
        }
        Item currentItem = getItem(position);

        CheckBox checkBox = convertView.findViewById(R.id.listcheck);
        ImageView image = convertView.findViewById(R.id.Image);
        TextView name = convertView.findViewById(R.id.Name);


        image.setImageBitmap(currentItem.imageResource);
        name.setText(currentItem.name);
        checkBox.setChecked(currentItem.check);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->{
            if (isChecked) {
                if (selectedCount < 3) {
                    currentItem.setChecked(true);
                    selectedCount++;
                } else {
                    checkBox.setChecked(false);
                    Toast.makeText(context, "You can only select up to 3 items.", Toast.LENGTH_SHORT).show();
                }
            } else {
                currentItem.setChecked(false);
                selectedCount--;
            }
            updateSelectedText();
        });

        return convertView;
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedText() {
        ArrayList<String> selectedItems = new ArrayList<>();
        for(Item item : objects) {
            if(item.check){
                selectedItems.add(BracketExtractor(item.name));
            }
        }
        text.setText("You Selected: " + String.join(", ", selectedItems));
    }

    private String BracketExtractor(String input){
        Pattern pattern = Pattern.compile("\\[(.*?)]");
        Matcher matcher = pattern.matcher(input);

        ArrayList<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        return matches.get(0);
    }
}
