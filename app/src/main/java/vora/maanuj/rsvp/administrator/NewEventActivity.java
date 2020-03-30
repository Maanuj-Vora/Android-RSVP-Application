package vora.maanuj.rsvp.administrator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import vora.maanuj.rsvp.R;

public class NewEventActivity extends AppCompatActivity {

    Button submit;
    AutoCompleteTextView eventName;
    MultiAutoCompleteTextView eventInfo;
    CalendarView calendarView;
    EditText startTime, endTime;
    ToggleButton startEra, endEra;
    Chip rsvp;

    int dayint, monthint, yearint;

    private ProgressDialog progressDialog;

    private FirebaseFirestore mFirestore;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser username;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        mFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        eventName = (AutoCompleteTextView) findViewById(R.id.eventName);
        eventInfo = (MultiAutoCompleteTextView) findViewById(R.id.eventInfo);
        calendarView = (CalendarView) findViewById(R.id.calendarView);
        startTime = (EditText) findViewById(R.id.startTime);
        endTime = (EditText) findViewById(R.id.endTime);
        startEra = (ToggleButton) findViewById(R.id.startEra);
        endEra = (ToggleButton) findViewById(R.id.endEra);
        rsvp = (Chip) findViewById(R.id.rsvp);
        submit = (Button) findViewById(R.id.submitButton);
        progressDialog = new ProgressDialog(this);


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

    public void submit(View v){

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

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);
        simpleDateFormat.setLenient(false);

        String date = (simpleDateFormat.format(calendar.getTime()));

        Map<String, Object> eventInformation = new HashMap<>();

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
        eventInformation.put("numRSVP", 0);

        db.collection("Events")
                .add(eventInformation)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Tag", "DocumentSnapshot written with ID: " + documentReference.getId());

                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        progressDialog.dismiss();
                        Toast.makeText(NewEventActivity.this,
                                "Updated To Server",
                                Toast.LENGTH_SHORT)
                                .show();

                        finish();
                        Intent i = new Intent(NewEventActivity.this, AdminActivity.class);
                        startActivity(i);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(NewEventActivity.this,
                                "Error Updating To Server, Please Try Again In A Little While",
                                Toast.LENGTH_LONG)
                                .show();
                        Log.w("Tag", "Error adding document", e);
                    }
                });

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }
}
