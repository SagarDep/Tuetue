package tk.twpooi.tuetue;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;

public class SelectInterestActivity extends AppCompatActivity {

    // UI
    private FrameLayout root;
    private LinearLayout btnField;
    private Button completeBtn;
    private TextView notice;

    // Data
    private String[] list = {"IT", "Design", "Test", "Min", "Max", "Short"};

    private ArrayList<Integer> selectItemIndex;

    private ArrayList<String> alreadySetItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_interest);

        Intent intent = getIntent();
        alreadySetItem = intent.getStringArrayListExtra("interest");

        list = StartActivity.CATEGORY_LIST.clone();
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

        selectItemIndex = new ArrayList<>();

        makeList();
        checkConfident();

    }

    private void setText(TextView t, boolean type){
        int index = (int)t.getTag();
        if(type){
            t.setBackgroundResource(R.drawable.round_button_gray);
            selectItemIndex.remove((Integer)index);
        }else{
            t.setBackgroundResource(R.drawable.round_button_blue);
            selectItemIndex.add(index);
        }
    }

    private void makeList(){

        final float scale =getResources().getDisplayMetrics().density;
        int dp5 = (int) (5 * scale + 0.5f);
        int dp3 = (int) (3 * scale * 0.5f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp3, dp3, dp3, dp3);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = (int) (320 * scale + 0.5f);//displayMetrics.widthPixels;// / displayMetrics.density;

        if(list != null){

            LinearLayout mNewLayout = new LinearLayout(this); // Horizontal layout which I am using to add my buttons.
            mNewLayout.setOrientation(LinearLayout.HORIZONTAL);
            int mButtonsSize = 0;
            Rect bounds = new Rect();

            for(int i = 0; i < list.length; i++){

                String mButtonTitle = list[i];
                final TextView mBtn = new TextView(this);
                mBtn.setPadding(dp5*2, dp5, dp5*2, dp5);
                mBtn.setTag(i);
                mBtn.setBackgroundResource(R.drawable.round_button_gray);
                mBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                mBtn.setText(mButtonTitle);
                mBtn.setLayoutParams(params);
                mBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = (int)mBtn.getTag();
                        setText(mBtn, selectItemIndex.contains(index));
                        checkConfident();
                    }
                });
                setText(mBtn, !alreadySetItem.contains(mButtonTitle));

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

        if(selectItemIndex.size() >= 3){
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

        ArrayList<String> in = new ArrayList<>();
        for(int i : selectItemIndex){
            in.add(list[i]);
        }

        Intent intent = new Intent();
        intent.putExtra("interest", in);
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
