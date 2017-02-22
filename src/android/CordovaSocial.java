package org.cordova.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaSocial extends CordovaPlugin implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static int FACEBOOK_LOGIN_REQUEST = 64206;
    private CallbackManager callbackManager;
    public CallbackContext callbackContext;
    private static int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        cordova.setActivityResultCallback(this);
        if (action.equals("loginFb")) {
            setUpFBCallback();
            LoginManager.getInstance().logInWithReadPermissions(cordova.getActivity(), Arrays.asList("email", "public_profile"));
            return true;
        } else if (action.equals("loginGoogle")) {
            buildGoogleApi();
            return true;
        }
        return false;
    }


    private void setUpFBCallback() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(
                callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object,
                                                            GraphResponse response) {
                                        callbackContext.success(object);
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields",
                                "id,first_name, last_name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        callbackContext.error("User cancelled login.");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        callbackContext.error("There is some error with facebook login.");
                    }
                });
    }

    private void buildGoogleApi() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(cordova.getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
    }


    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }



    private void handleGoogleSignIN(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                if (this.callbackContext != null) {
                    JSONObject googleUser = new JSONObject();
                    try {
                        googleUser.put("displayName", acct.getDisplayName());
                        googleUser.put("email", acct.getEmail());
                        googleUser.put("lname", acct.getFamilyName());
                        googleUser.put("fname", acct.getGivenName());
                        googleUser.put("id", acct.getId());
                        googleUser.put("idToken", acct.getIdToken());
                        googleUser.put("photo", acct.getPhotoUrl());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    this.callbackContext.success(googleUser);
                }
            } else {
                this.callbackContext.error("There is some error with google login.");
            }
        }
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == FACEBOOK_LOGIN_REQUEST) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignIN(result);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("SocialLogin", "Build Connected");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        this.cordova.startActivityForResult((CordovaPlugin) this, signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("SocialLogin", "connection suspended.");
        callbackContext.error("There is some error with google login.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("SocialLogin", "connection failed.");
        callbackContext.error("There is some error with google login.");
    }
}
