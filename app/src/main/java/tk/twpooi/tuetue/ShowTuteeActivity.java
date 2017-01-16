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
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.ParsePHP;


public class ShowTuteeActivity extends AppCompatActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_PROGRESS_HIDE = 502;
    private final int MSG_MESSAGE_SET_BUTTON_TRUE = 503;
    private final int MSG_MESSAGE_SET_BUTTON_FALSE = 504;
    private final int MSG_MESSAGE_REFRESH_PROGRESS = 1001;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private TutorCustomAdapter adapter;

    private HashMap<String, Object> item;

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
    private String img;
    public static String tutorId;
    private int count;
    private int cost;
    private ArrayList<String> participant;

    public static boolean isUpdate;

    private TextRoundCornerProgressBar progressBar;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tutor);

        item = new HashMap<>();
        progressDialog = new ProgressDialog(this);

        id = getIntent().getStringExtra("id");
        HashMap<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("service", "getTuteeList");

        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                item.clear();
                item = AdditionalFunc.getTuteeList(data).get(0);
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
            }
        }.start();

    }

    private void init(){

        userId = (String)item.get("userid");
        String nickname = (String)item.get("nickname");
        img = (String)item.get("img");
//        cost = (int)data.get("cost");
//        count = (int)data.get("count");
        participant = (ArrayList<String>)item.get("participant");
        String isFinish = (String)item.get("isFinish");
        tutorId = (String)item.get("tutorId");

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



        if(participant.contains(StartActivity.USER_ID)){
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

        makeList();

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_MAKE_LIST:
                    progressDialog.hide();
                    init();
                    break;
                case MSG_MESSAGE_PROGRESS_HIDE:
                    progressDialog.hide();
                    break;
                case MSG_MESSAGE_SET_BUTTON_FALSE:
                    progressDialog.hide();
                    setButton(false);
                    makeList();
                    break;
                case MSG_MESSAGE_SET_BUTTON_TRUE:
                    progressDialog.hide();
                    setButton(true);
                    makeList();
                    break;
                case MSG_MESSAGE_REFRESH_PROGRESS:
                    float percent = ((float)participant.size()/count)*100;
                    progressBar.setProgress(percent);
                    progressBar.setProgressText(participant.size() + "/" + count);
                    break;
                default:
                    break;
            }
        }
    }

    private void setButton(boolean check){

        if(userId.equals(StartActivity.USER_ID)){
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

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("service", "updateParticipant");
                        map.put("id", id);
                        map.put("participant", StartActivity.USER_ID);
                        map.put("table", "tutee");
                        map.put("mode", "1");

                        updateParticipant(map, false);

                    }
                });
            }else{
                jo_cost.setVisibility(View.GONE);
                jo_join.setText("철회하기");
                rl_join.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_background_color));
                rl_join.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("service", "updateParticipant");
                        map.put("id", id);
                        map.put("participant", StartActivity.USER_ID);
                        map.put("table", "tutee");
                        map.put("mode", "0");

                        updateParticipant(map, true);

                    }
                });
            }

        }

    }

    private void updateParticipant(HashMap<String, String> map, final boolean check){

        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {
                System.out.println("print : " + data);


                ArrayList<String> par = new ArrayList<>();
                if(!data.equals("")){
                    String[] p = data.split(",");
                    for(String s : p){
                        par.add(s);
                    }
                }
                if (par.size() > 0 && par.get(0).equals("")) {
                    par.remove(0);
                }
                participant = par;
//
                if(check){
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SET_BUTTON_TRUE));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SET_BUTTON_FALSE));
                }
//                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_REFRESH_PROGRESS));

            }
        }.start();

    }

    private void setProgressBar(){

        progressBar = (TextRoundCornerProgressBar)findViewById(R.id.progress);
        progressBar.setMax(100);
        progressBar.setProgress(0);


    }

    private ArrayList<HashMap<String, String>> makeData(){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        String contents = (String)item.get("contents");
        String time = (String)item.get("time");
        String category = (String)item.get("category");
        int cost = (int)item.get("cost");
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
