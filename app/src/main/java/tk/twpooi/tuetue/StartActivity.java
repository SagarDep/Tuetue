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
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import tk.twpooi.tuetue.sub.IntroduceActivity;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.FacebookLogin;
import tk.twpooi.tuetue.util.FacebookLoginSupport;
import tk.twpooi.tuetue.util.KakaoLogin;
import tk.twpooi.tuetue.util.KakaoLoginButton;
import tk.twpooi.tuetue.util.KakaoLoginSupport;
import tk.twpooi.tuetue.util.NaverLogin;
import tk.twpooi.tuetue.util.NaverLoginSupport;
import tk.twpooi.tuetue.util.ParsePHP;

public class StartActivity extends AppCompatActivity implements FacebookLoginSupport, NaverLoginSupport, KakaoLoginSupport {

    public static final int FIRST_LOADING = 5;
    public static final String FACEBOOK_LOGIN = "facebook";
    public static final String NAVER_LOGIN = "naver";
    public static final String KAKAO_LOGIN = "kakao";

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_SHOW_LOGIN = 500;
    private final int MSG_MESSAGE_SUCCESS = 501;
    private final int MSG_MESSAGE_FAIL_FB = 502;
    private final int MSG_MESSAGE_FAIL_NAVER = 503;
    private final int MSG_MESSAGE_FAIL_KAKAO = 504;
    private final int MSG_MESSAGE_FACEBOOK_EMPTY = 505;
    private final int MSG_MESSAGE_NAVER_EMPTY = 506;
    private final int MSG_MESSAGE_KAKAO_EMPTY = 507;

    private SharedPreferences setting;
    private SharedPreferences.Editor editor;

    // Facebook
    private FacebookLogin facebookLogin;
    private ImageView fbLogin;

    // Naver
    private NaverLogin naverLogin;
    private OAuthLoginButton mOAuthLoginButton;

    // Kakao
    private KakaoLogin kakaoLogin;
    private KakaoLoginButton kakaoLoginBtn;

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

        printKeyHash();

        init();

        boolean isFirst = setting.getBoolean("isFirst", true);
        if (isFirst) {
            editor.putBoolean("isFirst", false);
            editor.commit();
            Intent intent = new Intent(getApplicationContext(), IntroduceActivity.class);
            intent.putExtra("code", FIRST_LOADING);
            startActivityForResult(intent, FIRST_LOADING);
        } else {
            setCategoryList();
        }

    }

    private void init(){

//        progressDialog = new ProgressDialog(this);

        kenBurnsView = (KenBurnsView)findViewById(R.id.image);
        kenBurnsView.setImageResource(R.drawable.loading);
//        Picasso.with(getApplicationContext())
//                .load(Information.LODING_IMAGE_URL)
//                .memoryPolicy(MemoryPolicy.NO_CACHE)
//                .into(kenBurnsView);
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

        kakaoLogin = new KakaoLogin(this);
        kakaoLoginBtn = (KakaoLoginButton) findViewById(R.id.kakao_login);
//        kakaoLoginBtn.inflate(getApplicationContext(), R.layout.custom_kakao_login, kakaoLoginBtn);

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

            if (FACEBOOK_LOGIN.equals(login)) {

                if(facebookLogin.isAlreadyLogin()){

                    USER_ID = facebookLogin.getID();
                    checkLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FAIL_FB);

                    return;

                }

            } else if (NAVER_LOGIN.equals(login)) {

                HashMap<String, String> data = naverLogin.getUserInformation();
                if(!data.isEmpty()){

                    USER_ID = data.get("id");
                    checkLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FAIL_NAVER);

                    return;

                }

            } else if (KAKAO_LOGIN.equals(login)) {

                if (kakaoLogin.isAlreadyLogin()) {
                    kakaoLogin.login();
                    return;
                }
//                if(kakaoLogin.isAlreadyLogin()){
//
//                    USER_ID = Long.toString(data.getId());
//                    checkFBLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FAIL_KAKAO);
//
//                    return;
//
//                }

            }

        }

        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_LOGIN));

    }

    private void checkLogin(final int success, final int fail) {
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
                case MSG_MESSAGE_FAIL_KAKAO:
                    kakaoLogin.setAlreadyLogin(false);
                    kakaoLogin.login();
                    break;
                case MSG_MESSAGE_FACEBOOK_EMPTY:
                    redirectWtInfoActivity();
                    break;
                case MSG_MESSAGE_NAVER_EMPTY:
                    redirectWtInfoActivity();
                    break;
                case MSG_MESSAGE_KAKAO_EMPTY:
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
        kakaoLogin.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case FIRST_LOADING:
                setCategoryList();
                break;
            default:
                break;
        }
    }

    // =================== Login Method ============================

    // ================== Facebook ==================
    @Override
    public void afterFBLoginSuccess(Profile profile, HashMap<String, String> data) {
//        showSnackbar("Facebook 로그인 성공");
        editor.putString("login", FACEBOOK_LOGIN);
        editor.commit();
        USER_ID = profile.getId();

        wt_id = profile.getId();
        wt_img = profile.getProfilePictureUri(500, 500).toString();
        wt_email = data.get("email");
        wt_name = profile.getName();

        checkLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FACEBOOK_EMPTY);
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

    // ================== Naver ==================

    @Override
    public void afterNaverLoginSuccess(HashMap<String, String> data) {
//        showSnackbar("네이버 로그인 성공");
        editor.putString("login", NAVER_LOGIN);
        editor.commit();
        USER_ID = data.get("id");

        wt_id = data.get("id");
        wt_img = data.get("img");
        wt_email = data.get("email");
        wt_name = data.get("name");

        checkLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_NAVER_EMPTY);
    }

    @Override
    public void afterNaverLoginFail(String errorCode, String errorDesc) {
        showSnackbar("Naver Login Error : " + errorCode + ", " + errorDesc);
    }

    // ================== Kakao ==================

    @Override
    public void kakaoSessionOpenFailed(KakaoException exception) {
        showSnackbar("Session Open Failed");
    }

    @Override
    public void afterKakaoLoginSuccess(UserProfile userProfile) {
        editor.putString("login", KAKAO_LOGIN);
        editor.commit();
        USER_ID = Long.toString(userProfile.getId());

        wt_id = Long.toString(userProfile.getId());
        wt_img = userProfile.getThumbnailImagePath();
//        wt_img = profile.getProfilePictureUri(500, 500).toString();
        wt_email = null;
        wt_name = userProfile.getNickname();

        checkLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_KAKAO_EMPTY);
    }

    @Override
    public void afterKakaoLoginIsAlreadyLogin(UserProfile userProfile) {
        USER_ID = Long.toString(userProfile.getId());
        checkLogin(MSG_MESSAGE_SUCCESS, MSG_MESSAGE_FAIL_KAKAO);
    }

    @Override
    public void afterKakaoLoginFailed(ErrorResult errorResult) {
        showSnackbar(errorResult.getErrorMessage());
    }

    @Override
    public void afterKakaoLoginSessionClosed(ErrorResult errorResult) {
    }

    @Override
    public void afterKakaoLoginNotSignedUp() {
        showSnackbar("Login not signed up");
    }

    @Override
    public void afterKakaoLogout() {
        showSnackbar("Logout");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        AdditionalFunc.clearApplicationCache(getApplicationContext(), null);
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
//                showSnackbar(Base64.encodeToString(md.digest(), Base64.DEFAULT));
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

}
