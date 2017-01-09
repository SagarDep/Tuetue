package tk.twpooi.tuetue;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

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

public class ParticipantSelectListActivity extends AppCompatActivity {

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private ParticipantSelectListCustomAdapter adapter;

    private FrameLayout root;
    private Button completeBtn;

    private int selectionIndex = -1;

    private ArrayList<String> participantList;
    private ArrayList<HashMap<String, String>> participantInfoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_select_list);

        participantList = (ArrayList<String>)getIntent().getSerializableExtra("participant");
        participantInfoList = new ArrayList<>();

        for(String s : participantList){
            HashMap<String, String> map = new HashMap<>();
            map.put("id", s);

            GetUserInfo gui = new GetUserInfo(map);
            gui.start();
            try{
                gui.join();
            }catch (Exception e){

            }
            participantInfoList.add(gui.getReturnList().get(0));
            gui.interrupt();

        }

        init();

    }

    public void selectIndex(int index){
        this.selectionIndex = index;
        completeBtn.setEnabled(true);

        for(int i=0; i<participantInfoList.size(); i++){
            HashMap<String, String> h = participantInfoList.get(i);
            if(i == index){
                h.put("select", "1");
            }else{
                h.put("select", "0");
            }
            participantInfoList.set(i, h);
        }

        adapter.list = participantInfoList;
        adapter.notifyDataSetChanged();
    }

    private void init(){

        root = (FrameLayout)findViewById(R.id.activity_participant_list);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        completeBtn = (Button)findViewById(R.id.completeBtn);
        completeBtn.setEnabled(false);
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tutorId = participantList.get(selectionIndex);

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", ShowTuteeActivity.id);
                map.put("type", "tutee");
                map.put("tutorId", tutorId);

                UpdateFinish gti = new UpdateFinish(map);
                gti.start();
                try{
                    gti.join();
                }catch (Exception e){

                }

                ShowTuteeActivity.isUpdate = true;
                ShowTuteeActivity.setTutorId(tutorId);

                TuteeListFragment.setTuteeUpdate(ShowTuteeActivity.id);

                finish();

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

    class UpdateFinish extends Thread {

        private boolean result;
        private HashMap<String, String> map;

        public UpdateFinish(HashMap<String, String> map){
            this.map = map;
            result = false;
        }

        public void run(){

            String addr = Information.MAIN_SERVER_ADDRESS + "updateFinish.php";
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

                System.out.println(response);

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

    private void makeList(){

        adapter = new ParticipantSelectListCustomAdapter(getApplicationContext(), participantInfoList, getWindow().getDecorView().getRootView(), this);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

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
                    hashTemp.put("select", "0");

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



    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

}
