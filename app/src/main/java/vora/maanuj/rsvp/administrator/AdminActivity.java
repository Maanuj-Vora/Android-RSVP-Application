package vora.maanuj.rsvp.administrator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import vora.maanuj.rsvp.R;
import vora.maanuj.rsvp.auth.SplashScrActivity;

public class AdminActivity extends AppCompatActivity {
    private TextView mTextMessage;
    private ScrollView upcomingEvents;

    private FirebaseFirestore mFirestore;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser username;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    List<QueryDocumentSnapshot> docs = new ArrayList<>();
    List<String> name = new ArrayList<>();
    List<Integer> rsvpNum = new ArrayList<>();

    int rsvpNumber;

    private ProgressDialog progressDialog;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    home();
                    return true;
                case R.id.navigation_dashboard:
                    pastEvents();
                    return true;
                case R.id.navigation_notifications:
                    upcomingEvents();
                    return true;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        progressDialog = new ProgressDialog(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        mTextMessage = findViewById(R.id.message);
        upcomingEvents = (ScrollView) findViewById(R.id.upcomingEvents);
        upcomingEvents.setVisibility(View.INVISIBLE);

        home();

        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void upcomingEvents() {

        progressDialog.setMessage("Loading...");
        progressDialog.show();

        docs.clear();
        name.clear();
        rsvpNum.clear();

        LinearLayout ll = (LinearLayout) findViewById(R.id.eventList);
        if(((LinearLayout) ll).getChildCount() > 0)
            ((LinearLayout) ll).removeAllViews();

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);
        simpleDateFormat.setLenient(false);

        String formattedDate = simpleDateFormat.format(c.getTime());
        System.out.println(formattedDate);
        upcomingEvents.setVisibility(View.VISIBLE);

        db.collection("Events")
                .whereGreaterThanOrEqualTo("date", formattedDate)
                .orderBy("date", Query.Direction.ASCENDING)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Log.d("Tag", document.getId() + " => " + document.getData());
                            docs.add(document);
                        }
                        for (int x = 0; x < docs.size(); x++) {
                            Button myButton = new Button(getApplicationContext());
                            myButton.setText(docs.get(x).getString("name") + " - " + docs.get(x).getString("date"));
                            myButton.setId(x);
                            myButton.setOnClickListener(onClickListener);


                            name.add(docs.get(x).getId());
                            LinearLayout ll = (LinearLayout) findViewById(R.id.eventList);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            ll.addView(myButton, lp);
                        }
                        progressDialog.dismiss();
                    }
                });



    }

    private void pastEvents() {

        progressDialog.setMessage("Loading...");
        progressDialog.show();

        docs.clear();
        name.clear();
        rsvpNum.clear();
        LinearLayout ll = (LinearLayout) findViewById(R.id.eventList);
        if(((LinearLayout) ll).getChildCount() > 0)
            ((LinearLayout) ll).removeAllViews();

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);
        simpleDateFormat.setLenient(false);

        String formattedDate = simpleDateFormat.format(c.getTime());
        upcomingEvents.setVisibility(View.VISIBLE);

        db.collection("Events")
                .whereLessThanOrEqualTo("date", formattedDate)
                .orderBy("date", Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Log.d("Tag", document.getId() + " => " + document.getData());
                            docs.add(document);
                        }
                        for(int x = 0; x < docs.size(); x++){
                            Button myButton = new Button(getApplicationContext());
                            myButton.setText(docs.get(x).getString("name") + " - " + docs.get(x).getString("date"));
                            myButton.setId(x);
                            myButton.setOnClickListener(onClickListener);


                            name.add(docs.get(x).getId());
                            LinearLayout ll = (LinearLayout) findViewById(R.id.eventList);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            ll.addView(myButton, lp);
                        }
                        progressDialog.dismiss();
                    }
                });

    }

    private void home() {
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        docs.clear();
        name.clear();
        rsvpNum.clear();

        LinearLayout ll = (LinearLayout) findViewById(R.id.eventList);
        if(((LinearLayout) ll).getChildCount() > 0) {
            ((LinearLayout) ll).removeAllViews();
        }

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "MM/dd/yyyy", Locale.US);
        simpleDateFormat.setLenient(false);

        String formattedDate = simpleDateFormat.format(c.getTime());

        System.out.println(formattedDate);
        upcomingEvents.setVisibility(View.VISIBLE);


        db.collection("Events")
                .whereGreaterThanOrEqualTo("date", formattedDate)
                .orderBy("date", Query.Direction.ASCENDING)
                .get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Log.d("Tag", document.getId() + " => " + document.getData());
                            docs.add(document);
                        }
                        for (int x = 0; x < docs.size(); x++) {

                            Button myButton = new Button(getApplicationContext());
                            myButton.setText(docs.get(x).getString("name") + " - RSVP Number = " + docs.get(x).get("numRSVP"));
                            myButton.setId(x);

                            LinearLayout ll = (LinearLayout) findViewById(R.id.eventList);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            ll.addView(myButton, lp);
                        }
                        progressDialog.dismiss();
                    }
                });


    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            Intent i = new Intent(AdminActivity.this, InformationActivity.class);
            i.putExtra("doc", name.get(id).toString());
            startActivity(i);
        }
    };

    public void addEvent(View view) {
        Intent i = new Intent(AdminActivity.this, NewEventActivity.class);
        startActivity(i);
    }

    public void logout(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Would you like to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(AdminActivity.this, SplashScrActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // user doesn't want to logout
                    }
                })
                .show();
    }
}
