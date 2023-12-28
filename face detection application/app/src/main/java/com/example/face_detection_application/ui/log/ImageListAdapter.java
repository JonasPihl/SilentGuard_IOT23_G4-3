package com.example.face_detection_application.ui.log;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.face_detection_application.databinding.LogsItemBinding;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class ImageListAdapter extends ArrayAdapter<Map.Entry<String, String>> {

    private final Context context;

    public ImageListAdapter(Context context, List<Map.Entry<String, String>> entries) {
        super(context, 0, entries);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogsItemBinding binding;

        if (convertView == null) {
            binding = LogsItemBinding.inflate(LayoutInflater.from(context), parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (LogsItemBinding) convertView.getTag();
        }

        Map.Entry<String, String> entry = getItem(position);

        if (entry != null) {
            String filename = entry.getKey().replace(".jpg", "");
            String imageUrl = entry.getValue();

            // Load image using Picasso, an Image Loading library
            Picasso.get().load(imageUrl).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(binding.imageView);

            // Set the filename as text
            binding.textView.setText(filename);
        }

        return convertView;
    }
}