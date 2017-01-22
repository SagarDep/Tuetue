package tk.twpooi.tuetue;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.TextView;

import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.MaterialDialog;
import com.flyco.dialog.widget.NormalListDialog;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.ParsePHP;

public class AddTuteeActivity extends AppCompatActivity {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_SAVE_SUCCESS = 1000;
    private final int MSG_MESSAGE_SAVE_ERROR = 1001;

//    private SweetAlertDialog pDialog;

    private TextView categoryBtn;
    private MaterialEditText edit_time;
    private MaterialEditText edit_contents;
    private TextView limitBtn;

    private Button addBtn;

    private boolean isCategory;
    private boolean isLimit;

    private long ms_limit;

    private ProgressDialog progressDialog;

//    private SaveTutee saveTutee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tutee);

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
                isCategory = true;
                setDateBtn(categoryBtn, ca);
//                categoryBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red_btn_bg_pressed_color));
                checkAddable();
                dialog.dismiss();
            }
        });
        checkAddable();
    }

    private void init(){

        progressDialog = new ProgressDialog(this);
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

        categoryBtn = (TextView) findViewById(R.id.category_btn);
        categoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectCategory();
            }
        });
        edit_time = (MaterialEditText)findViewById(R.id.edit_time);
        edit_time.addTextChangedListener(textWatcher);
        edit_contents = (MaterialEditText)findViewById(R.id.edit_contents);
        edit_contents.addTextChangedListener(textWatcher);

        limitBtn = (TextView) findViewById(R.id.limit_btn);
        limitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                isLimit = true;
                                setDateBtn(limitBtn, String.format("%s년 %s월 %s일", year, monthOfYear+1, dayOfMonth));
                                ms_limit = AdditionalFunc.getMilliseconds(year, monthOfYear+1, dayOfMonth);
                                checkAddable();
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "모집기한");
            }
        });

        addBtn = (Button)findViewById(R.id.addBtn);
        addBtn.setEnabled(false);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                HashMap<String, Object> user = StartActivity.USER_DATA;
                String contents = AdditionalFunc.replaceNewLineString(edit_contents.getText().toString());
                String time = AdditionalFunc.replaceNewLineString(edit_time.getText().toString());
                String category = categoryBtn.getText().toString();

                HashMap<String, String> map = new HashMap<>();
                map.put("service", "saveTutee");
                map.put("id", getID());
                map.put("userid", StartActivity.USER_ID);
                map.put("img", (String)user.get("img"));
                map.put("email", (String)user.get("email"));
                map.put("nickname", (String)user.get("nickname"));
                map.put("category", category);
                map.put("limit", Long.toString(ms_limit));
                map.put("time", time);
                map.put("contents", contents);

                progressDialog.show();
                new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
                    @Override
                    protected void afterThreadFinish(String data) {
                        System.out.println(data);
                        if("1".equals(data)){
                            handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SAVE_SUCCESS));
                        }else{
                            handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SAVE_ERROR));
                        }
                    }
                }.start();

            }
        });

        checkAddable();

    }

    private void checkAddable(){

        boolean isTime = edit_time.isCharactersCountValid();
        boolean isContents = edit_contents.isCharactersCountValid();

        boolean setting = isCategory && isTime && isContents && isLimit;

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

    private void setDateBtn(TextView tv, String text){

        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
        tv.setTypeface(Typeface.DEFAULT);

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_SAVE_SUCCESS:
                    progressDialog.hide();
                    final MaterialDialog dialog = new MaterialDialog(AddTuteeActivity.this);
                    dialog.title("확인")
                            .content("성공적으로 게시하였습니다.")
                            .btnNum(1)
                            .btnText("확인")
                            .show();
                    dialog.setOnBtnClickL(new OnBtnClickL() {
                        @Override
                        public void onBtnClick() {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    break;
                case MSG_MESSAGE_SAVE_ERROR:
                    progressDialog.hide();
                    final MaterialDialog dialog2 = new MaterialDialog(AddTuteeActivity.this);
                    dialog2.title("에러")
                            .content("잠시 후에 다시 시도해주세요...")
                            .btnNum(1)
                            .btnText("확인")
                            .show();
                    dialog2.setOnBtnClickL(new OnBtnClickL() {
                        @Override
                        public void onBtnClick() {
                            dialog2.dismiss();
                        }
                    });
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
