package tk.twpooi.tuetue;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.ParsePHP;

public class WtInfoActivity extends AppCompatActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_FINISH = 500;

    // UI
    private ImageView profileImage;
    private TextView profileName;

    private MaterialEditText editNickName;
    private MaterialEditText editContact;
    private MaterialEditText editIntro;
    private MaterialEditText editEmail;
    private Button nextBtn;

    // UI - Interest Field
    private LinearLayout interestField;
    private TextView interestBtn;


    // User Data
    private String id;
    private String img;
    private String name;
    private String email;
    private ArrayList<String> interest;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wtinfo);

        initData();

        init();

    }

    private void initData(){

        Intent intent = getIntent();

        id = intent.getStringExtra("id");
        img = intent.getStringExtra("img");
        name = intent.getStringExtra("name");
        email = intent.getStringExtra("email");
        interest = new ArrayList<>();

    }

    private void init(){

        profileImage = (ImageView)findViewById(R.id.profileImg);
        profileName = (TextView)findViewById(R.id.profileName);

        editNickName = (MaterialEditText)findViewById(R.id.edit_nickname);
        editContact = (MaterialEditText)findViewById(R.id.edit_contact);
        editIntro = (MaterialEditText)findViewById(R.id.edit_intro);
        editEmail = (MaterialEditText)findViewById(R.id.edit_email);

        interestField = (LinearLayout)findViewById(R.id.interest_field);
        interestBtn = (TextView)findViewById(R.id.interest_btn);
        interestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectSelectActivity();
            }
        });
        nextBtn = (Button)findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        setNextButton(false);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkEditText();
            }
        };

        editNickName.addTextChangedListener(textWatcher);
        editContact.addTextChangedListener(textWatcher);
        editIntro.addTextChangedListener(textWatcher);
        editEmail.addTextChangedListener(textWatcher);


        Picasso.with(getApplicationContext())
                .load(img)
                .transform(new CropCircleTransformation())
                .into(profileImage);
        profileName.setText(name + "님");

        editEmail.setText(email);
        editContact.setText(email);

        progressDialog = new ProgressDialog(this);

    }

    private void checkEditText(){

        boolean nick = editNickName.isCharactersCountValid();
        boolean contact = editContact.isCharactersCountValid();
        boolean intro = editIntro.isCharactersCountValid();
        boolean em = editEmail.isCharactersCountValid();
        boolean inter = interest.size() >= 3;

        setNextButton(nick && contact && intro && em && inter);

    }

    private void setNextButton(boolean type){

        if(type){
            nextBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            nextBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        }else{
            nextBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
            nextBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.light_gray));
        }

        nextBtn.setEnabled(type);

    }

    private void setInterestField(){

        if(interest.size() <= 0){
            interestBtn.setVisibility(View.VISIBLE);
            return;
        }else{
            interestBtn.setVisibility(View.GONE);
        }

        interestField.removeAllViews();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpWidth -= 85;

        final float scale =getResources().getDisplayMetrics().density;
        int width = (int) (dpWidth * scale + 0.5f);
        int dp5 = (int) (5 * scale + 0.5f);
        int dp3 = (int) (3 * scale * 0.5f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp3, dp3, dp3, dp3);
        int mButtonsSize = 0;
        Rect bounds = new Rect();

        boolean isAdd = false;
        TextView finalText = new TextView(this);
        finalText.setPadding(dp5*2, dp5, dp5*2, dp5);
        finalText.setBackgroundResource(R.drawable.round_button_blue);
        finalText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        finalText.setText("+" + interest.size());
        finalText.setLayoutParams(params);
        finalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectSelectActivity();
            }
        });

        for(int i=0; i<interest.size(); i++){

            int remainCount = interest.size() - i;
            String remainString = "+" + remainCount;
            finalText.setText(remainString);

            String s = interest.get(i);
            TextView mBtn = new TextView(this);
            mBtn.setPadding(dp5*2, dp5, dp5*2, dp5);
            mBtn.setBackgroundResource(R.drawable.round_button_blue_line);
            mBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.pastel_blue));
            mBtn.setText(s);
            mBtn.setLayoutParams(params);

            Paint textPaint = mBtn.getPaint();
            textPaint.getTextBounds(s, 0, s.length(), bounds);
            int textWidth = bounds.width() + dp5*4 + dp3*2;

            Rect tempBounds = new Rect();
            textPaint = finalText.getPaint();
            textPaint.getTextBounds(remainString, 0, remainString.length(), tempBounds);
            int remainWidth = tempBounds.width() + dp5*4 + dp3*2;

            if(mButtonsSize + textWidth + remainWidth < width){
                interestField.addView(mBtn);
                mButtonsSize += textWidth;
            }else{
                interestField.addView(finalText);
                isAdd = true;
                break;
            }

        }

        if(!isAdd){
            finalText.setText("+0");
            interestField.addView(finalText);
        }

    }

    private void saveUserInformation(){

        email = editEmail.getText().toString();
        String nickname = editNickName.getText().toString();
        String intro = editIntro.getText().toString();
        String contact = editContact.getText().toString();
        String inter = "";
        for(int i=0; i<interest.size(); i++){
            inter += interest.get(i);
            if(i+1 < interest.size()){
                inter += ",";
            }
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("service", "saveUser");
        map.put("id", id);
        map.put("img", img);
        map.put("name", name);
        map.put("email", email);
        map.put("nickname", nickname);
        map.put("intro", intro);
        map.put("contact", contact);
        map.put("interest", inter);


        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                if("1".equals(data)) {
                    StartActivity.USER_ID = id;

                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("service", "getUserInfo");
                    map.put("id", id);
                    new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
                        @Override
                        protected void afterThreadFinish(String data) {
                            StartActivity.USER_DATA = AdditionalFunc.getUserInfo(data);
                            handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_FINISH));
                        }
                    }.start();

                }
            }
        }.start();

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_FINISH:
                    progressDialog.hide();
                    redirectMainActivity();
                    break;
                default:
                    break;
            }
        }
    }

    public void redirectSelectActivity() {
        Intent intent = new Intent(this, SelectInterestActivity.class);
        intent.putStringArrayListExtra("interest", interest);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 1:
                ArrayList<String> temp = (ArrayList<String>)data.getSerializableExtra("interest");

                boolean check = true;

                if(temp.size() == interest.size()) {
                    for (String s : temp) {
                        if (!interest.contains(s)) {
                            check = false;
                            break;
                        }
                    }
                }else{
                    check = false;
                }

                interest = temp;

                if(!check) {
                    showSnackbar("관심분야가 수정되었습니다.");
                    setInterestField();
                }

                checkEditText();
                break;
            default:
                break;
        }
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

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

}
