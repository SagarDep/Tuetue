package tk.twpooi.tuetue;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.flyco.dialog.widget.NormalListDialog;
import com.rengwuxian.materialedittext.MaterialEditText;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class AddTutorActivity extends AppCompatActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_LOAD_DATA_START = 1001;
    private final int MSG_MESSAGE_LOAD_DATA_FINISH = 1002;
    private final int MSG_MESSAGE_LOAD_DATA_FINISH2 = 1003;
    private final int MSG_MESSAGE_ERROR = 1004;

    private SweetAlertDialog pDialog;

    private Button categoryBtn;
    private MaterialEditText me_cost;
    private MaterialEditText me_count;
    private MaterialEditText me_limit;
    private MaterialEditText me_time;
    private MaterialEditText me_contents;
    private Button addBtn;

    private boolean isCategory;

    private SaveTutor saveTutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tutor);



        init();

    }

    private void selectCategory() {
        final NormalListDialog dialog = new NormalListDialog(this, StartActivity.CATEGORY_LIST);
        dialog.title("카테고리 선택")//
                .titleTextSize_SP(14.5f)//
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ca = StartActivity.CATEGORY_LIST[position];
                categoryBtn.setText(ca);
                isCategory = true;
                categoryBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_btn_bg_pressed_color));
                dialog.dismiss();
            }
        });
        checkAddable();
    }

    private void init(){

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkAddable();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        categoryBtn = (Button) findViewById(R.id.category);
        categoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCategory();
            }
        });
        me_cost = (MaterialEditText)findViewById(R.id.cost);
        me_cost.addTextChangedListener(textWatcher);
        me_count = (MaterialEditText)findViewById(R.id.count);
        me_count.addTextChangedListener(textWatcher);
        me_limit = (MaterialEditText)findViewById(R.id.limit);
        me_limit.addTextChangedListener(textWatcher);
        me_time = (MaterialEditText)findViewById(R.id.time);
        me_time.addTextChangedListener(textWatcher);
        me_contents = (MaterialEditText)findViewById(R.id.contents);
        me_contents.addTextChangedListener(textWatcher);

        addBtn = (Button)findViewById(R.id.addBtn);
        addBtn.setEnabled(false);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GetUserInfo gui = new GetUserInfo(StartActivity.USER_FACEBOOK_ID);
                gui.start();
                try{
                    gui.join();
                }catch (Exception e){

                }

                String id = getID();
                String userId = StartActivity.USER_FACEBOOK_ID;
                String img = gui.getReturnList().get(0).get("img");
                String nickname = gui.getReturnList().get(0).get("nickname");

                String category = categoryBtn.getText().toString();
                String cost = me_cost.getText().toString();
                String count = me_count.getText().toString();
                String limit = me_limit.getText().toString();
                String time = me_time.getText().toString();
                String contents = me_contents.getText().toString();

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("id", id);
                map.put("userid", userId);
                map.put("img", img);
                map.put("nickname", nickname);
                map.put("category", category);
                map.put("cost", cost);
                map.put("count", count);
                map.put("limit", limit);
                map.put("time", time);
                map.put("contents", contents);

                gui.interrupt();

                saveTutor = new SaveTutor(map);
                saveTutor.start();

            }
        });

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);

        checkAddable();


    }

    private boolean isValid(MaterialEditText me){

        return me.getText().toString().length() >= 1;

    }

    private void checkAddable(){

        boolean cost = isValid(me_cost);
        boolean count = isValid(me_count);
        boolean limit = isValid(me_limit);
        boolean time = isValid(me_time);
        boolean contents = isValid(me_contents);

        boolean setting = isCategory && cost && count && limit && time && contents;
        addBtn.setEnabled(setting);
        if(setting){
            addBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_btn_bg_color));
        }else {
            addBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        }

    }

    public String getID(){

        Random rand = new Random();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());
        Date date = new Date();
        String strDate = dateFormat.format(date);
        String randomNumber = Integer.toString(1000 + rand.nextInt(8999));

        String id = strDate + randomNumber;

        return id;
    }

    private class GetUserInfo extends Thread{

        private boolean result;
        private HashMap<String, String> map;
        private ArrayList<HashMap<String, String>> returnList;

        public GetUserInfo(String id){
            map = new HashMap<>();
            map.put("id", id);
            result = false;
        }

        public void run(){

            Message msg;
            msg = handler.obtainMessage(MSG_MESSAGE_LOAD_DATA_START);
            handler.sendMessage(msg);

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

            msg = handler.obtainMessage(MSG_MESSAGE_LOAD_DATA_FINISH);
            handler.sendMessage(msg);

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

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_LOAD_DATA_START:
                    pDialog.show();
                    break;
                case MSG_MESSAGE_LOAD_DATA_FINISH:
                    pDialog.hide();
                    break;
                case MSG_MESSAGE_LOAD_DATA_FINISH2:
                    pDialog.setTitleText("성공")
                            .setContentText("성공적으로 등록하였습니다.")
                            .setConfirmText("확인")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    pDialog.dismiss();
                                    finish();
                                }
                            })
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

                    break;
                case MSG_MESSAGE_ERROR:
                    pDialog.hide();
                    showSnackbar("잠시 후 다시 시도해주세요.");
                    break;
                default:
                    break;
            }
        }
    }

    private class SaveTutor extends Thread{

        private boolean result;
        private HashMap<String, String> map;

        public SaveTutor(HashMap<String, String> map){
            this.map = map;
            result = false;
        }

        public void run(){

            Message msg;
            msg = handler.obtainMessage(MSG_MESSAGE_LOAD_DATA_START);
            handler.sendMessage(msg);

            String addr = Information.MAIN_SERVER_ADDRESS + "saveTutor.php";
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
                msg = handler.obtainMessage(MSG_MESSAGE_LOAD_DATA_FINISH2);
                handler.sendMessage(msg);
            }else{
                result = false;
                msg = handler.obtainMessage(MSG_MESSAGE_ERROR);
                handler.sendMessage(msg);
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

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

}
