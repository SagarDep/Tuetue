package tk.twpooi.tuetue;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.ParsePHP;

public class ParticipantListActivity extends AppCompatActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private ParticipantListCustomAdapter adapter;

    private FrameLayout root;

    private ArrayList<String> participantList;
    private ArrayList<HashMap<String, Object>> participantInfoList;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_list);

        progressDialog = new ProgressDialog(this);

        participantList = (ArrayList<String>)getIntent().getSerializableExtra("participant");
        participantInfoList = new ArrayList<>();

        progressDialog.show();
        for(String s : participantList){
            HashMap<String, String> map = new HashMap<>();
            map.put("id", s);
            map.put("service", "getUserInfo");

            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
                @Override
                protected void afterThreadFinish(String data) {
                    participantInfoList.add(AdditionalFunc.getUserInfo(data));
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                }
            }.start();

        }

        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));

    }

    private void init(){

        root = (FrameLayout)findViewById(R.id.activity_participant_list);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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

        makeList();

    }

    private void makeList(){

        adapter = new ParticipantListCustomAdapter(getApplicationContext(), participantInfoList, getWindow().getDecorView().getRootView());

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_MAKE_LIST:
                    if(participantInfoList.size() == participantList.size()) {
                        init();
                        progressDialog.hide();
                    }
                    break;
                default:
                    break;
            }
        }
    }

//    private class GetUserInfo extends Thread{
//
//        private boolean result;
//        private HashMap<String, String> map;
//        private ArrayList<HashMap<String, String>> returnList;
//
//        public GetUserInfo(HashMap<String, String> map){
//            this.map = map;
//            result = false;
//        }
//
//        public void run(){
//
//            String addr = Information.MAIN_SERVER_ADDRESS + "getUser.php";
//            String response = new String();
//
//            try {
//                URL url = new URL(addr);
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 해당 URL에 연결
//
//                conn.setConnectTimeout(10000); // 타임아웃: 10초
//                conn.setUseCaches(false); // 캐시 사용 안 함
//                conn.setRequestMethod("POST"); // POST로 연결
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
//
//                if (map != null) { // 웹 서버로 보낼 매개변수가 있는 경우우
//                    OutputStream os = conn.getOutputStream(); // 서버로 보내기 위한 출력 스트림
//                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8")); // UTF-8로 전송
//                    bw.write(getPostString(map)); // 매개변수 전송
//                    bw.flush();
//                    bw.close();
//                    os.close();
//                }
//
//                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 연결에 성공한 경우
//                    String line;
//                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())); // 서버의 응답을 읽기 위한 입력 스트림
//
//                    while ((line = br.readLine()) != null) // 서버의 응답을 읽어옴
//                        response += line;
//                }
//
//                conn.disconnect();
//            } catch (MalformedURLException me) {
//                me.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            String str = response.toString();
//            returnList = new ArrayList<>();
//            try {
//                // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
//                JSONObject jObject = new JSONObject(str);
//                // results라는 key는 JSON배열로 되어있다.
//                JSONArray results = jObject.getJSONArray("result");
//                String countTemp = (String)jObject.get("num_result");
//                int count = Integer.parseInt(countTemp);
//
////                HashMap<String, String> hashTemp = new HashMap<>();
//                for ( int i = 0; i < count; ++i ) {
//                    JSONObject temp = results.getJSONObject(i);
//
//                    HashMap<String, String> hashTemp = new HashMap<>();
//                    hashTemp.put("userId", (String)temp.get("id"));
//                    hashTemp.put("intro", (String)temp.get("intro"));
//                    hashTemp.put("img", (String)temp.get("img"));
//                    hashTemp.put("nickname", (String)temp.get("nickname"));
//
//                    returnList.add(hashTemp);
//
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        private String getPostString(HashMap<String, String> map) {
//            StringBuilder result = new StringBuilder();
//            boolean first = true; // 첫 번째 매개변수 여부
//
//            for (Map.Entry<String, String> entry : map.entrySet()) {
//                if (first)
//                    first = false;
//                else // 첫 번째 매개변수가 아닌 경우엔 앞에 &를 붙임
//                    result.append("&");
//
//                try { // UTF-8로 주소에 키와 값을 붙임
//                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
//                    result.append("=");
//                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
//                } catch (UnsupportedEncodingException ue) {
//                    ue.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            return result.toString();
//        }
//
//        public boolean getResult(){
//            return result;
//        }
//
//        public ArrayList<HashMap<String, String>> getReturnList(){
//            return returnList;
//        }
//
//    }



    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
