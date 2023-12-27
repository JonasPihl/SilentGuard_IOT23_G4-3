package com.example.face_detection_application;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.face_detection_application.databinding.ActivityMainBinding;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String serverAdress = "http://192.168.1.174:5000/connect";  // TODO Replace with Pi's IP    private OkHttpClient client;
    private OkHttpClient client;
    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_stream, R.id.navigation_log).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        client = new OkHttpClient();

        Request request = new Request.Builder().url(serverAdress).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                System.out.println("recived psu notification" + text);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
            }
        });


    }

    protected void onDestroy(){
        super.onDestroy();
        if (webSocket != null){
            webSocket.close(1000, "Activcity closed");
        }
    }



}