package com.example.joinus.search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.joinus.R;
import com.example.joinus.Util.Utils;
import com.example.joinus.model.Event;
import com.example.joinus.ShareViewModel.ShareViewModelResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private ShareViewModelResult model;
    private List<Event> result;
    private Double lat, lon;
    private int distance;

    //Google Map
    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        lat = getActivity().getIntent().getExtras().getDouble("lat");
        lon = getActivity().getIntent().getExtras().getDouble("lon");
        if(getArguments() != null){
            distance = getArguments().getInt("distance");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.search_map_result);
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(ShareViewModelResult.class);
        result = model.getList().getValue();
        Log.d("test map", result.toString());

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setOnInfoWindowClickListener(this);

        LatLng currentLocation = new LatLng(lat, lon);
        Integer zoomLevel = 15;
        if(distance > 10){
            zoomLevel = 5;
        }

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(currentLocation)
                .title("Your Location"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));

        for(Event e : result){
            LatLng location = new LatLng(e.getEventLocation().getLatitude(),e.getEventLocation().getLongitude());
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(e.getEventName()));
            marker.setTag(e);
        }
        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdpater());
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        if(marker.getTitle().equals("Your Location")) {
            return;
        }
        Event event = (Event) marker.getTag();
        Intent intent = new Intent(getActivity().getApplicationContext(), EventDetailActivity.class);
        intent.putExtra("eventId", event.getEventId());
        startActivity(intent);
    }

    class CustomInfoWindowAdpater implements GoogleMap.InfoWindowAdapter {
        private final View markerview;

        CustomInfoWindowAdpater() {
            markerview = getLayoutInflater()
                    .inflate(R.layout.custom_info_window, null);
        }

        public View getInfoWindow(Marker marker) {
            if(marker.getTitle().equals("Your Location")){
                return null;
            }else {
                render(marker, markerview);
                return markerview;
            }
        }

        public View getInfoContents(Marker marker) {
            return null;
        }

        private void render(Marker marker, View view) {
            Event event = (Event) marker.getTag();
            TextView info_window_name = view.findViewById(R.id.info_window_name);
            TextView info_window_date = view.findViewById(R.id.info_window_date);
            TextView info_window_description = view.findViewById(R.id.info_window_description);

            info_window_name.setText("Event Name: " + event.getEventName());
            info_window_date.setText("Event Date: "+ Utils.formatDate(event.getEventDate().toDate()));
            info_window_description.setText("Event Description: "+ event.getEventDescription());
        }
    }

}
