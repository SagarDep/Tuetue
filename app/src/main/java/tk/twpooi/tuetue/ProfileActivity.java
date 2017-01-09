package tk.twpooi.tuetue;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;

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

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ProfileActivity extends AppCompatActivity {

    private FrameLayout root;
    private TextView tv_nickname;
    private ImageView img_profile;
    private TextView tv_intro;
    private TextView tv_tutorCount;
    private TextView tv_tuteeCount;
    private TextView tv_rank;
    private TextView tv_interest;
    private ShineButton heartBtn;

    private HashMap<String, String> data;
    private String userId;
    private ArrayList<HashMap<String, Object>> tutorList;
    private int interestCount = 0;
    private int tuteeCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");


        initData();


        init();

    }

    private void initData(){

        HashMap<String, String> map = new HashMap<>();
        map.put("id", userId);

        GetUserInfo gui = new GetUserInfo(map);
        gui.start();
        try{
            gui.join();
        }catch (Exception e){

        }
        data = gui.getReturnList().get(0);
        gui.interrupt();

        map = new HashMap<>();
        map.put("userid", userId);

        GetUserTutorInfo gti = new GetUserTutorInfo(map);
        gti.start();
        try{
            gti.join();
        }catch (Exception e){

        }
        tutorList = gti.getResult();
        System.out.println(tutorList);

        for(HashMap<String, Object> h : tutorList){
            int inter = (int)h.get("interest");
            interestCount += inter;
        }

        map = new HashMap<>();
        map.put("userId", userId);
        GetUserTuteeInfo guti = new GetUserTuteeInfo(map);
        try{
            guti.join();
        }catch (Exception e){

        }
        tuteeCount = guti.getResult();
        System.out.println("tuteeCount : " + tuteeCount);

    }

    private void init(){

        root = (FrameLayout)findViewById(R.id.activity_profile);
        tv_nickname = (TextView)findViewById(R.id.nickname);
        img_profile = (ImageView)findViewById(R.id.rl_profile_img);
        tv_intro = (TextView)findViewById(R.id.intro);
        tv_tutorCount = (TextView)findViewById(R.id.tutor_count);
        tv_tuteeCount = (TextView)findViewById(R.id.tutee_count);
        tv_rank = (TextView)findViewById(R.id.rank);
        tv_interest = (TextView)findViewById(R.id.interest);
        heartBtn = (ShineButton)findViewById(R.id.heartBtn);

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tv_nickname.setText(data.get("nickname"));
        Picasso.with(getApplicationContext())
                .load(data.get("img"))
                .transform(new CropCircleTransformation())
                .into(img_profile);
        tv_intro.setText(data.get("intro"));
        heartBtn.setChecked(true);
        heartBtn.setEnabled(false);
        tv_tutorCount.setText(tutorList.size() + "개");
        tv_interest.setText(interestCount + "개");

        if(interestCount <20){
            tv_rank.setText("1");
            tv_rank.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.rank01));
        }else if(interestCount <40){
            tv_rank.setText("2");
            tv_rank.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.rank02));
        }else if(interestCount <60){
            tv_rank.setText("3");
            tv_rank.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.rank03));
        }else if(interestCount <80){
            tv_rank.setText("4");
            tv_rank.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.rank04));
        }else if(interestCount <100){
            tv_rank.setText("5");
            tv_rank.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.rank05));
        }else{
            tv_rank.setText("6");
            tv_rank.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.rank06));
        }

    }

    private class GetUserInfo extends Thread{

        private boolean result;
        private HashMap<String, String> map;
        private ArrayList<HashMap<String, String>> returnList;

        public GetUserInfo(HashMap<String, String> map){
            this.map = map;
            result = false;
        }

        public void run(){

            String addr = Information.MAIN_SERVER_ADDRESS + "getUser.php";
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

            String str = response.toString();
            returnList = new ArrayList<>();
            try {
                // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                JSONObject jObject = new JSONObject(str);
                // results라는 key는 JSON배열로 되어있다.
                JSONArray results = jObject.getJSONArray("result");
                String countTemp = (String)jObject.get("num_result");
                int count = Integer.parseInt(countTemp);

//                HashMap<String, String> hashTemp = new HashMap<>();
                for ( int i = 0; i < count; ++i ) {
                    JSONObject temp = results.getJSONObject(i);

                    HashMap<String, String> hashTemp = new HashMap<>();
                    hashTemp.put("userId", (String)temp.get("id"));
                    hashTemp.put("intro", (String)temp.get("intro"));
                    hashTemp.put("img", (String)temp.get("img"));
                    hashTemp.put("nickname", (String)temp.get("nickname"));

                    returnList.add(hashTemp);

                }

            } catch (JSONException e) {
                e.printStackTrace();
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

        public ArrayList<HashMap<String, String>> getReturnList(){
            return returnList;
        }

    }

    private class GetUserTutorInfo extends Thread{

        private ArrayList<HashMap<String, Object>> result = new ArrayList<>();
        private HashMap<String, String> map;

        public GetUserTutorInfo(HashMap<String, String> map){
            this.map = map;
        }

        public void run(){

            String addr = Information.MAIN_SERVER_ADDRESS + "getUserTutorList.php";
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

            String str = response.toString();
            try {
                // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                JSONObject jObject = new JSONObject(str);
                // results라는 key는 JSON배열로 되어있다.
                JSONArray results = jObject.getJSONArray("result");
                String countTemp = (String)jObject.get("num_result");
                int count = Integer.parseInt(countTemp);

                for ( int i = 0; i < count; ++i ) {
                    JSONObject temp = results.getJSONObject(i);

                    HashMap<String, Object> hashTemp = new HashMap<>();
                    hashTemp.put("id", (String)temp.get("id"));
                    hashTemp.put("userid", (String)temp.get("userid"));
                    hashTemp.put("img", (String)temp.get("img"));
                    hashTemp.put("nickname", (String)temp.get("nickname"));
                    hashTemp.put("category", (String)temp.get("category"));
                    hashTemp.put("cost", Integer.parseInt((String)temp.get("cost")));
                    hashTemp.put("count", Integer.parseInt((String)temp.get("count")));
                    hashTemp.put("limit", Integer.parseInt((String)temp.get("limit")));
                    hashTemp.put("interest", Integer.parseInt((String)temp.get("interest")));
                    hashTemp.put("time", (String)temp.get("time"));
                    hashTemp.put("contents", (String)temp.get("contents"));
                    hashTemp.put("isFinish", (String)temp.get("isFinish"));

                    String participant = (String)temp.get("participant");

                    ArrayList<String> par = new ArrayList<>();

                    if(!participant.equals("")){
                        String[] p = participant.split(",");

                        for(String s : p){
                            par.add(s);
                        }

                    }
                    hashTemp.put("participant", par);

                    result.add(hashTemp);

                }

            } catch (JSONException e) {
                e.printStackTrace();
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


        public ArrayList<HashMap<String, Object>> getResult(){
            return result;
        }

    }

    private class GetUserTuteeInfo extends Thread{

        private int result;
        private HashMap<String, String> map;

        public GetUserTuteeInfo(HashMap<String, String> map){
            this.map = map;
        }

        public void run(){

            String addr = Information.MAIN_SERVER_ADDRESS + "searchTutee.php";
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

                System.out.println("respone : " + response);
                conn.disconnect();
            } catch (MalformedURLException me) {
                me.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String str = response.toString();

            try{
                result = Integer.parseInt(str);
            }catch (Exception e){

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


        public int getResult(){
            return result;
        }

    }


    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

}
