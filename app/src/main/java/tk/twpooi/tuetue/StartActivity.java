package tk.twpooi.tuetue;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import tk.twpooi.tuetue.util.FacebookLogin;
import tk.twpooi.tuetue.util.FacebookLoginSupport;
import tk.twpooi.tuetue.util.NaverLogin;
import tk.twpooi.tuetue.util.NaverLoginSupport;
import tk.twpooi.tuetue.util.ParsePHP;

public class StartActivity extends AppCompatActivity implements FacebookLoginSupport, NaverLoginSupport{


    private SharedPreferences setting;
    private SharedPreferences.Editor editor;

    // Facebook
    private FacebookLogin facebookLogin;
    public static String USER_ID = "";
    public static String[] CATEGORY_LIST;
    private ImageView fbLogin;

    // Naver
    private NaverLogin naverLogin;
    private OAuthLoginButton mOAuthLoginButton;

    // UI
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facebookLogin = new FacebookLogin(this, this);
        setContentView(R.layout.activity_start);

        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();

        //printKeyHash();

        init();

        setCategoryList();

    }

    private void init(){

        progressDialog = new ProgressDialog(this);

        fbLogin = (ImageView)findViewById(R.id.fb_login);

        fbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogin.login();
            }
        });
        Button fbLoout = (Button)findViewById(R.id.fb_logout);
        fbLoout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogin.logout();
            }
        });

        mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.naver_login);
        naverLogin = new NaverLogin(this, mOAuthLoginButton, this);
        mOAuthLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                naverLogin.login();
            }
        });

    }

    public void setCategoryList(){

        progressDialog.show();

        new ParsePHP("http://ldayou.asuscomm.com:36080/android/tuetue/getCategory.php", new HashMap<String, String>()) {
            @Override
            protected void afterThreadFinish(String data) {
                try {
                    // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                    JSONObject jObject = new JSONObject(data);
                    // results라는 key는 JSON배열로 되어있다.
                    JSONArray results = jObject.getJSONArray("result");
                    String countTemp = (String)jObject.get("num_result");
                    int count = Integer.parseInt(countTemp);

                    CATEGORY_LIST = new String[count];

                    for ( int i = 0; i < count; ++i ) {
                        JSONObject temp = results.getJSONObject(i);
                        CATEGORY_LIST[i] = (String)temp.get("category");
                    }


                    checkAlreadyLogin();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void checkAlreadyLogin(){

        String login = setting.getString("login", null);

        if(null != login){

            if("facebook".equals(login)){

                if(facebookLogin.isAlreadyLogin()){

                    USER_ID = facebookLogin.getID();
                    redirectMainActivity();

                    return;

                }

            }else if("naver".equals(login)){

                HashMap<String, String> data = naverLogin.getUserInformation();
                if(!data.isEmpty()){

                    USER_ID = data.get("id");
                    redirectMainActivity();

                    return;

                }

            }

        }

        progressDialog.dismiss();

    }

    public void redirectMainActivity() {
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
//        finish();
    }

    public void redirectWtInfoActivity() {
        Intent intent = new Intent(this, WtInfoActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookLogin.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void afterFBLoginSuccess(Profile profile, HashMap<String, String> data) {
//        String name = profile.getName();
//        String img = profile.getProfilePictureUri(500, 500).toString();
//        String email = data.get("email");
//        showSnackbar(name + " " + email + " " + img);
        showSnackbar("Facebook 로그인 성공");
        editor.putString("login", "facebook");
        editor.commit();
        redirectWtInfoActivity();
    }

    @Override
    public void afterFBLoginCancel() {
        showSnackbar("Facebook Login Cancel");
    }

    @Override
    public void afterFBLoginError(FacebookException error) {
        showSnackbar("Facebook Login Error : " + error.getCause().toString());
    }

    @Override
    public void afterFBLogout() {
        showSnackbar("Facebook Logout");
    }

    @Override
    public void afterNaverLoginSuccess(HashMap<String, String> data) {
        showSnackbar("네이버 로그인 성공");
        editor.putString("login", "naver");
        editor.commit();
        redirectWtInfoActivity();
    }

    @Override
    public void afterNaverLoginFail(String errorCode, String errorDesc) {
        showSnackbar("Naver Login Error : " + errorCode + ", " + errorDesc);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }


    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    private void printKeyHash(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "tk.twpooi.tuetue",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                showSnackbar(Base64.encodeToString(md.digest(), Base64.DEFAULT));
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

}
