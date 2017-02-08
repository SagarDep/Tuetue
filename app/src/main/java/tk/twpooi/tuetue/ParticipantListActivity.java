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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.ParsePHP;

public class ParticipantListActivity extends AppCompatActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private ParticipantListCustomAdapter adapter;

    // UI
    private FrameLayout root;
    private TextView tv_noParticipant;
    private Button sendEmailBtn;

    // DATA
    private ArrayList<String> participantList;
    private ArrayList<HashMap<String, Object>> participantInfoList;
    private boolean isEnableEmail;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participant_list);

        progressDialog = new ProgressDialog(this);

        isEnableEmail = getIntent().getBooleanExtra("email", false);
        participantList = (ArrayList<String>)getIntent().getSerializableExtra("participant");
        participantInfoList = new ArrayList<>();

        init();

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

    private void sendEmail(String[] list) {

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, list);
        i.putExtra(Intent.EXTRA_SUBJECT, "튜튜에서 보냅니다.");
        i.putExtra(Intent.EXTRA_TEXT, "내용을 입력해주세요.");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            showSnackbar("설치된 이메일 클라이언트가 존재하지 않습니다.");
        }

    }

    private void init(){

        root = (FrameLayout)findViewById(R.id.activity_participant_list);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        sendEmailBtn = (Button) findViewById(R.id.send_email_btn);
        sendEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] emailList = new String[participantInfoList.size()];
                for (int i = 0; i < participantInfoList.size(); i++) {
                    HashMap<String, Object> h = participantInfoList.get(i);
                    String email = (String) h.get("email");
                    emailList[i] = email;
                }
                sendEmail(emailList);

            }
        });
        if (isEnableEmail) {
            sendEmailBtn.setVisibility(View.VISIBLE);
        } else {
            sendEmailBtn.setVisibility(View.GONE);
        }
        tv_noParticipant = (TextView) findViewById(R.id.tv_no_participant);
        tv_noParticipant.setVisibility(View.GONE);

        if (participantList.size() <= 0) {
            tv_noParticipant.setVisibility(View.VISIBLE);
            sendEmailBtn.setVisibility(View.GONE);
        }

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
//                        init();
                        makeList();
                        progressDialog.hide();
                    }
                    break;
                default:
                    break;
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
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
