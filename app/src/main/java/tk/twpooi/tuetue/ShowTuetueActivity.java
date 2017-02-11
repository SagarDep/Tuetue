package tk.twpooi.tuetue;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.Theme;
import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar;
import com.flyco.animation.FadeEnter.FadeEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.ParsePHP;


public class ShowTuetueActivity extends AppCompatActivity {

    public static final int SELECT_TUTOR = 30;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_PROGRESS_HIDE = 502;
    private final int MSG_MESSAGE_SET_BUTTON_TRUE = 503;
    private final int MSG_MESSAGE_SET_BUTTON_FALSE = 504;
    private final int MSG_MESSAGE_REFRESH_PROGRESS = 1001;
    private final int MSG_MESSAGE_REFRESHVIEW = 1002;
    private final int MSG_MESSAGE_SHOW_ERROR_MESSAGE = 1003;

    public final static int EDIT_CONTENTS = 1;

    private HashMap<String, Object> item;
    private HashMap<String, View> svItem;

    private RelativeLayout rl_progress;

    private Toolbar toolbar;
    private RelativeLayout rl_profile;
    private TextView tv_finish;
    private ImageView profileImg;
    private TextView tv_nickname;
    private TextView tv_email;
    private TextView tv_dday;

    // Scroll View
    private int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;
    private int previous_scrollY = 0;
    private ScrollView sv;
    private LinearLayout infoField;

    private FloatingActionsMenu menu;
    private FloatingActionButton fabReport;
    private FloatingActionButton fabEdit;
    private TextView joinBtn;

    private int type;
    public final static int TYPE_TUTOR_LIST = 0;
    public final static int TYPE_TUTEE_LIST = 1;
    public final static int TYPE_USER_TUTOR_LIST = 2;
    public final static int TYPE_USER_TUTEE_LIST = 3;

    private int index;
    private String id;
    private boolean isFinishArticle;
    private boolean isMyArticle;

    private boolean isFinishLoading;

    private TextRoundCornerProgressBar progressBar;

