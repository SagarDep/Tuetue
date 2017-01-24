package tk.twpooi.tuetue;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.FacebookException;
import com.facebook.Profile;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.FacebookLogin;
import tk.twpooi.tuetue.util.FacebookLoginSupport;
import tk.twpooi.tuetue.util.NaverLogin;
import tk.twpooi.tuetue.util.NaverLoginSupport;
import tk.twpooi.tuetue.util.ParsePHP;

public class StartActivity extends AppCompatActivity implements FacebookLoginSupport, NaverLoginSupport{

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_SHOW_LOGIN = 500;
    private final int MSG_MESSAGE_SUCCESS = 501;
    private final int MSG_MESSAGE_FAIL_FB = 502;
    private final int MSG_MESSAGE_FAIL_NAVER = 503;
    private final int MSG_MESSAGE_FACEBOOK_EMPTY = 504;
    private final int MSG_MESSAGE_NAVER_EMPTY = 505;

    private SharedPreferences setting;
    private SharedPreferences.Editor editor;

    // Facebook
    private FacebookLogin facebookLogin;
    private ImageView fbLogin;

    // Naver
    private NaverLogin naverLogin;
    private OAuthLoginButton mOAuthLoginButton;

    // UI
    private KenBurnsView kenBurnsView;
    private RelativeLayout rl_background;
//    private LinearLayout li_login;
//    private ProgressDialog progressDialog;

    // Data
    public static String USER_ID = "";
    public static String[] CATEGORY_LIST;
    public static int LIST_SIZE = 10;
    public static HashMap<String, Object> USER_DATA = new HashMap<>();

    // ToWtInfo
    private String wt_id;
    private String wt_email;
    private String wt_img;
    private String wt_name;

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

//        progressDialog = new ProgressDialog(this);

        kenBurnsView = (KenBurnsView)findViewById(R.id.image);
        Picasso.with(getApplicationContext())
                .load(Information.LODING_IMAGE_URL)
                .into(kenBurnsView);
        rl_background = (RelativeLayout) findViewById(R.id.rl_background);
        rl_background.setVisibility(View.INVISIBLE);
//        li_login = (LinearLayout)findViewById(R.id.li_login);
//        li_login.setVisibility(View.GONE);

        fbLogin = (ImageView)findViewById(R.id.fb_login);

        fbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLogin.login();
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

//        progressDialog.show();

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getCategoryList");

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {
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
                    checkFBLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FAIL_FB);

                    return;

                }

            }else if("naver".equals(login)){

                HashMap<String, String> data = naverLogin.getUserInformation();
                if(!data.isEmpty()){

                    USER_ID = data.get("id");
                    checkFBLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FAIL_NAVER);

                    return;

                }

            }

        }

        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOGIN));

    }

    private void checkFBLogin(final int success, final int fail){
        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getUserInfo");
        map.put("id", USER_ID);
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                USER_DATA = AdditionalFunc.getUserInfo(data);
                if(USER_DATA.isEmpty()){
                    handler.sendMessage(handler.obtainMessage(fail));
                }else{
                    handler.sendMessage(handler.obtainMessage(success));
                }
            }
        }.start();
    }
    private void checkNaverLogin(final int success, final int fail){
        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getUserInfo");
        map.put("id", USER_ID);
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                USER_DATA = AdditionalFunc.getUserInfo(data);
                if(USER_DATA.isEmpty()){
                    handler.sendMessage(handler.obtainMessage(fail));
                }else{
                    handler.sendMessage(handler.obtainMessage(success));
                }
            }
        }.start();
    }

    public void redirectMainActivity() {
//        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOGIN));
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void redirectWtInfoActivity() {
        Intent intent = new Intent(this, WtInfoActivity.class);
        intent.putExtra("id", wt_id);
        intent.putExtra("img", wt_img);
        intent.putExtra("email", wt_email);
        intent.putExtra("name", wt_name);
//        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOGIN));
        startActivity(intent);
        finish();
    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_SHOW_LOGIN:
//                    li_login.setVisibility(View.VISIBLE);
                    rl_background.setVisibility(View.VISIBLE);
                    break;
                case MSG_MESSAGE_SUCCESS:
                    redirectMainActivity();
                    break;
                case MSG_MESSAGE_FAIL_FB:
                    facebookLogin.login();
                    break;
                case MSG_MESSAGE_FAIL_NAVER:
                    naverLogin.login();
                    break;
                case MSG_MESSAGE_FACEBOOK_EMPTY:
                    redirectWtInfoActivity();
                    break;
                case MSG_MESSAGE_NAVER_EMPTY:
                    redirectWtInfoActivity();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookLogin.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void afterFBLoginSuccess(Profile profile, HashMap<String, String> data) {
//        showSnackbar("Facebook 로그인 성공");
        editor.putString("login", "facebook");
        editor.commit();
        USER_ID = profile.getId();

        wt_id = profile.getId();
        wt_img = profile.getProfilePictureUri(500, 500).toString();
        wt_email = data.get("email");
        wt_name = profile.getName();

        checkFBLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FACEBOOK_EMPTY);
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
//        showSnackbar("네이버 로그인 성공");
        editor.putString("login", "naver");
        editor.commit();
        USER_ID = data.get("id");

        wt_id = data.get("id");
        wt_img = data.get("img");
        wt_email = data.get("email");
        wt_name = data.get("name");

        checkNaverLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_NAVER_EMPTY);
    }

    @Override
    public void afterNaverLoginFail(String errorCode, String errorDesc) {
        showSnackbar("Naver Login Error : " + errorCode + ", " + errorDesc);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
//        if(progressDialog != null){
//            progressDialog.dismiss();
//        }
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
