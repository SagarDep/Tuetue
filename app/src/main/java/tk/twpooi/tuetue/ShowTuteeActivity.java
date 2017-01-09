package tk.twpooi.tuetue;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar;
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
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;


public class ShowTuteeActivity extends AppCompatActivity {


    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private TutorCustomAdapter adapter;

    private HashMap<String, Object> data;

    private RelativeLayout rl_progress;
    private RelativeLayout rl_tutor;
    private LinearLayout li_profile;
    private TextView tv_nickname;
    private ImageView profileImg;
    private RelativeLayout rl_join;
    private TextView jo_cost;
    private TextView jo_join;

    public static String id;
    private String userId;
    public static String tutorId;
    private int count;
    private int cost;
    private ArrayList<String> participant;

    public static boolean isUpdate;

    private TextRoundCornerProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tutor);

        id = getIntent().getStringExtra("id");
        HashMap<String, String> map = new HashMap<>();
        map.put("id", id);

        GetTuteeInfoById guibi = new GetTuteeInfoById(map);
        guibi.start();
        try{
            guibi.join();
        }catch (Exception e){

        }
        data = guibi.getResult().get(0);

        init();

    }

    private void init(){

        userId = (String)data.get("userid");
        String nickname = (String)data.get("nickname");
        String img = (String)data.get("img");
//        cost = (int)data.get("cost");
//        count = (int)data.get("count");
        participant = (ArrayList<String>)data.get("participant");
        String isFinish = (String)data.get("isFinish");
        tutorId = (String)data.get("tutorId");

        rl_tutor = (RelativeLayout)findViewById(R.id.rl_tutor);
        rl_progress = (RelativeLayout)findViewById(R.id.rl_progress);
        rl_progress.setVisibility(View.GONE);
        li_profile = (LinearLayout)findViewById(R.id.rl_profile);
        tv_nickname = (TextView)findViewById(R.id.nickname);
        profileImg = (ImageView)findViewById(R.id.rl_profile_img);
        rl_join = (RelativeLayout) findViewById(R.id.rl_join);
        jo_cost = (TextView)findViewById(R.id.cost);
        jo_join = (TextView)findViewById(R.id.join);

        if("0".equals(isFinish)){
            rl_tutor.setVisibility(View.GONE);
        }else{
            rl_join.setVisibility(View.GONE);
            rl_tutor.setVisibility(View.VISIBLE);
        }
        rl_tutor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", tutorId);
                startActivity(intent);
            }
        });

        tv_nickname.setText(nickname);
        Picasso.with(getApplicationContext())
                .load(img)
                .transform(new CropCircleTransformation())
                .into(profileImg);



        if(participant.contains(StartActivity.USER_FACEBOOK_ID)){
            setButton(false);
        }else{
            setButton(true);
        }

        li_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });


        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        //rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL_LIST));
        rv.setLayoutManager(mLinearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rv.getContext(),
                mLinearLayoutManager.getOrientation());
        rv.addItemDecoration(dividerItemDecoration);

//        setProgressBar();

//        float percent = ((float)participant.size()/count)*100;
//        progressBar.setProgress(percent);
//        progressBar.setProgressText(participant.size() + "/" + count);

        makeList();

    }

    private void setButton(boolean check){

        if(userId.equals(StartActivity.USER_FACEBOOK_ID)){
            jo_cost.setVisibility(View.GONE);
            jo_join.setText("선택하기");
            rl_join.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.facebook_blue));
            rl_join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ParticipantSelectListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("participant", participant);
                    intent.putExtra("id", id);
                    startActivity(intent);
                }
            });
        }else{

            if(check){ // 참가하기
                jo_cost.setVisibility(View.GONE);
//            jo_cost.setVisibility(View.VISIBLE);
//            jo_cost.setText("비용 : " + cost + "원");
//            jo_join.setText("참가하기 (" + participant.size() + "/" + count + ")");
                jo_join.setText("참가하기");
                rl_join.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_btn_bg_color));
                rl_join.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        participant.add(StartActivity.USER_FACEBOOK_ID);
                        String p = "";
                        for(int i=0; i<participant.size(); i++){
                            p += participant.get(i);
                            if(i+1 < participant.size()){
                                p += ",";
                            }
                        }

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("id", id);
                        map.put("participant", p);

                        UpdateParticipant up = new UpdateParticipant(map);
                        up.start();
                        try{
                            up.join();
                        }catch (Exception e){

                        }

