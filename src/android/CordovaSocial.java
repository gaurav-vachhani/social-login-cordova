package org.cordova.social;

import com.facebook.login.LoginManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaSocial extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("loginFb")) {
            String message = args.getString(0);
            this.loginFb(message, callbackContext);
            return true;
        }
        return false;
    }

    private void loginFb(String message, CallbackContext callbackContext) {
        LoginManager.getInstance().logInWithReadPermissions(cordova.getActivity(), Arrays.asList("email", "public_profile"));
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
