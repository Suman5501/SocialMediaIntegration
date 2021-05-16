package com.example.grip_task;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {


    private String name,  userId, picURL, profileLink, userBirthday, gender, email;
    private ImageView profilePic;
    private Button backBtn;
    private TextView tvFullName,  tvGender, tvDOB, tvEmail, tvLink;
    private String api;
    private ConstraintLayout constraintLayout;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        profilePic = findViewById(R.id.profile_pic);
        backBtn = findViewById(R.id.btnLogout);
        tvFullName = findViewById(R.id.fullname);

        tvGender = findViewById(R.id.gender);
        tvDOB = findViewById(R.id.dob);
        tvEmail = findViewById(R.id.email);
        tvLink = findViewById(R.id.link);


        constraintLayout = (ConstraintLayout) findViewById(R.id.mainLayout);
        linearLayout = findViewById(R.id.layouttemp);
        final TextView child = constraintLayout.findViewById(R.id.link);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        email = intent.getStringExtra("email");
        userId = intent.getStringExtra("id");
        picURL = intent.getStringExtra("picstring");
        api = intent.getStringExtra("social");

        userBirthday = intent.getStringExtra("birthday");
        profileLink = intent.getStringExtra("profilelink");
        gender = intent.getStringExtra("gender");


        if(api.equals("0")) {
            linearLayout.removeView(tvGender);
            linearLayout.removeView(tvDOB);
            linearLayout.removeView(tvLink);


        }


        if(picURL.equals("No Picture found")) {

            profilePic.setImageResource(R.drawable.ic_undraw_profile_pic_ic5t);

        } else {

            Picasso.with(ProfileActivity.this).load(picURL).into(profilePic);

        }

        Log.i("Data", email + profilePic + name);
        tvFullName.setText(name);

        tvEmail.setText(email);
        tvGender.setText(gender);
        tvDOB.setText(userBirthday);
        tvLink.setText(profileLink);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                disconnectFromFacebook();
                Toast.makeText(ProfileActivity.this,"User Logged out" , Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));

            }
        });

    }

    public void disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null,
                HttpMethod.DELETE,
                new GraphRequest
                        .Callback() {
                    @Override
                    public void onCompleted(GraphResponse graphResponse) {
                        LoginManager.getInstance().logOut();
                    }
                })
                .executeAsync();
    }
}