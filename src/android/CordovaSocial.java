package org.cordova.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaSocial extends CordovaPlugin {
    private static int FACEBOOK_LOGIN_REQUEST = 64206;
    private CallbackManager callbackManager;
    public CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        cordova.setActivityResultCallback(this);
        if (action.equals("loginFb")) {
            setUpFBCallback();
            LoginManager.getInstance().logInWithReadPermissions(cordova.getActivity(), Arrays.asList("email", "public_profile"));
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

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == FACEBOOK_LOGIN_REQUEST) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}
