package com.example.joinus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class SearchResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        String keyword = getIntent().getExtras().getString("keyword");

        TextView result = findViewById(R.id.search_result_txt);
        ImageButton map = findViewById(R.id.search_map_btn);

        result.setText("Results by searching " + keyword);

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.search_result_fragement, new ListFragment()).commit();
        }

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.search_result_fragement, new MapFragment()).commit();
            }
        });
    }
}