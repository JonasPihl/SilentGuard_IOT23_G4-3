package com.example.face_detection_application.ui.log;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.face_detection_application.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class ImageListAdapter extends ArrayAdapter<Map.Entry<String, String>> {

    private final Context context;

    public ImageListAdapter(Context context, List<Map.Entry<String, String>> entries) {
        super(context, R.layout.logs_item, entries);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.logs_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageView);
        TextView textView = convertView.findViewById(R.id.textView);

        Map.Entry<String, String> entry = getItem(position);

        if (entry != null) {
            String filename = entry.getKey().replace(".jpg", "");
            String imageUrl = entry.getValue();

            // Load image using Picasso, a Image Loading library
            Picasso.get().load(imageUrl).into(imageView);

            // Set the filename as text
            textView.setText(filename);
        }

        return convertView;
    }


}