//                    float percent = ((float)participant.size()/count)*100;
//                    progressBar.setProgress(percent);
//                    progressBar.setProgressText(participant.size() + "/" + count);

                        makeList();
                        setButton(false);



                    }
                });
            }else{
                jo_cost.setVisibility(View.GONE);
                jo_join.setText("철회하기");
                rl_join.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_background_color));
                rl_join.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        participant.remove(StartActivity.USER_FACEBOOK_ID);
                        String p = "";
                        for(int i=0; i<participant.size(); i++){
                            p += participant.get(i);
                            if(i+1 < participant.size()){
                                p += ",";
                            }
                        }

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("id", id);
                        map.put("participant", p);

                        UpdateParticipant up = new UpdateParticipant(map);
                        up.start();
                        try{
                            up.join();
                        }catch (Exception e){

                        }

//                    float percent = ((float)participant.size()/count)*100;
//                    progressBar.setProgress(percent);
//                    progressBar.setProgressText(participant.size() + "/" + count);

                        makeList();
                        setButton(true);

                    }
                });
            }

        }

    }

    private void setProgressBar(){

        progressBar = (TextRoundCornerProgressBar)findViewById(R.id.progress);
        progressBar.setMax(100);
        progressBar.setProgress(0);


    }

    private class GetTuteeInfoById extends Thread{

        private ArrayList<HashMap<String, Object>> result = new ArrayList<>();
        private HashMap<String, String> map;

        public GetTuteeInfoById(HashMap<String, String> map){
            this.map = map;
        }

        public void run(){

            String addr = Information.MAIN_SERVER_ADDRESS + "getTuteeListById.php";
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
                    hashTemp.put("time", (String)temp.get("time"));
                    hashTemp.put("contents", (String)temp.get("contents"));
                    hashTemp.put("isFinish", (String)temp.get("isFinish"));
                    hashTemp.put("tutorId", (String)temp.get("tutorId"));

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

    class UpdateParticipant extends Thread {

        private boolean result;
        private HashMap<String, String> map;

        public UpdateParticipant(HashMap<String, String> map){
            this.map = map;
            result = false;
        }

        public void run(){

            String addr = Information.MAIN_SERVER_ADDRESS + "updateTuteeParticipant.php";
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

    private ArrayList<HashMap<String, String>> makeData(){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        String contents = (String)data.get("contents");
        String time = (String)data.get("time");
        String category = (String)data.get("category");
        int cost = (int)data.get("cost");
//        int interest = (int)data.get("interest");
//        int count = (int)data.get("count");
        String parti = Integer.toString(participant.size());

        HashMap<String, String> temp = new HashMap<>();
        temp.put("title", "소개");
        temp.put("content", contents);
        list.add(temp);

        temp = new HashMap<>();
        temp.put("title", "시간");
        temp.put("content", time);
        list.add(temp);

        temp = new HashMap<>();
        temp.put("title", "분야");
        temp.put("content", category);
        list.add(temp);

        temp = new HashMap<>();
        temp.put("title", "비용");
        temp.put("content", cost + "원");
        list.add(temp);

        temp = new HashMap<>();
        temp.put("title", "참여한 튜터");
        temp.put("content", parti + "명");
        list.add(temp);

//        temp = new HashMap<>();
//        temp.put("title", "정원");
//        temp.put("content", count + "명");
//        list.add(temp);

//        temp = new HashMap<>();
//        temp.put("title", "관심");
//        temp.put("content", interest + "명");
//        list.add(temp);


        return list;

    }

    public static void setUpdate(){
        isUpdate = true;
    }
    public static void setTutorId(String id){
        tutorId = id;
    }

    private void makeList(){

        ArrayList<HashMap<String, String>> list = makeData();

        adapter = new TutorCustomAdapter(getApplicationContext(), list, participant, getWindow().getDecorView().getRootView());

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(isUpdate){
            rl_join.setVisibility(View.GONE);
            rl_tutor.setVisibility(View.VISIBLE);
        }
    }

}
