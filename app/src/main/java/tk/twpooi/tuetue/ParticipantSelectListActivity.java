package tk.twpooi.tuetue;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flyco.animation.FadeEnter.FadeEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.ParsePHP;

public class ParticipantSelectListActivity extends AppCompatActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_FINISH_ACTIVITY = 501;

    private FrameLayout root;
    private AVLoadingIndicatorView loading;
    private TextView tv_noParticipant;
    private Button completeBtn;
    private LinearLayout li_contentField;

    private int selectionIndex = -1;
    private int defaultCheckDrawable = R.drawable.ic_check_grey600_18dp;
    private int selectCheckDrawable = R.drawable.check;

    private ArrayList<String> participantList;
    private ArrayList<HashMap<String, Object>> participantInfoList;
    private ArrayList<View> participantViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_select_list);

        participantList = (ArrayList<String>)getIntent().getSerializableExtra("participant");
        participantInfoList = new ArrayList<>();
        participantViewList = new ArrayList<>();

        init();

        for(String s : participantList){
            HashMap<String, String> map = new HashMap<>();
            map.put("id", s);
            map.put("service", "getUserInfo");

            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
                @Override
                protected void afterThreadFinish(String data) {
                    HashMap<String, Object> temp = AdditionalFunc.getUserInfo(data);
                    temp.put("select", "0");
                    participantInfoList.add(temp);
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                }
            }.start();

        }

        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));

    }

    public void selectIndex(int index){
        this.selectionIndex = index;
        completeBtn.setEnabled(true);

        for(int i=0; i<participantInfoList.size(); i++){
            HashMap<String, Object> h = participantInfoList.get(i);
            if(i == index){
                h.put("select", "1");
            }else{
                h.put("select", "0");
            }
            participantInfoList.set(i, h);
        }

//        adapter.list = participantInfoList;
//        adapter.notifyDataSetChanged();
    }

    private void init(){

        root = (FrameLayout)findViewById(R.id.activity_participant_list);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        loading = (AVLoadingIndicatorView) findViewById(R.id.loading);
        loading.show();
        completeBtn = (Button)findViewById(R.id.completeBtn);
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectionIndex >= 0 && selectionIndex < participantInfoList.size()) {

                    HashMap<String, Object> h = participantInfoList.get(selectionIndex);
                    final String id = (String) h.get("userId");
                    String nickname = (String) h.get("nickname");
                    String email = (String) h.get("email");
                    String contact = (String) h.get("contact");

                    String text = String.format("정보\n닉네임 : %s\n이메일 : %s\n기타 연락수단 : %s\n\n%s님으로 확정하시겠습니까?", nickname, email, contact, nickname);

                    final MaterialDialog dialog = new MaterialDialog(ParticipantSelectListActivity.this);
                    dialog.content(text)
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

                            Intent intent = new Intent();
                            intent.putExtra("userId", id);
                            setResult(ShowTuetueActivity.SELECT_TUTOR, intent);
                            finish();
                        }
                    };
                    dialog.setOnBtnClickL(left, right);

                } else {
                    final MaterialDialog dialog = new MaterialDialog(ParticipantSelectListActivity.this);
                    dialog.content("튜터를 선택해주세요.")
                            .title("경고")
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
                }
            }
        });

        tv_noParticipant = (TextView) findViewById(R.id.tv_no_participant);
        tv_noParticipant.setVisibility(View.GONE);

        if (participantList.size() <= 0) {
            tv_noParticipant.setVisibility(View.VISIBLE);
            loading.hide();
        }

        li_contentField = (LinearLayout) findViewById(R.id.li_content_field);

    }


    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_MAKE_LIST:
                    if(participantInfoList.size() == participantList.size()) {
                        makeList();
                        loading.hide();
                    }
                    break;
                case MSG_MESSAGE_FINISH_ACTIVITY:
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    private void makeList(){

        li_contentField.removeAllViews();
        participantViewList.clear();
        for (int i = 0; i < participantInfoList.size(); i++) {
            HashMap<String, Object> h = participantInfoList.get(i);

            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.participant_select_list_custom_item, null, false);

            final RelativeLayout root = (RelativeLayout) v.findViewById(R.id.root);
            root.setTag(i);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    intent.putExtra("userId", (String) participantInfoList.get((int) root.getTag()).get("userId"));
                    startActivity(intent);
                }
            });
            ImageView img = (ImageView) v.findViewById(R.id.rl_profile_img);
            TextView tv_nickname = (TextView) v.findViewById(R.id.nickname);
            TextView tv_email = (TextView) v.findViewById(R.id.email);
            final RelativeLayout rl_checkField = (RelativeLayout) v.findViewById(R.id.rl_check_field);
            rl_checkField.setTag(i);
            ImageView checkBtn = (ImageView) v.findViewById(R.id.check_btn);
            checkBtn.setTag(i);

            Picasso.with(getApplicationContext())
                    .load((String) h.get("img"))
                    .transform(new CropCircleTransformation())
                    .into(img);
            tv_nickname.setText((String) h.get("nickname"));
            tv_email.setText((String) h.get("email"));
            checkBtn.setImageResource(defaultCheckDrawable);
            rl_checkField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCheckBtn((int) rl_checkField.getTag());
                }
            });

            participantViewList.add(v);
            li_contentField.addView(v);

        }

    }

    private void setCheckBtn(int index) {

        selectionIndex = -1;

        for (int i = 0; i < participantViewList.size(); i++) {

            View v = participantViewList.get(i);
            ImageView checkBtn = (ImageView) v.findViewById(R.id.check_btn);

            if (index == i) {
                checkBtn.setImageResource(selectCheckDrawable);
                selectionIndex = i;
            } else {
                checkBtn.setImageResource(defaultCheckDrawable);
            }

        }

    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

}
