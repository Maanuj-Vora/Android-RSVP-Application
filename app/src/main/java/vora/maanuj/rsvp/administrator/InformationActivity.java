package vora.maanuj.rsvp.administrator;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import vora.maanuj.rsvp.R;
public class InformationActivity extends AppCompatActivity {


        Button submit;
        AutoCompleteTextView eventName;
        MultiAutoCompleteTextView eventInfo;
        CalendarView calendarView;
        EditText startTime, endTime;
        ToggleButton startEra, endEra;
        Chip rsvp;
        String docName;
        TextView rsvpNum;

        int dayint, monthint, yearint;

        String date;

        private ProgressDialog progressDialog;

        private FirebaseFirestore mFirestore;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        private FirebaseUser username;
        private FirebaseAuth auth;
        private FirebaseAuth.AuthStateListener authListener;

        DocumentSnapshot docInfo;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_information);

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Retrieving Information From The Cloud..." +
                    "\n Be Sure To Have An Active Internet Connection");
            progressDialog.show();

            docName = getIntent().getStringExtra("doc");
            mFirestore = FirebaseFirestore.getInstance();

            eventName = (AutoCompleteTextView) findViewById(R.id.eventName);
            eventInfo = (MultiAutoCompleteTextView) findViewById(R.id.eventInfo);
            calendarView = (CalendarView) findViewById(R.id.calendarView);
            startTime = (EditText) findViewById(R.id.startTime);
            endTime = (EditText) findViewById(R.id.endTime);
            startEra = (ToggleButton) findViewById(R.id.startEra);
            endEra = (ToggleButton) findViewById(R.id.endEra);
            rsvp = (Chip) findViewById(R.id.rsvp);
            submit = (Button) findViewById(R.id.submitButton);
            rsvpNum = (TextView) findViewById(R.id.rsvped);


            db.collection("Events")
                    .document(docName)
                    .get(Source.SERVER)
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (Objects.requireNonNull(document).exists()) {
                                    Log.d("Tag", "DocumentSnapshot data: " + document.getData());

                                    docInfo = document;
                                    eventName.setText(document.get("name").toString());
                                    eventInfo.setText(document.get("info").toString());

                                    date = document.get("date").toString();
                                    String parts[] = date.split("/");
                                    int day = Integer.parseInt(parts[1]);
                                    int month = Integer.parseInt(parts[0]) - 1;
                                    int year = Integer.parseInt(parts[2]);
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.set(Calendar.YEAR, year);
                                    calendar.set(Calendar.MONTH, month);
                                    calendar.set(Calendar.DAY_OF_MONTH, day);
                                    long milliTime = calendar.getTimeInMillis();
                                    calendarView.setDate (milliTime, true, true);

                                    if(document.get("endEra").toString().equals("PM")){
                                        endEra.setChecked(true);
                                    } else{
                                        endEra.setChecked(false);
                                    }

                                    if(document.get("startEra").toString().equals("PM")){
                                        startEra.setChecked(true);
                                    } else{
                                        startEra.setChecked(false);
                                    }

                                    rsvp.setChecked(Boolean.parseBoolean(document.get("rsvp").toString()));
                                    startTime.setText(document.get("startTime").toString());
                                    endTime.setText(document.get("endTime").toString());

                                    rsvpNum.setText("Number of RSVP People is: " + document.get("numRSVP"));


                                } else {
                                    Log.d("Tag", "No such document");

                                }
                            } else {
                                Log.d("Tag", "get failed with ", task.getException());

                            }

                            progressDialog.dismiss();


                        }
                    });

            calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                //show the selected date as a toast
                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
//                Toast.makeText(getApplicationContext(), day + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
                    dayint = day;
                    yearint = year;
                    monthint = month + 1;
                }
            });

        }

    public void submit(View view) {

        progressDialog.setMessage("Adding Event Information Into Server...");
        progressDialog.show();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        String eventName1 = "";
        String eventInfo1 = "";
        String startTime1 = "";
        String endTime1 = "";


        if(this.eventName.getText().toString().isEmpty()){
            eventName1 = "";
        } else {
            eventName1 = this.eventName.getText().toString();
        }

        if(this.eventInfo.getText().toString().isEmpty()){
            eventInfo1 = "";
        } else {
            eventInfo1 = this.eventInfo.getText().toString();
        }

        if(this.startTime.getText().toString().isEmpty()){
            startTime1 = "";
        } else {
            startTime1 = this.startTime.getText().toString();
        }

        if(this.endTime.getText().toString().isEmpty()){
            endTime1 = "";
        } else {
            endTime1 = this.endTime.getText().toString();
        }

        String startEra1;
        String endEra1;

        boolean rsvp = this.rsvp.isChecked();

        if(this.startEra.isChecked()){
            startEra1 = "PM";
        } else{
            startEra1 = "AM";
        }

        if(this.endEra.isChecked()){
            endEra1 = "PM";
        } else{
            endEra1 = "AM";
        }



        Map<String, Object> eventInformation = new HashMap<>();

//        if((dayint == 0) && (monthint == 0) && (yearint == 0)){
//            eventInformation.put("date", date);
//        }
//        else{
//            eventInformation.put("date", dayint + "/" + monthint + "/" + yearint);
//        }

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);
        simpleDateFormat.setLenient(false);

        String date = (simpleDateFormat.format(calendar.getTime()));

        if((dayint == 0) && (monthint == 0) && (yearint == 0)){
            eventInformation.put("date", date);
        }
        else{
            String monthStr = monthint + "";
            String dayStr = dayint + "";
            String yearStr = yearint + "";

            if((monthint == 1) || (monthint == 2) || (monthint == 3) || (monthint == 4) || (monthint == 5) ||
                    (monthint == 6) || (monthint == 7) || (monthint == 8) || (monthint == 9)){
                monthStr = "0" + monthStr;
            }

            if((dayint == 1) || (dayint == 2) || (dayint == 3) || (dayint == 4) || (dayint == 5) ||
                    (dayint == 6) || (dayint == 7) || (dayint == 8) || (dayint == 9)){
                dayStr = "0" + dayStr;
            }


            eventInformation.put("date", monthStr + "/" + dayStr + "/" + yearStr);
        }

        eventInformation.put("info", eventInfo1);
        eventInformation.put("name", eventName1);
        eventInformation.put("rsvp", rsvp);
        eventInformation.put("startTime", startTime1);
        eventInformation.put("endTime", endTime1);
        eventInformation.put("startEra", startEra1);
        eventInformation.put("endEra", endEra1);

            db.collection("Events")
                    .document(docName)
                    .update(eventInformation)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("", "DocumentSnapshot successfully updated!");
                            progressDialog.dismiss();
                            Toast.makeText(InformationActivity.this,
                                    "Updated To Server",
                                    Toast.LENGTH_SHORT)
                                    .show();

                            finish();
                            Intent i = new Intent(InformationActivity.this, AdminActivity.class);
                            startActivity(i);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("", "Error updating document", e);
                            progressDialog.dismiss();
                            Toast.makeText(InformationActivity.this,
                                    "Error Updating To Server, Please Try Again In A Little While",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });


    }
}
