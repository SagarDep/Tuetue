package tk.twpooi.tuetue;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class WtInfoActivity extends AppCompatActivity {

    // Facebook
    private CallbackManager callbackManager;
    private String facebook_id = "";
    private String full_name = "";
    private String profileImg;

    // UI
    private RelativeLayout root;
    private ImageView profileImageView;
    private TextView profileName;
    private MaterialEditText nickName;
    private EditText email;
    private MaterialEditText intro;

    private TextView continueBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_wtinfo);

        init();

    }

    private void init(){

        Profile profile = Profile.getCurrentProfile();

        facebook_id = profile.getId();
        full_name = profile.getName();

        root = (RelativeLayout)findViewById(R.id.activity_wtinfo);
        profileImg = profile.getProfilePictureUri(250, 250).toString();
        profileImageView = (ImageView)findViewById(R.id.profileImg);
        Picasso.with(getApplicationContext())
                .load(profileImg)
                .transform(new CropCircleTransformation())
                .into(profileImageView);

        profileName = (TextView)findViewById(R.id.profileName);
        profileName.setText(profile.getName());

        nickName = (MaterialEditText)findViewById(R.id.nickname);
        nickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkEditText();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
//        email = (EditText)findViewById(R.id.email);
//        email.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                checkEditText();
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
        intro = (MaterialEditText)findViewById(R.id.intro);
        intro.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkEditText();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        continueBtn = (TextView)findViewById(R.id.nextBtn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackbar(nickName.getText() + ", " + intro.getText());
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", facebook_id);
                map.put("img", profileImg);
                map.put("name", full_name);
                map.put("nickname", nickName.getText().toString());
                map.put("intro", intro.getText().toString());
                System.out.println(map);


                SaveUserInformation sui = new SaveUserInformation(map);
                sui.start();
                try{
                    sui.join();
                }catch (Exception e){

                }

                sui.interrupt();
                redirectMainActivity();


            }
        });
        continueBtn.setEnabled(false);

    }



    private void checkEditText(){

        boolean isNickName = false;
        boolean isEmail = true;
        boolean isIntro = false;

        if(nickName.getText().toString().length() >= 1){
            isNickName = true;
        }

//        if(email.getText().toString().length() >= 1){
//            isEmail = true;
//        }

        if(intro.getText().toString().length() >= 1){
            isIntro = true;
        }

        continueBtn.setEnabled(isNickName && isEmail && isIntro);


    }

    private class SaveUserInformation extends Thread{

        private boolean result;
        private HashMap<String, String> map;

        public SaveUserInformation(HashMap<String, String> map){
            this.map = map;
            result = false;
        }

        public void run(){

            String addr = Information.MAIN_SERVER_ADDRESS + "saveMember.php";
            String response = new String();

            try {
                URL url = new URL(addr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 해당 URL에 연결

                conn.setConnectTimeout(10000); // 타임아웃: 10초
                conn.setUseCaches(false); // 캐시 사용 안 함
                conn.setRequestMethod("POST"); // POST로 연결
                conn.setDoInput(true);
                conn.setDoOutput(true);

                if (map != null) { // 웹 서버로 보낼 매개변수가 있는 경우우
                    OutputStream os = conn.getOutputStream(); // 서버로 보내기 위한 출력 스트림
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8")); // UTF-8로 전송
                    bw.write(getPostString(map)); // 매개변수 전송
                    bw.flush();
                    bw.close();
                    os.close();
                }

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 연결에 성공한 경우
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())); // 서버의 응답을 읽기 위한 입력 스트림

                    while ((line = br.readLine()) != null) // 서버의 응답을 읽어옴
                        response += line;
                }


                conn.disconnect();
            } catch (MalformedURLException me) {
                me.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(response.equals("1")){
                result = true;
            }else{
                result = false;
            }

        }

        private String getPostString(HashMap<String, String> map) {
            StringBuilder result = new StringBuilder();
            boolean first = true; // 첫 번째 매개변수 여부

            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (first)
                    first = false;
                else // 첫 번째 매개변수가 아닌 경우엔 앞에 &를 붙임
                    result.append("&");

                try { // UTF-8로 주소에 키와 값을 붙임
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException ue) {
                    ue.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return result.toString();
        }

        public boolean getResult(){
            return result;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    public void redirectMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

}
