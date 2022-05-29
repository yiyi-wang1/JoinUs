package com.example.joinus.search;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.joinus.R;

public class SearchResultActivity extends AppCompatActivity {
    private Integer distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        TextView result = findViewById(R.id.search_result_txt);
        TextView distance_tv = findViewById(R.id.search_result_txt_distance);
        ImageButton map = findViewById(R.id.search_map_btn);
        SeekBar seekBar = findViewById(R.id.search_seekbar);

        String keyword = getIntent().getExtras().getString("keyword");

        distance = 5;
        seekBar.setMax(50);
        seekBar.setProgress(distance);

        result.setText("Results by searching " + keyword);
        distance_tv.setText("within " + distance + " KM");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                distance = i;
                distance_tv.setText("within " + distance + " KM");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateDistance(distance);
            }
        });

        if(savedInstanceState == null){
            Bundle bundle = new Bundle();
            bundle.putInt("distance", distance);
            Fragment fragment = new ListFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.search_result_fragement, fragment).commit();
        }

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("distance", distance);
                Fragment fragment = new MapFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.search_result_fragement, fragment).commit();
            }
        });
    }

    private void updateDistance(int i) {
        Bundle bundle = new Bundle();
        bundle.putInt("distance", distance);
        Fragment fragment = new ListFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.search_result_fragement, fragment).commit();
    }
}