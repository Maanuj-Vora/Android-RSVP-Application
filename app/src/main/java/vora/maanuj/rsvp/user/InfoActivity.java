package vora.maanuj.rsvp.user;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

import android.text.InputType;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import vora.maanuj.rsvp.R;

public class InfoActivity extends AppCompatActivity {

    Button submit;
    AutoCompleteTextView eventName;
    MultiAutoCompleteTextView eventInfo;
    CalendarView calendarView;
    EditText startTime, endTime;
    ToggleButton startEra, endEra;
    Chip rsvp;
    String docName;

    int dayint, monthint, yearint;

    String date;

    private ProgressDialog progressDialog;

    private FirebaseFirestore mFirestore;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser username;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    DocumentSnapshot docInfo;

    private String m_Text = "";

    private String hash;

    Map<String, Object> inform = new HashMap<>();

    int amountOfRSVP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        setContentView(R.layout.activity_info);

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
                                eventName.setEnabled(false);
                                eventName.setClickable(false);
                                eventInfo.setText(document.get("info").toString());
                                eventInfo.setEnabled(false);
                                eventInfo.setClickable(false);
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
                                calendarView.setDate(milliTime, true, true);
                                calendarView.setEnabled(false);
                                calendarView.setClickable(false);

                                if (document.get("endEra").toString().equals("PM")) {
                                    endEra.setChecked(true);
                                } else {
                                    endEra.setChecked(false);
                                }
                                endEra.setEnabled(false);
                                endEra.setClickable(false);

                                if (document.get("startEra").toString().equals("PM")) {
                                    startEra.setChecked(true);
                                } else {
                                    startEra.setChecked(false);
                                }
                                startEra.setEnabled(false);
                                startEra.setClickable(false);

                                rsvp.setChecked(Boolean.parseBoolean(document.get("rsvp").toString()));
                                startTime.setText(document.get("startTime").toString());
                                endTime.setText(document.get("endTime").toString());

                                rsvp.setEnabled(false);
                                rsvp.setClickable(false);

                                startTime.setEnabled(false);
                                startTime.setClickable(false);

                                endTime.setEnabled(false);
                                endTime.setClickable(false);

                                if(Boolean.parseBoolean(document.get("rsvp").toString())){
                                    submit.setEnabled(true);
                                    submit.setClickable(true);
                                } else{
                                    submit.setVisibility(View.INVISIBLE);
                                }

                                amountOfRSVP = Integer.parseInt(document.get("numRSVP").toString());

                            } else {
                                Log.d("Tag", "No such document");

                            }
                        } else {
                            Log.d("Tag", "get failed with ", task.getException());

                        }

                        progressDialog.dismiss();


                    }
                });
    }

    public void submit(View v){


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Number of People");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                updated();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    public void updated(){

        if(m_Text.isEmpty()){
            m_Text = "0";
        }

        progressDialog.setMessage("Loading...");
        progressDialog.show();

        username = FirebaseAuth.getInstance().getCurrentUser();
        String email = username.getEmail();

        db.collection("Users")
                .document(email)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (Objects.requireNonNull(document).exists()) {
                                Log.d("Tag", "DocumentSnapshot data: " + document.getData());

                                hash = document.get("hash").toString();


                                username = FirebaseAuth.getInstance().getCurrentUser();
                                String email = username.getEmail();

                                inform.put("name", username);
                                inform.put("id", hash);
                                inform.put("email", email);
                                inform.put("amount", Integer.parseInt(m_Text));

                                db.collection("Events")
                                        .document(docName)
                                        .collection("rsvp")
                                        .document(hash)
                                        .get(Source.SERVER)
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                               @Override
                                               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                   if (task.isSuccessful()) {
                                                       DocumentSnapshot document = task.getResult();
                                                       if(!document.exists()){
                                                           db.collection("Events")
                                                                   .document(docName)
                                                                   .collection("rsvp")
                                                                   .document(hash)
                                                                   .set(inform)
                                                                   .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                       @Override
                                                                       public void onSuccess(Void aVoid) {
                                                                           Log.d("", "DocumentSnapshot successfully written!");
                                                                       }
                                                                   })
                                                                   .addOnFailureListener(new OnFailureListener() {
                                                                       @Override
                                                                       public void onFailure(@NonNull Exception e) {
                                                                           Log.w("", "Error writing document", e);
                                                                       }
                                                                   });

                                                           Map<String, Object> eventInformation = new HashMap<>();
                                                           eventInformation.put("numRSVP", amountOfRSVP + Integer.parseInt(m_Text));


                                                           db.collection("Events")
                                                                   .document(docName)
                                                                   .update(eventInformation)
                                                                   .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                       @Override
                                                                       public void onSuccess(Void aVoid) {
                                                                           Log.d("", "DocumentSnapshot successfully updated!");
                                                                           progressDialog.dismiss();
                                                                           Toast.makeText(InfoActivity.this,
                                                                                   "Updated To Server",
                                                                                   Toast.LENGTH_SHORT)
                                                                                   .show();

                                                                           finish();
                                                                           Intent i = new Intent(InfoActivity.this, UserActivity.class);
                                                                           startActivity(i);
                                                                       }
                                                                   })
                                                                   .addOnFailureListener(new OnFailureListener() {
                                                                       @Override
                                                                       public void onFailure(@NonNull Exception e) {
                                                                           Log.w("", "Error updating document", e);
                                                                           progressDialog.dismiss();
                                                                           Toast.makeText(InfoActivity.this,
                                                                                   "Error Updating To Server, Please Try Again In A Little While",
                                                                                   Toast.LENGTH_LONG)
                                                                                   .show();
                                                                       }
                                                                   });

                                                       }
                                                       else{
                                                           Toast.makeText(InfoActivity.this, "You have already done the RSVP", Toast.LENGTH_SHORT).show();
                                                       }

                                                   }
                                               }
                                           });
                            } else {
                                Log.d("Tag", "No such document");

                            }
                        } else {
                            Log.d("Tag", "get failed with ", task.getException());

                        }


                        Toast.makeText(getApplicationContext(), "RSVP Successful", Toast.LENGTH_SHORT).show();

                        submit.setVisibility(View.INVISIBLE);

                    }
                });


        progressDialog.dismiss();

    }
}
