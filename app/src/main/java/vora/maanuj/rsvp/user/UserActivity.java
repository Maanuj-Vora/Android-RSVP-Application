package vora.maanuj.rsvp.user;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import vora.maanuj.rsvp.R;

public class UserActivity extends AppCompatActivity {

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

    private ScrollView upcomingEvents;

    int sizee;

    TextView noEvents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        progressDialog = new ProgressDialog(this);

        noEvents = (TextView) findViewById(R.id.textView);

        upcomingEvents = (ScrollView) findViewById(R.id.upcomingEvents);
        upcomingEvents.setVisibility(View.INVISIBLE);

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

                        sizee = docs.size();

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

                        if(name.size() == 0){
                            noEvents.setVisibility(View.VISIBLE);
                            System.out.println("Visible");
                        }
                        else{
                            noEvents.setVisibility(View.INVISIBLE);
                        }
                    }
                });

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            Intent i = new Intent(UserActivity.this, InfoActivity.class);
            i.putExtra("doc", name.get(id).toString());
            startActivity(i);
        }
    };
}
