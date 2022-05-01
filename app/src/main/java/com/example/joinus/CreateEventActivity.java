package com.example.joinus;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.location.Address;
import android.location.Geocoder;
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

import com.example.joinus.model.Event;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity {

    public static final String TAG = "CreateEvent";
    private static String SERVER_KEY;

    EditText eventName_tv, eventDescription_tv, eventLocation_tv;
    TextView eventDate_tv, eventTime_tv;
    ImageView eventImg;
    ProgressBar pb;
    Button create_btn;
    RadioGroup rg;

    private Calendar calendar1;
    private boolean selectedDate, selectedTime;
    private GeoPoint eventLocation;
    private String eventTopic;
    UUID uuid;
    Uri imgURi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        eventName_tv = findViewById(R.id.create_event_name);
        eventDescription_tv = findViewById(R.id.create_event_description);
        eventLocation_tv = findViewById(R.id.create_event_location);
        eventDate_tv = findViewById(R.id.create_event_date);
        eventTime_tv = findViewById(R.id.create_event_date_time);
        eventImg = findViewById(R.id.create_event_img);
        pb = findViewById(R.id.create_event_img_pb);
        create_btn = findViewById(R.id.create_event);
        rg = findViewById(R.id.create_event_topic);

        uuid = UUID.randomUUID();

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

        eventImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");
            }
        });

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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

        create_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInfo()){

                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();

                    //get the eventInfo
                    String currentUserId = mAuth.getCurrentUser().getUid();
                    String eventId = uuid.toString();
                    String eventName = eventName_tv.getText().toString().trim();
                    String eventDescription = eventDescription_tv.getText().toString().trim();
                    String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(eventLocation.getLatitude(), eventLocation.getLongitude()));

                    Event newEvent = new Event(eventId, eventName,
                            eventLocation, currentUserId ,new Timestamp(calendar1.getTime()),imgURi.toString(),1,eventTopic, eventDescription, hash);

                    database.collection("events").document(eventId).set(newEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(),"Create Event Successfully!", Toast.LENGTH_SHORT).show();
                            Event newEvent1 = new Event(eventId, eventName, eventLocation, new Timestamp(calendar1.getTime()), imgURi.toString());
                            sendEventNotification(eventTopic,eventId,eventName);
                            database.collection("users").document(currentUserId).collection("events").document(eventId).set(newEvent1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(),"Add Event Successfully!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    imgURi = uri;
                    eventImg.setImageURI(imgURi);
                    uploadImg();
                }
            });

    public GeoPoint getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        GeoPoint p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            p1 = new GeoPoint(location.getLatitude(),location.getLongitude());

            Log.d(TAG,p1.toString());

            return p1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void uploadImg(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference();
        StorageReference profileReference = reference.child("images/" + uuid);

        if(imgURi != null){
          create_btn.setClickable(false);
            pb.setVisibility(View.VISIBLE);
            profileReference.putFile(imgURi)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Toast.makeText(getApplicationContext(),"Cannot Upload", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imgURi = uri;
                                    pb.setVisibility(View.INVISIBLE);
                                    create_btn.setClickable(true);
                                }
                            });
                        }
            });
        }
    }

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

    private boolean checkInfo(){
        String eventAddress = eventLocation_tv.getText().toString().trim();

        if(!TextUtils.isEmpty(eventAddress)){
            eventLocation = getLocationFromAddress(eventAddress);
            Log.d(TAG,eventLocation.toString());

            if(eventLocation == null){
                Toast.makeText(getApplicationContext(),"Please enter valid Event Address", Toast.LENGTH_SHORT).show();
                return false;
            }

        }else{
            Toast.makeText(getApplicationContext(),"Please enter Event Address", Toast.LENGTH_SHORT).show();
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