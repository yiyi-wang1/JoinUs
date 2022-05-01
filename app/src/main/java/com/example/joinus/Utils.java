package com.example.joinus;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.joinus.model.Event;
import com.example.joinus.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import android.os.Handler;

public class Utils {

    public static final String TAG = "Utils";
    public static final String DEFAULTIMAGE = "DEFAULTIMAGE";
    public static final String USER_ERROR = "The user does not exists";
    public static final String[] TOPICS = new String[]{"Music", "Sports"};

    public final static User getUserData (String uid, Context context){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference reference = database.collection("users").document(uid);
        User currentUser = new User();
//        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot snapshot = task.getResult();
//                    //Log.d(TAG, "DocumentSnapshot data: " + task.getResult().toString());
//                    if (snapshot.exists()) {
//                        //Log.d(TAG, "DocumentSnapshot data: " + snapshot.getData());
//                        currentUser.setUid(uid);
//                        currentUser.setUsername(snapshot.getData().get("username").toString());
//                        currentUser.setEmail(snapshot.getData().get("email").toString());
//                        currentUser.setProfileImg(snapshot.getData().get("profileImg").toString());
//                        currentUser.setVerified((boolean) snapshot.getData().get("verified"));
//                        currentUser.setLocation((GeoPoint) snapshot.getData().get("location"));
//                        currentUser.setChatList((List<String>) snapshot.getData().get("chatList"));
//                        currentUser.setInterestedTopics((List<String>) snapshot.getData().get("interestedTopics"));
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });

        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    currentUser.setUid(uid);
                    currentUser.setUsername(snapshot.getData().get("username").toString());
                    currentUser.setEmail(snapshot.getData().get("email").toString());
                    currentUser.setProfileImg(snapshot.getData().get("profileImg").toString());
                    currentUser.setVerified((boolean) snapshot.getData().get("verified"));
                    currentUser.setLocation((GeoPoint) snapshot.getData().get("location"));
                    currentUser.setInterestedTopics((List<String>) snapshot.getData().get("interestedTopics"));
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        //get the event list
        List<Event> eventList = new ArrayList<>();
        Query query = database.collection("users").document(uid).collection("events").orderBy("eventDate");
//        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(task.isSuccessful()){
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Event event = document.toObject(Event.class);
//                        eventList.add(event);
//                        Log.d(TAG + "test", document.getId() + " => " + document.getData());
//                    }
//                }
//            }
//        });

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (snapshot != null) {
                    for (QueryDocumentSnapshot document : snapshot) {
                        Event event = document.toObject(Event.class);
                        eventList.add(event);
                        Log.d(TAG + "test", event.getEventName());
                    }
                    currentUser.setEventList(eventList);
                    Log.d(TAG + "eventlist", eventList.toString());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
        return currentUser;
    }

    public static final String formatDate (Date date){
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", date).toString();
    }

    public static final void updateToken(String refreshedToken){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth == null || mAuth.getCurrentUser() == null){
            return;
        }else{
            String currentUid = mAuth.getCurrentUser().getUid();
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference userRef = database.collection("users").document(currentUid);
            database.runTransaction(new Transaction.Function<Void>() {
                public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(userRef);
                    transaction.update(userRef, "fcmToken", refreshedToken);
                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.w(TAG, "Transaction successfully");
                    Log.d(TAG + "new Token",refreshedToken);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Transaction failure.", e);
                }
            });
        }
    }


    public static void resetSubscription (List<String> list, Context context){

        for(String topic: list){
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "You subscribed " + topic;
                            if (!task.isSuccessful()) {
                                msg = "Failed to subscribe " + topic;
                            }
                            Log.d(TAG, msg);
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        for(String unsubscribed: Utils.TOPICS){
            if(!list.contains(unsubscribed)){
                FirebaseMessaging.getInstance().unsubscribeFromTopic(unsubscribed)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String msg = "You unsubscribed " + unsubscribed;
                                if (!task.isSuccessful()) {
                                    msg = "Failed to unsubscribe " + unsubscribed;
                                }
                                Log.d(TAG, msg);
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    public static String fcmHttpConnection(String serverToken, JSONObject jsonObject) {
        try {

            // Open the HTTP connection and send the payload
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", serverToken);
            conn.setDoOutput(true);

            // Send FCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jsonObject.toString().getBytes());
            outputStream.close();

            // Read FCM response.
            InputStream inputStream = conn.getInputStream();
            return convertStreamToString(inputStream);
        } catch (IOException e) {
            return "NULL";
        }

    }

    public static String convertStreamToString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String len;
            while ((len = bufferedReader.readLine()) != null) {
                stringBuilder.append(len);
            }
            bufferedReader.close();
            return stringBuilder.toString().replace(",", ",\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Properties getProperties(Context context)  {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open("firebase.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    public static void postToastMessage(final String message, final Context context){
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