    private com.afollestad.materialdialogs.MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_tuetue);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        index = intent.getIntExtra("index", -1);
        type = intent.getIntExtra("type", -1);

        item = new HashMap<>();
        svItem = new HashMap<>();
        progressDialog = new com.afollestad.materialdialogs.MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

        init();

        HashMap<String, String> map = new HashMap<>();
        map.put("id", id);
        if (isTutorContents()) {
            map.put("service", "getTutorList");
            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

                @Override
                protected void afterThreadFinish(String data) {
                    item.clear();
                    item = AdditionalFunc.getTutorList(data).get(0);
                    isFinishLoading = true;
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                }
            }.start();
        }else{
            map.put("service", "getTuteeList");
            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

                @Override
                protected void afterThreadFinish(String data) {
                    item.clear();
                    item = AdditionalFunc.getTuteeList(data).get(0);
                    isFinishLoading = true;
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                }
            }.start();
        }

        makeScrollView();

    }

    private boolean isTutorContents() {
        if (type == TYPE_TUTOR_LIST || type == TYPE_USER_TUTOR_LIST) {
            return true;
        } else {
            return false;
        }
    }

    private void init(){

        rl_progress = (RelativeLayout) findViewById(R.id.rl_progress);
        if (!isTutorContents()) {
            rl_progress.setVisibility(View.GONE);
        }

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        tv_finish = (TextView) findViewById(R.id.tv_finish);
        rl_profile = (RelativeLayout)findViewById(R.id.rl_profile);
        profileImg = (ImageView)findViewById(R.id.profileImg);
        tv_nickname = (TextView)findViewById(R.id.tv_nickname);
        tv_email = (TextView)findViewById(R.id.tv_email);
        tv_dday = (TextView)findViewById(R.id.tv_dday);

        sv = (ScrollView)findViewById(R.id.sv);
//        sv.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
//            @Override
//            public void onScrollChanged() {
//                int svScrollY = Math.max(sv.getScrollY(), 0);
//                int svY = (int)sv.getY();
//                int scrollY = svScrollY - svY; // For ScrollView
//                System.out.println(sv.getY() + ", " + sv.getPivotY() + ", " + sv.getRotationY() + ", " + sv.getScaleY() + ", " + sv.getScrollY() + ", " + sv.getTranslationY());
//                System.out.println(scrollY);
//                int dy = scrollY - previous_scrollY;
//                previous_scrollY = scrollY;
//                int scrollX = sv.getScrollX(); // For HorizontalScrollView
//
//                if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
//                    onHide();
//                    controlsVisible = false;
//                    scrolledDistance = 0;
//                } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
//                    onShow();
//                    controlsVisible = true;
//                    scrolledDistance = 0;
//                }
//
//                if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
//                    scrolledDistance += dy;
//                }
//            }
//        });
        infoField = (LinearLayout)findViewById(R.id.li_info_field);

        menu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        fabEdit = (FloatingActionButton) findViewById(R.id.fab_edit);
        fabEdit.setTitle("편집");
        fabEdit.setVisibility(View.GONE);
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTutorContents()) {
                    Intent intent = new Intent(getApplicationContext(), AddTutorActivity.class);
                    intent.putExtra("edit", true);
                    intent.putExtra("id", id);
                    startAddActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), AddTuteeActivity.class);
                    intent.putExtra("edit", true);
                    intent.putExtra("id", id);
                    startAddActivity(intent);
                }
                menu.toggle();
            }
        });

        fabReport = (FloatingActionButton) findViewById(R.id.fab_report);
        fabReport.setTitle("게시글 신고");
        fabReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFinishLoading) {
                    String type;
                    if (isTutorContents()) {
                        type = "재능나눔";
                    } else {
                        type = "재능기부";
                    }
                    String articleId = (String) item.get("id");
                    String nickname = (String) item.get("nickname");
                    String email = (String) item.get("email");
                    String category = (String) item.get("category");
                    String contents = (String) item.get("contents");

                    String msg = String.format("신고자 ID : %s\n신고 내용을 입력해주세요 : \n\n\n\n------------------\n게시글 ID : %s\n분류 : %s\n게시자 닉네임 : %s\n게시자 이메일 : %s\n게시글 카테고리 : %s\n게시글 내용\n%s\n", StartActivity.USER_ID, articleId, type, nickname, email, category, contents);

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("message/rfc822");
                    i.putExtra(Intent.EXTRA_EMAIL, new String[]{Information.ADMINISTRATOR_EMAIL});
                    i.putExtra(Intent.EXTRA_SUBJECT, "튜튜 게시글 신고");
                    i.putExtra(Intent.EXTRA_TEXT, msg);
                    try {
                        startActivity(Intent.createChooser(i, "Send mail..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        showSnackbar("설치된 이메일 클라이언트가 존재하지 않습니다.");
                    }

                }
                menu.toggle();
            }
        });
        joinBtn = (TextView)findViewById(R.id.join);

        setProgressBar();

    }

    private void startAddActivity(Intent intent) {
        this.startActivityForResult(intent, 0);
    }

    private void onShow(){
//        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        toolbar.setVisibility(View.VISIBLE);
        System.out.println("onShow");
    }
    private void onHide(){
//        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        toolbar.setVisibility(View.GONE);
        System.out.println("onHide");
    }

    private void checkData() {

        isMyArticle = StartActivity.USER_ID.equals((String) item.get("userid"));

        int dday = AdditionalFunc.getDday((Long) item.get("limit"));
        if (dday < 0) {
            isFinishArticle = true;
            return;
        }

        if ("1".equals((String) item.get("isFinish"))) {
            isFinishArticle = true;
            return;
        }

        isFinishArticle = false;

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_MAKE_LIST:
                    checkData();
                    setProfile();
                    refreshView();
                    setFinishUI();
                    break;
                case MSG_MESSAGE_PROGRESS_HIDE:
                    progressDialog.hide();
                    break;
                case MSG_MESSAGE_SET_BUTTON_FALSE:
                    progressDialog.hide();
                    checkData();
                    refreshView();
                    setFinishUI();
                    setButton(false);
                    break;
                case MSG_MESSAGE_SET_BUTTON_TRUE:
                    progressDialog.hide();
                    checkData();
                    refreshView();
                    setFinishUI();
                    setButton(true);
                    break;
                case MSG_MESSAGE_REFRESH_PROGRESS:
                    ArrayList<String> participant = (ArrayList<String>)item.get("participant");
                    int count = (int)item.get("count");
                    float percent = ((float)participant.size()/count)*100;
                    progressBar.setProgress(percent);
                    progressBar.setProgressText(participant.size() + "/" + count);
                    break;
                case MSG_MESSAGE_REFRESHVIEW:
                    progressDialog.hide();
                    checkData();
                    setProfile();
                    refreshView();
                    setFinishUI();
                    break;
                case MSG_MESSAGE_SHOW_ERROR_MESSAGE:
                    progressDialog.hide();
                    final MaterialDialog dialog = new MaterialDialog(ShowTuetueActivity.this);
                    dialog.content("잠시 후 다시 시도해주세요.")
                            .title("에러")
                            .btnText("확인")
                            .btnNum(1)
                            .showAnim(new FadeEnter())
                            .show();
                    dialog.setOnBtnClickL(new OnBtnClickL() {
                        @Override
                        public void onBtnClick() {
                            dialog.hide();
                            dialog.dismiss();
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

    private void makeScrollView(){

        if (isTutorContents()) { // Tutor

            for(HashMap<String, String> map : AdditionalFunc.getShowTutorList()){

                View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.basic_list_custom2_item, null, false);

                TextView title = (TextView)v.findViewById(R.id.title);
                TextView content = (TextView)v.findViewById(R.id.content);
                AVLoadingIndicatorView av = (AVLoadingIndicatorView)v.findViewById(R.id.loading);

                title.setText(map.get("ko"));
                content.setText("");
                av.show();

                infoField.addView(v);
                svItem.put(map.get("en"), v);

            }

        }else{ // Tutee

            for(HashMap<String, String> map : AdditionalFunc.getShowTuteeList()){

                View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.basic_list_custom2_item, null, false);

                TextView title = (TextView)v.findViewById(R.id.title);
                TextView content = (TextView)v.findViewById(R.id.content);
                AVLoadingIndicatorView av = (AVLoadingIndicatorView)v.findViewById(R.id.loading);

                title.setText(map.get("ko"));
                content.setText("");
                av.show();

                infoField.addView(v);
                svItem.put(map.get("en"), v);

            }

        }

    }

    private void setFinishUI() {
        if (isFinishArticle) {
            tv_dday.setText("마감");
            tv_finish.setVisibility(View.VISIBLE);
            joinBtn.setVisibility(View.GONE);
        } else {
            int dday = AdditionalFunc.getDday((Long) item.get("limit"));
            if (dday < 0) {
//            tv_dday.setText("D+"+Math.abs(dday));
            } else if (dday == 0) {
                tv_dday.setText("D-day");
            } else {
                tv_dday.setText("D-" + dday);
            }
            tv_finish.setVisibility(View.GONE);
            joinBtn.setVisibility(View.VISIBLE);
        }
    }

    private void setProfile(){

        // 프로필 이미지 설정
        Picasso.with(getApplicationContext())
                .load((String)item.get("img"))
                .transform(new CropCircleTransformation())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(profileImg);
        tv_nickname.setText((String)item.get("nickname"));
        tv_email.setText((String)item.get("email"));
        int dday = AdditionalFunc.getDday((Long)item.get("limit"));
        if(dday < 0){
//            tv_dday.setText("D+"+Math.abs(dday));
        }else if(dday == 0){
            tv_dday.setText("D-day");
        }else{
            tv_dday.setText("D-"+dday);
        }

    }

    private void refreshView(){

        for(String s : svItem.keySet()){

            if("period".equals(s)){

                long start = (Long)item.get("start");
                long finish = (Long)item.get("finish");

                String st = AdditionalFunc.getDateString(start);
                String fi = AdditionalFunc.getDateString(finish);

                View v = svItem.get(s);
                TextView content = (TextView)v.findViewById(R.id.content);
                AVLoadingIndicatorView av = (AVLoadingIndicatorView)v.findViewById(R.id.loading);

                content.setText(st + "\n~ " + fi);
                av.hide();

            }else{

                if(item.containsKey(s)){

                    View v = svItem.get(s);
                    TextView content = (TextView)v.findViewById(R.id.content);
                    AVLoadingIndicatorView av = (AVLoadingIndicatorView)v.findViewById(R.id.loading);

                    Object o = item.get(s);
                    if(o instanceof Integer){
                        content.setText((int)o + "명");
                    }else if(o instanceof String){
                        content.setText((String)o);
                    }else if(o instanceof ArrayList){
                        int c = ((ArrayList) o).size();
                        content.setText(c+"명");
                    }
                    av.hide();

                }

            }

        }

        if (isTutorContents()) {

            rl_progress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ParticipantListActivity.class);
                    intent.putExtra("participant", (ArrayList<String>)item.get("participant"));
                    intent.putExtra("email", isMyArticle);
                    startActivity(intent);
                }
            });


            ArrayList<String> participant = (ArrayList<String>)item.get("participant");
            if(participant.contains(StartActivity.USER_ID)){
                setButton(false);
            }else{
                setButton(true);
            }

            int count = (int)item.get("count");
            float percent = ((float)participant.size()/count)*100;
            progressBar.setProgress(percent);
            progressBar.setProgressText(participant.size() + "/" + count);

        }else{

            ArrayList<String> participant = (ArrayList<String>)item.get("participant");
            if(participant.contains(StartActivity.USER_ID)){
                setButton(false);
            }else{
                setButton(true);
            }

            final String tutorId = (String) item.get("tutorId");
            if (!"".equals(tutorId) || tutorId != null) {
                tv_finish.setText("마감(튜터보기)");
                tv_finish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        intent.putExtra("userId", tutorId);
                        startActivity(intent);
                    }
                });
            } else {
                tv_finish.setText("마감");
                tv_finish.setOnClickListener(null);
            }

        }

        rl_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", (String)item.get("userid"));
                startActivity(intent);
            }
        });

        if (isMyArticle && !"1".equals((String) item.get("isFinish"))) {
            fabEdit.setVisibility(View.VISIBLE);
        } else {
            fabEdit.setVisibility(View.GONE);
        }

    }

    private void setButton(boolean check){

        joinBtn.setVisibility(View.VISIBLE);
        if (isMyArticle) {

            if (isTutorContents()) {
                joinBtn.setText("마감하기");
                joinBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.facebook_blue));
                joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        checkFinish();

                    }
                });
            }else{
                joinBtn.setText("선택하기");
                joinBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.facebook_blue));
                joinBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), ParticipantSelectListActivity.class);
                        intent.putExtra("participant", (ArrayList<String>)item.get("participant"));
                        intent.putExtra("id", id);
                        startActivityForResult(intent, SELECT_TUTOR);
                    }
                });
            }


        }else{

            if (isTutorContents()) {
                if(check){ // 참가하기
                    ArrayList<String> participant = (ArrayList<String>)item.get("participant");
                    int count = (int)item.get("count");
                    joinBtn.setText("참가하기 (" + participant.size() + "/" + count + ")");
                    joinBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    joinBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("service", "updateParticipant");
                            map.put("id", id);
                            map.put("participant", StartActivity.USER_ID);
                            map.put("table", "tutor");
                            map.put("mode", "1");

                            updateTutorParticipant(map, false);


                        }
                    });
                }else{
                    joinBtn.setText("철회하기");
                    joinBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_background_color));
                    joinBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("service", "updateParticipant");
                            map.put("id", id);
                            map.put("participant", StartActivity.USER_ID);
                            map.put("table", "tutor");
                            map.put("mode", "0");

                            updateTutorParticipant(map, true);

                        }
                    });
                }
            }else{
                if(check){ // 참가하기
                    joinBtn.setText("참가하기");
                    joinBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                    joinBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("service", "updateParticipant");
                            map.put("id", id);
                            map.put("participant", StartActivity.USER_ID);
                            map.put("table", "tutee");
                            map.put("mode", "1");

                            updateTuteeParticipant(map, false);

                        }
                    });
                }else{
                    joinBtn.setText("철회하기");
                    joinBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_background_color));
                    joinBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("service", "updateParticipant");
                            map.put("id", id);
                            map.put("participant", StartActivity.USER_ID);
                            map.put("table", "tutee");
                            map.put("mode", "0");

                            updateTuteeParticipant(map, true);

                        }
                    });
                }
            }

        }

    }

    private void setProgressBar(){

        progressBar = (TextRoundCornerProgressBar)findViewById(R.id.progress);
        progressBar.setMax(100);
        progressBar.setProgress(0);

    }

    private void checkFinish() {

        if (item == null) {
            return;
        }
        ArrayList<String> participant = (ArrayList<String>) item.get("participant");

        final MaterialDialog dialog = new MaterialDialog(ShowTuetueActivity.this);
        dialog.content("현재 " + participant.size() + "명이 참가하였습니다.\n마감하시겠습니까?")
                .title("확인")
                .btnText("취소", "확인")
                .showAnim(new FadeEnter())
                .show();
        OnBtnClickL left = new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.hide();
                dialog.dismiss();
            }
        };
        OnBtnClickL right = new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.hide();
                dialog.dismiss();

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("service", "updateFinish");
                map.put("id", id);
                map.put("table", "tutor");

                progressDialog.show();
                new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

                    @Override
                    protected void afterThreadFinish(String data) {

                        item.put("isFinish", "1");

                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SET_BUTTON_FALSE));
                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));

                        if (index >= 0) {
                            TutorListFragment.setRefresh(index);
                        }
                    }
                }.start();

            }
        };
        dialog.setOnBtnClickL(left, right);

    }

    private void updateTutorParticipant(HashMap<String, String> map, final boolean check){

        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {

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
//                participant = par;
                item.put("participant", par);
//
                if(check){
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SET_BUTTON_TRUE));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SET_BUTTON_FALSE));
                }
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_REFRESH_PROGRESS));

            }
        }.start();

    }
    private void updateTuteeParticipant(HashMap<String, String> map, final boolean check){

        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){

            @Override
            protected void afterThreadFinish(String data) {

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
//                participant = par;
                item.put("participant", par);

                if(check){
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SET_BUTTON_TRUE));
                }else{
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SET_BUTTON_FALSE));
                }

            }
        }.start();

    }

    private void addTutorIdToTutee(final String tutorId) {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service", "updateFinish");
        map.put("id", id);
        map.put("table", "tutee");
        map.put("tutorId", tutorId);

        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

            @Override
            protected void afterThreadFinish(String data) {

                if ("1".equals(data)) {
                    item.put("isFinish", "1");
                    item.put("tutorId", tutorId);
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_REFRESHVIEW));
                } else {
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SHOW_ERROR_MESSAGE));
                }

            }
        }.start();

    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        if (isFinishLoading) {
            Intent intent = new Intent();
            intent.putExtra("item", item);
            intent.putExtra("index", index);
            switch (type) {
                case TYPE_TUTOR_LIST:
                    this.setResult(MainActivity.RESULT_CODE_TUTOR_LIST_FRAGMENT, intent);
                    break;
                case TYPE_TUTEE_LIST:
                    this.setResult(MainActivity.RESULT_CODE_TUTEE_LIST_FRAGMENT, intent);
                    break;
                case TYPE_USER_TUTOR_LIST:
                    this.setResult(MainActivity.RESULT_CODE_USER_TUTOR_LIST_FRAGMENT, intent);
                    break;
                case TYPE_USER_TUTEE_LIST:
                    this.setResult(MainActivity.RESULT_CODE_USER_TUTEE_LIST_FRAGMENT, intent);
                    break;
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("resultCode : " + resultCode);
        switch (resultCode) {
            case EDIT_CONTENTS:
                item = (HashMap<String, Object>) data.getSerializableExtra("item");
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                break;
            case SELECT_TUTOR:
                String userId = data.getStringExtra("userId");
                addTutorIdToTutee(userId);
//                showSnackbar(userId);
                break;
            default:
                break;
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
