package tk.twpooi.tuetue.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by tw on 2017-01-11.
 */

public class FacebookLogin {

    private static final String TAG = "FacebookLogin";

    private Context context;
    private CallbackManager callbackManager;

    private String facebook_id,f_name, m_name, l_name, gender, profile_image, full_name, email_id;

    private FacebookLoginSupport fls;

    public FacebookLogin(final Context context){

        this.context = context;
        FacebookSdk.sdkInitialize(context); // 꼭 setContentView 전에 호출
        callbackManager = CallbackManager.Factory.create();

        facebook_id=f_name= m_name= l_name= gender= profile_image= full_name= email_id="";

    }

    public FacebookLogin(Context context, FacebookLoginSupport fls){
        this(context);
        this.fls = fls;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private HashMap<String, String> getFacebookData(JSONObject object) {

        HashMap<String, String> data = new HashMap<>();

        try {

            data.put("id", object.getString("id"));
            if (object.has("first_name"))
                data.put("first_name", object.getString("first_name"));
            if (object.has("last_name"))
                data.put("last_name", object.getString("last_name"));
            if (object.has("email"))
                data.put("email", object.getString("email"));
            if (object.has("gender"))
                data.put("gender", object.getString("gender"));
            if (object.has("birthday"))
                data.put("birthday", object.getString("birthday"));
            if (object.has("location"))
                data.put("location", object.getString("location"));

            return data;
        }
        catch(JSONException e) {
            return null;
        }
    }

    public void login(){
//        LoginManager.getInstance().logInWithReadPermissions((Activity)context, Arrays.asList("public_profile", "user_friends", "email"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("LoginActivity", response.toString());
                        // Get facebook data from login
//                        Bundle bFacebookData = getFacebookData(object);
                        if(fls != null){
                            fls.afterFBLoginSuccess(Profile.getCurrentProfile(), getFacebookData(object));
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location"); // Parámetros que pedimos a facebook
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                if(fls != null){
                    fls.afterFBLoginCancel();
                }
            }

            @Override
            public void onError(FacebookException error) {
                if(fls != null){
                    fls.afterFBLoginError(error);
                }
            }
        });

        LoginManager.getInstance().logInWithReadPermissions((Activity)context, Arrays.asList("email", "public_profile", "user_friends"));

    }
    public void logout(){
        LoginManager.getInstance().logOut();
        if(fls != null){
            fls.afterFBLogout();
        }
    }

    public Profile getUserProfile(){
        return Profile.getCurrentProfile();
    }
    public String getID(){
        Profile profile = Profile.getCurrentProfile();
        if(profile != null){
            return profile.getId();
        }else{
            return null;
        }
    }
    public boolean isAlreadyLogin(){
        Profile profile = Profile.getCurrentProfile();

        if(profile != null){
            return true;
        }else{
            return false;
        }
    }

}
