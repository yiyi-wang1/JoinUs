package com.example.joinus.views.home;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.joinus.R;
import com.example.joinus.Util.Utils;
import com.example.joinus.model.Event;
import com.example.joinus.viewmodel.EventViewModel;
import com.example.joinus.viewmodel.ImageViewModel;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity {

    public static final String TAG = "CreateEvent";
    private static String SERVER_KEY;
    private static String API_KEY;

    EditText eventName_tv, eventDescription_tv, eventLocation_tv;
    TextView eventDate_tv, eventTime_tv;
    ImageView eventImg;
    ProgressBar pb;
    Button create_btn;
    RadioGroup radioGroup;

    private Calendar calendar1;
    private boolean selectedDate, selectedTime;
    private GeoPoint eventLocation;
    private String eventTopic;
    private UUID uuid;
    private Uri imgURi;
    private ImageViewModel imageViewModel;
    private EventViewModel eventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        //uuid for the event id
        uuid = UUID.randomUUID();

        eventName_tv = findViewById(R.id.create_event_name);
        eventDescription_tv = findViewById(R.id.create_event_description);
        eventLocation_tv = findViewById(R.id.create_event_location);
        eventDate_tv = findViewById(R.id.create_event_date);
        eventTime_tv = findViewById(R.id.create_event_date_time);
        eventImg = findViewById(R.id.create_event_img);
        pb = findViewById(R.id.create_event_img_pb);
        create_btn = findViewById(R.id.create_event);
        radioGroup = findViewById(R.id.create_event_topic);

        // Initialize the Place SDK
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            API_KEY = bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        Places.initialize(getApplicationContext(), API_KEY);
        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);

        //ViewModel
        imageViewModel = new ViewModelProvider(this).get(ImageViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        //Select Date and Time
        selectedDate = false;
        selectedTime = false;

        calendar1 = Calendar.getInstance();
        eventDate_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        eventTime_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
                Log.d(TAG, new Timestamp(calendar1.getTime()).toString());

            }
        });

        //Get the location information
        eventLocation_tv.setFocusable(false);
        eventLocation_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Set the fields to specify which types of place data to
                // return after the user has made a selection.
                List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(CreateEventActivity.this);
                autocompleteLauncher.launch(intent);
            }
        });

        //Select the image
        eventImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });

        //Select the Topic from radio group
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.create_event_topic_music:
                        eventTopic = "Music";
                        break;
                    case R.id.create_event_topic_sports:
                        eventTopic = "Sports";
                        break;
                }
            }
        });

        //Create event
        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInfo()){

                    //get the eventInfo
                    String eventId = uuid.toString();
                    String eventName = eventName_tv.getText().toString().trim();
                    String eventDescription = eventDescription_tv.getText().toString().trim();

                    eventViewModel.createEvent(eventId,eventName,eventLocation,new Timestamp(calendar1.getTime()),imgURi.toString(),eventTopic,eventDescription);
                    eventViewModel.getEventMutableLiveData().observe(CreateEventActivity.this, new Observer<Event>() {
                        @Override
                        public void onChanged(Event event) {
                            sendEventNotification(event.getEventTopic(),event.getEventId(),event.getEventName());
                        }
                    });
                    finish();
                }
            }
        });
    }

    //Start the auto-complete fragment
    ActivityResultLauncher<Intent> autocompleteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        //set the location
                        eventLocation_tv.setText(place.getAddress());
                        eventLocation = new GeoPoint(place.getLatLng().latitude,place.getLatLng().longitude);
                    }
                }
            });


    //Start the content selection
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    imgURi = uri;
                    eventImg.setImageURI(imgURi);
                    uploadImg();
                }
            });


    private void uploadImg(){
        if(imgURi != null){
            create_btn.setClickable(false);
            pb.setVisibility(View.VISIBLE);
            imageViewModel.uploadImg(uuid,imgURi);
            imageViewModel.getUriMutableLiveData().observe(this, new Observer<Uri>() {
                @Override
                public void onChanged(Uri uri) {
                    if(uri != null){
                        imgURi = uri;
                    }
                }
            });
            imageViewModel.getIsLoadingMutableLiveData().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if(!aBoolean){
                        pb.setVisibility(View.INVISIBLE);
                        create_btn.setClickable(true);
                    }
                }
            });
        }
    }

    //Show the Date Dialog
    private void showDatePickerDialog(){
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int date) {

                calendar1.set(Calendar.YEAR, year);
                calendar1.set(Calendar.MONTH, month);
                calendar1.set(Calendar.DATE, date);
                String dateText = DateFormat.format("EEEE, MMM d, yyyy", calendar1).toString();
                eventDate_tv.setText(dateText);
                selectedDate = true;

            }
        }, YEAR, MONTH, DATE);
        datePickerDialog.show();
    }

    //Show the Time Dialog
    private void showTimePickerDialog(){
        Calendar calendar = Calendar.getInstance();
        int HOUR = calendar.get(Calendar.HOUR);
        int MINUTE = calendar.get(Calendar.MINUTE);
        boolean is24HourFormat = DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar1.set(Calendar.HOUR, hour);
                calendar1.set(Calendar.MINUTE, minute);
                String dateText = DateFormat.format("h:mm a", calendar1).toString();
                eventTime_tv.setText(dateText);
                selectedTime = true;
            }
        }, HOUR, MINUTE, is24HourFormat);
        timePickerDialog.show();
    }

    //Helper method to check input
    private boolean checkInfo(){
        String eventAddress = eventLocation_tv.getText().toString().trim();

        if(TextUtils.isEmpty(eventAddress)) {
            Toast.makeText(getApplicationContext(), "Please enter Event Address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!selectedDate || !selectedTime){
            Toast.makeText(getApplicationContext(),"Please select Date and Time", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(eventName_tv.getText().toString().trim())){
            Toast.makeText(getApplicationContext(),"Please enter Event Name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(TextUtils.isEmpty(eventDescription_tv.getText().toString().trim())){
            Toast.makeText(getApplicationContext(),"Please enter Event Description", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(eventTopic == null){
            Toast.makeText(getApplicationContext(),"Please Select Event Topic", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(imgURi == null){
            Toast.makeText(getApplicationContext(),"Please Select Event Image", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void sendEventNotification(String eventTopic,String eventId,String eventName){
        // Prepare data
        JSONObject jPayload = new JSONObject();
        JSONObject jdata = new JSONObject();
        try {
            String title = "New event for " + eventTopic + " just created!";
            String content = "Event Name: " + eventName;


            jdata.put("title", title);
            jdata.put("body", content);
            jdata.put("eventId",eventId);

            // Populate the Payload object.
            // Note that "to" is a topic, not a token representing an app instance
            jPayload.put("to", "/topics/"+ eventTopic);
            jPayload.put("priority", "high");
            jPayload.put("data", jdata);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                SERVER_KEY = "key=" + Utils.getProperties(getApplicationContext()).getProperty("SERVER_KEY");
                final String resp = Utils.fcmHttpConnection(SERVER_KEY, jPayload);
                Utils.postToastMessage("Status from Server: " + resp, getApplicationContext());
            }
        }).start();
    }
}