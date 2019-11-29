package com.example.fitapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

public class googlesign extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener  {


    private SignInButton signin;
    GoogleApiClient googleApiClient;
    private static final int reqcd=9001;
    String personPhoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_googlesign);


            signin=findViewById(R.id.signbtn);

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            // Build a GoogleSignInClient with the options specified by gso.
            googleApiClient=new GoogleApiClient.Builder(this)
                    .enableAutoManage(this,this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                    .build();
            signin.setOnClickListener(this);

        }
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.signbtn:
                    signIn();
                    break;
          /*  case R.id.logout:
                signOut();
                break;
            // ...*/
            }
        }

        private void signIn() {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(signInIntent, reqcd);
        }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==reqcd) {

            GoogleSignInResult result=Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);

            // The Task returned from this call is always completed, no need to attach
            // a listener.
            // Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            // handleSignInResult(task);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            if(null!= acct.getPhotoUrl()) {
                personPhoto  = acct.getPhotoUrl().toString();

            }

            updateUI(true);

            Intent in =new Intent(googlesign.this,MainActivity.class);
            in.putExtra("uname",personName);
            in.putExtra("email",personEmail);
            in.putExtra("picurl",personPhoto);
            startActivity(in);

        }
        else
        {
            updateUI(false);
        }
    }

    private void updateUI(boolean islogin) {
        if (islogin) {
            //  prosec.setVisibility(View.VISIBLE);
            signin.setVisibility(View.GONE);

        } else {
            //prosec.setVisibility(View.GONE);
            signin.setVisibility(View.VISIBLE);
        }
    }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

}
