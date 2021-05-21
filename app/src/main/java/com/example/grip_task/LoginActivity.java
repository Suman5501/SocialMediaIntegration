 package com.example.grip_task;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private Button fbLogin_btn, googleLogin_btn;
    private ProgressDialog progressDialog;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 7;
    private static final int LI_SDK_AUTH_REQUEST_CODE = 3672, FB_REQUEST_CODE = 64206;

    private long accessTokenExpiry;
    private String accessToken;

    private String api; //0 for google, 1 for facebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        googleLogin_btn = findViewById(R.id.btn_google);
        fbLogin_btn = findViewById(R.id.btn_fb);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Loading..Please wait");

        loginManager = LoginManager.getInstance();
        callbackManager = CallbackManager.Factory.create();

        fbLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api = "1";
                progressDialog.show();
                loginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("email", "public_profile","user_birthday","user_gender"));

                checkLoginStatus();

            }
        });

        googleLogin_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                api = "0";
                progressDialog.show();
                switch (v.getId()) {

                    case R.id.btn_google:
                        googleSignIn();
                        break;

                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if(progressDialog.isShowing()) {

                    progressDialog.dismiss();

                }
            }

            @Override
            public void onCancel() {
                if(progressDialog.isShowing()) {

                    progressDialog.dismiss();

                }
            }

            @Override
            public void onError(FacebookException error) {
                if(progressDialog.isShowing()) {

                    progressDialog.dismiss();

                }
                Log.i("Error", error.toString());
            }
        });
    }

    private void googleSignIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {

            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            String url = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "No Picture found";
            api = "0";

            Log.i("GLOGIN", url);
            Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
            intent.putExtra("name", account.getDisplayName());
            intent.putExtra("email", account.getEmail());
            intent.putExtra("id",account.getId());
            intent.putExtra("picstring", url);
            intent.putExtra("social", api);

            if(progressDialog.isShowing()) {

                progressDialog.dismiss();

            }

            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(intent);

        } catch (ApiException e) {

            if(progressDialog.isShowing()) {

                progressDialog.dismiss();

            }

            Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == FB_REQUEST_CODE)
            callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }

    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

            if(currentAccessToken==null) {
                Toast.makeText(LoginActivity.this,"User Logged Out",Toast.LENGTH_SHORT).show();

            }
            else
                fbLogin(currentAccessToken);

        }
    };

    public void fbLogin(AccessToken accessToken) {

        progressDialog.show();
        Bundle parameters = new Bundle();
        parameters.putBoolean("redirect", false);
        parameters.putString(
                "fields",
                "first_name, last_name, email, id, picture.type(normal), birthday, gender");

        new GraphRequest(
                accessToken,
                "me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {

                    @Override
                    public void onCompleted(GraphResponse response) {

                        if(response != null) {

                            Log.v("Main Activity",response.toString());

                            try {

                                JSONObject object = response.getJSONObject();
                                String name = object.getString("first_name") + " " + object.getString("last_name");
                                String email = object.getString("email");
                                String id = object.getString("id");
                                String birthday = object.getString("birthday");
//                                String profileLink = Profile.getCurrentProfile().getLinkUri().toString();

//                                String profileLink = object.getString("link");
                                String gender = object.getString("gender");

                                String link = object.getJSONObject("picture").getJSONObject("data").getString("url");
                                api = "1";
                                Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
                                intent.putExtra("name", name);
                                intent.putExtra("email", email);
                                intent.putExtra("id", id);
                                intent.putExtra("picstring", link);
                                intent.putExtra("social", api);
                                intent.putExtra("birthday", birthday);
                                intent.putExtra("gender", gender);

//                                intent.putExtra("profilelink", profileLink);


                                Log.v("TAGObjectInfo","id: "+id);
                                Log.v("TAGObjectInfo","name: "+name);
                                Log.v("TAGObjectInfo","email: "+email);
                                Log.v("TAGObjectInfo","birthday: "+birthday);
                                Log.v("TAGObjectInfo","gender: "+gender);



                                if(progressDialog.isShowing()) {

                                    progressDialog.dismiss();

                                }

                                Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                startActivity(intent);

                                // do action after Facebook login success
                                // or call your API
                            }
                            catch (Exception e) {

                                Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                Log.i("Error", "SOME ERROR" + e.toString());
                                e.printStackTrace();

                            }


                        }

                    }
                }

        ).executeAsync();

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

    private void checkLoginStatus() {
        if (AccessToken.getCurrentAccessToken() != null) {
            fbLogin(AccessToken.getCurrentAccessToken());
        }
    }

}