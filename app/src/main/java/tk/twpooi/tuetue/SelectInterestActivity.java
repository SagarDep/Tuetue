package tk.twpooi.tuetue;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;

public class SelectInterestActivity extends AppCompatActivity {

    // UI
    private FrameLayout root;
    private LinearLayout btnField;
    private Button completeBtn;
    private TextView notice;
    private MaterialEditText editSearch;

    // Data
    private String[] list;
    private ArrayList<String> listTemp;

    private ArrayList<String> alreadySetItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_interest);

        Intent intent = getIntent();
        alreadySetItem = intent.getStringArrayListExtra("interest");

        if (null == alreadySetItem || alreadySetItem.size() < 0) {
            alreadySetItem = new ArrayList<>();
        }

        list = StartActivity.CATEGORY_LIST.clone();
        listTemp = new ArrayList<>();
        for (String s : list) {
            listTemp.add(s);
        }
//        Random rand = new Random();
//        list = new String[100];
//        for(int i=0; i<100; i++){
//            list[i] = i + "번";
//        }

        init();

    }

    private void init(){

        root = (FrameLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btnField = (LinearLayout) findViewById(R.id.btn_field);
        completeBtn = (Button)findViewById(R.id.completeBtn);
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                complete();
            }
        });
        notice = (TextView)findViewById(R.id.notice);
        editSearch = (MaterialEditText) findViewById(R.id.edit_search);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        makeList();
        checkConfident();

    }

    private void searchList(String sh) {

        listTemp.clear();
        for (String s : list) {
            int i = s.indexOf(sh);
            if (i >= 0) {
                listTemp.add(s);
            }
        }
        makeList();

    }

    private void setText(TextView t, boolean type, boolean addAble) {
        String tag = (String) t.getTag();
        if(type){
            t.setBackgroundResource(R.drawable.round_button_gray);
            if (addAble) {
                alreadySetItem.remove(tag);
            }
        }else{
            t.setBackgroundResource(R.drawable.round_button_blue);
            if (addAble) {
                alreadySetItem.add(tag);
            }
        }
    }

    private void makeList(){

        btnField.removeAllViews();

        final float scale =getResources().getDisplayMetrics().density;
        int dp5 = (int) (5 * scale + 0.5f);
        int dp3 = (int) (3 * scale * 0.5f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp3, dp3, dp3, dp3);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = (int) (320 * scale + 0.5f);//displayMetrics.widthPixels;// / displayMetrics.density;

        if (listTemp != null) {

            LinearLayout mNewLayout = new LinearLayout(this); // Horizontal layout which I am using to add my buttons.
            mNewLayout.setOrientation(LinearLayout.HORIZONTAL);
            int mButtonsSize = 0;
            Rect bounds = new Rect();

            for (int i = 0; i < listTemp.size(); i++) {

                String mButtonTitle = listTemp.get(i);
                final TextView mBtn = new TextView(this);
                mBtn.setPadding(dp5*2, dp5, dp5*2, dp5);
                mBtn.setTag(mButtonTitle);
                mBtn.setBackgroundResource(R.drawable.round_button_gray);
                mBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                mBtn.setText(mButtonTitle);
                mBtn.setLayoutParams(params);
                mBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String tag = (String) mBtn.getTag();
                        setText(mBtn, alreadySetItem.contains(tag), true);
                        checkConfident();
                    }
                });
                setText(mBtn, !alreadySetItem.contains(mButtonTitle), false);

//                paint.getTextBounds(buttonText, 0, buttonText.length(), bounds);
//                int textWidth = bounds.width();


                Paint textPaint = mBtn.getPaint();
                textPaint.getTextBounds(mButtonTitle, 0, mButtonTitle.length(), bounds);
                int textViewWidth = bounds.width() + dp5*4 + dp3*2;

                if(mButtonsSize + textViewWidth < dpWidth - dp3*2 - dp5*2){ // -32 because of extra padding in main layout.
                    mNewLayout.addView(mBtn);
                    mButtonsSize += textViewWidth;
                } else {
                    btnField.addView(mNewLayout);
                    mNewLayout = new LinearLayout(this);
                    mNewLayout.setOrientation(LinearLayout.HORIZONTAL);
                    mButtonsSize = textViewWidth;//bounds.width();
                    mNewLayout.addView(mBtn); // add button to a new layout so it won't be stretched because of it's width.
                }

            }

            btnField.addView(mNewLayout); // add the last layout/ button.
        }

    }

    private void checkConfident(){

        if (alreadySetItem.size() >= 3) {
            completeBtn.setEnabled(true);
            completeBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            notice.setVisibility(View.GONE);
        }else{
            completeBtn.setEnabled(false);
            completeBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_gray));
            notice.setVisibility(View.VISIBLE);
        }

    }

    private void complete(){

//        ArrayList<String> in = new ArrayList<>();
//        for(int i : selectItemIndex){
//            in.add(list[i]);
//        }

        Intent intent = new Intent();
        intent.putExtra("interest", alreadySetItem);
        setResult(1, intent);
        finish();

    }


    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

}
