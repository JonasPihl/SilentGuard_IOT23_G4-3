package com.example.face_detection_application.ui.stream;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;



import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.example.face_detection_application.databinding.FragmentStreamBinding;

public class StreamFragment extends Fragment {

    private FragmentStreamBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        StreamViewModel streamViewModel =
                new ViewModelProvider(this).get(StreamViewModel.class);

        binding = FragmentStreamBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        streamViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        super.onCreate(savedInstanceState);
        // setContentView(binding.activity);

        VideoView videoView = binding.videoView;
        String videoUrl = "http://raspberry_pi_ip:5000/video";  // Replace with your Raspberry Pi's IP

        VideoStreamTask videoStreamTask = new VideoStreamTask(videoView);
        videoStreamTask.execute(videoUrl);

        return root;
    }

    private static class VideoStreamTask extends AsyncTask<String, Void, String> {
        private final VideoView videoView;

        public VideoStreamTask(VideoView videoView) {
            this.videoView = videoView;
        }

        @Override
        protected String doInBackground(String... params) {
            String videoUrl = params[0];
            return params[0];
        }

        @Override
        protected void onPostExecute(String result) {
            videoView.setVideoPath(result);
            videoView.start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}