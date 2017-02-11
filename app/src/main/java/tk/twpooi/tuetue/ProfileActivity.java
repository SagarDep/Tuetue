package tk.twpooi.tuetue;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.flyco.animation.FadeEnter.FadeEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalListDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.AlphaForegroundColorSpan;
import tk.twpooi.tuetue.util.ParsePHP;

public class ProfileActivity extends Activity {

    public final static int EDIT_PROFILE = 100;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_UPDATE_TUTOR_ITEM = 500;
    private final int MSG_MESSAGE_UPDATE_TUTEE_ITEM = 502;
    private final int MSG_MESSAGE_PROGRESS_FINISH = 504;
    private final int MSG_MESSAGE_COMPLIMENT_FINISH = 505;
    private final int MSG_MESSAGE_COMPLIMENT_ERROR = 506;

    // User Data
    private HashMap<String, Object> item;
    private String userId;
    private ArrayList<HashMap<String, Object>> tutorList;
    private ArrayList<HashMap<String, Object>> tuteeList;

    // Basic UI
    private FrameLayout root;
    private int mActionBarTitleColor;
    private int mActionBarHeight;
    private int mHeaderHeight;
    private int mMinHeaderTranslation;
    private FloatingActionButton fabEdit;
    private ImageView mHeaderPicture;
    private ImageView mHeaderLogo;
    private View mHeader;
    private View mPlaceHolderView;
    private AccelerateDecelerateInterpolator mSmoothInterpolator;
    // Compliment
    private LinearLayout complimentBtn;
    private ImageView complimentImg;
    private TextView tv_compliment;

    private MaterialDialog progressDialog;

    // Profile Image Move
    private RectF mRect1 = new RectF();
    private RectF mRect2 = new RectF();
    private AlphaForegroundColorSpan mAlphaForegroundColorSpan;
    private SpannableString mSpannableString;
    private TypedValue mTypedValue = new TypedValue();

    // RecycleView
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private ProfileCustomAdapter adapter;

    private ArrayList<HashMap<String, String>> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        setStatusColor();

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        item = new HashMap<>();
        tutorList = new ArrayList<>();
        tuteeList = new ArrayList<>();


        setBasicUI();
        makeData();
        makeList();


        getUserInfo();
        getUserTutorList();
        getUserTuteeList();

    }

    private void setBasicUI(){

        root = (FrameLayout)findViewById(R.id.root);
        // Compliment
        complimentBtn = (LinearLayout) findViewById(R.id.compliment_btn);
        complimentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean tag = (boolean) complimentBtn.getTag();
                updateCompliment(!tag);
            }
        });
        complimentImg = (ImageView) findViewById(R.id.compliment_img);
        tv_compliment = (TextView) findViewById(R.id.tv_compliment);
        setComplimentBtn(FileManager.readComplimentListFile(getApplicationContext()).contains(userId));

        mSmoothInterpolator = new AccelerateDecelerateInterpolator();
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        mMinHeaderTranslation = -mHeaderHeight + getActionBarHeight();

        mHeader = findViewById(R.id.header);
        mHeaderPicture = (ImageView) findViewById(R.id.header_picture);
//        Picasso.with(getApplicationContext())
//                .load(Information.PROFILE_DEFAULT_IAMGE_URL)
//                .memoryPolicy(MemoryPolicy.NO_CACHE)
//                .into(mHeaderPicture);
        mHeaderLogo = (ImageView) findViewById(R.id.header_logo);

        mActionBarTitleColor = ContextCompat.getColor(getApplicationContext(), R.color.dark_gray);

        mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(mActionBarTitleColor);

        mSpannableString = new SpannableString("Profile");

        fabEdit = (FloatingActionButton) findViewById(R.id.fab_edit);
        fabEdit.setTitle("편집");
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WtInfoActivity.class);
                intent.putExtra("edit", true);
                intent.putExtra("id", StartActivity.USER_ID);
                startActivityForResult(intent, 0);
            }
        });
        if (StartActivity.USER_ID.equals(userId)) {
            fabEdit.setVisibility(View.VISIBLE);
        } else {
            fabEdit.setVisibility(View.GONE);
        }

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

        setupActionBar();
        setupListView();

    }

    private void setComplimentBtn(boolean b) {
        complimentBtn.setTag(b);
        if (b) {
            complimentBtn.setBackgroundResource(R.drawable.compliment_background_select);
            complimentImg.setImageResource(R.drawable.thumb_up_select);
            tv_compliment.setText("취소");
            tv_compliment.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.thump_up_color));
        } else {
            complimentBtn.setBackgroundResource(R.drawable.compliment_background);
            complimentImg.setImageResource(R.drawable.thumb_up);
            tv_compliment.setText("칭찬하기");
            tv_compliment.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        }
    }

    private void updateCompliment(boolean b) { // true면 증가, false면 감소

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("service", "updateItem");
        map.put("id", userId);
        map.put("table", "user");
        map.put("type", "compliment");
        if (b)
            map.put("mode", "1");
        else
            map.put("mode", "0");

        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {
            @Override
            protected void afterThreadFinish(String data) {
                if ("1".equals(data)) {
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_COMPLIMENT_FINISH));
                } else {
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_COMPLIMENT_ERROR));
                }
            }
        }.start();

    }

    public void makeList(){

        adapter = new ProfileCustomAdapter(getApplicationContext(), list, rv, getWindow().getDecorView().getRootView(), mPlaceHolderView, this);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        adapter.notifyItemChanged(0);

    }

    private ArrayList<HashMap<String, String>> makeData(){

        list = new ArrayList<>();

        try {
            String nickname = (String) item.get("nickname");
            String email = (String) item.get("email");
            String contact = (String) item.get("contact");
            String intro = (String) item.get("intro");
            String interest = "";


            HashMap<String, String> temp = new HashMap<>();
            temp.put("title", "닉네임");
            temp.put("content", nickname);
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "이메일");
            temp.put("content", email);
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "연락수단");
            temp.put("content", contact);
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "관심분야");
            temp.put("content", interest);
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "나눔한 재능");
            temp.put("content", "");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "나눔 받은 재능");
            temp.put("content", "");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "소개");
            temp.put("content", intro);
            list.add(temp);
        }catch (Exception e){
            e.printStackTrace();
        }

        return list;

    }

    private void setAdditionUI(){
        String img = (String) item.get("img");
        Picasso.with(getApplicationContext())
                .load(img)
                .transform(new CropCircleTransformation())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(mHeaderLogo);
        Picasso.with(getApplicationContext())
                .load((String) item.get("background"))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(mHeaderPicture);
        mSpannableString = new SpannableString((String)item.get("nickname"));
    }

    private void setTutorUI() {
        for (int i = 0; i < list.size(); i++) {
            HashMap<String, String> map = list.get(i);
            if ("나눔한 재능".equals((String) map.get("title"))) {
                map.put("content", tutorList.size() + "개");
                adapter.updateList(i, tutorList.size() + "개");
            }
        }
    }

    private void setTuteeUI() {
        for (int i = 0; i < list.size(); i++) {
            HashMap<String, String> map = list.get(i);
            if ("나눔 받은 재능".equals((String) map.get("title"))) {
                map.put("content", tuteeList.size() + "개");
                adapter.updateList(i, tuteeList.size() + "개");
            }
        }
    }

    private void setMainUI() {
        setAdditionUI();
        for (int i = 0; i < list.size(); i++) {
            HashMap<String, String> map = list.get(i);
            switch (map.get("title")) {
                case "닉네임":
                    map.put("content", (String) item.get("nickname"));
                    adapter.updateList(i, (String) item.get("nickname"));
                    break;
                case "이메일":
                    map.put("content", (String) item.get("email"));
                    adapter.updateList(i, (String) item.get("email"));
                    break;
                case "연락수단":
                    map.put("content", (String) item.get("contact"));
                    adapter.updateList(i, (String) item.get("contact"));
                    break;
                case "관심분야":
                    String interest = "";
                    ArrayList<String> inter = (ArrayList<String>) item.get("interest");
                    if (inter != null) {
                        for (int j = 0; j < inter.size(); j++) {
                            interest += inter.get(j);
                            if (j + 1 < inter.size()) {
                                interest += ",";
                            }
                        }
                    }
                    map.put("content", interest);
                    adapter.updateList(i, interest);
                    break;
                case "소개":
                    map.put("content", (String) item.get("intro"));
                    adapter.updateList(i, (String) item.get("intro"));
                    break;
            }
        }
    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_UPDATE_TUTOR_ITEM:
                    setTutorUI();
                    break;
                case MSG_MESSAGE_UPDATE_TUTEE_ITEM:
                    setTuteeUI();
                    break;
                case MSG_MESSAGE_PROGRESS_FINISH:
                    setMainUI();
                    break;
                case MSG_MESSAGE_COMPLIMENT_FINISH:
                    progressDialog.hide();
                    boolean tag = (boolean) complimentBtn.getTag();
                    setComplimentBtn(!tag);
                    FileManager.addComplimentList(getApplicationContext(), userId);
                    break;
                case MSG_MESSAGE_COMPLIMENT_ERROR:
                    progressDialog.hide();
                    final com.flyco.dialog.widget.MaterialDialog dialog = new com.flyco.dialog.widget.MaterialDialog(ProfileActivity.this);
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

    private void getUserInfo(){
        HashMap<String, String> map = new HashMap<>();
        map.put("service", "getUserInfo");
        map.put("id", userId);

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                item.clear();
                item = AdditionalFunc.getUserInfo(data);
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_PROGRESS_FINISH));

            }
        }.start();
    }

    private void getUserTutorList(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", userId);
        map.put("service", "getUserTutorList");

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                tutorList.clear();
                tutorList = AdditionalFunc.getTutorList(data);
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_UPDATE_TUTOR_ITEM));
            }
        }.start();

    }

    private void getUserTuteeList(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("id", userId);
        map.put("service", "getUserTuteeList");

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                tuteeList.clear();
                tuteeList = AdditionalFunc.getTuteeList(data);
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_UPDATE_TUTEE_ITEM));
            }
        }.start();

    }

    public void showListDialog(String title, String[] list) {
        NormalListDialog dialog = new NormalListDialog(this, list);
        dialog.title(title)
                .titleTextSize_SP(14.5f)
                .show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("resultCode : " + resultCode);
        switch (resultCode) {
            case EDIT_PROFILE:
                item = (HashMap<String, Object>) data.getSerializableExtra("item");
                getUserTutorList();
                getUserTuteeList();
                setMainUI();
                makeList();
                break;
            default:
                break;
        }

    }

    public void showView() {
        fabEdit.setVisibility(View.VISIBLE);
    }

    public void hideView() {
        fabEdit.setVisibility(View.GONE);
    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    public int getActionBarHeight() {
        if (mActionBarHeight != 0) {
            return mActionBarHeight;
        }
        getTheme().resolveAttribute(android.R.attr.actionBarSize, mTypedValue, true);
        mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, getResources().getDisplayMetrics());
        return mActionBarHeight;
    }

    private void setupListView() {

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);

        mPlaceHolderView = getLayoutInflater().inflate(R.layout.view_header_placeholder, rv, false);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int scrollY = rv.computeVerticalScrollOffset();
                mHeader.setTranslationY(Math.max(-scrollY, mMinHeaderTranslation));
                float ratio = clamp(mHeader.getTranslationY() / mMinHeaderTranslation, 0.0f, 1.0f);
                interpolate(mHeaderLogo, getActionBarIconView(), mSmoothInterpolator.getInterpolation(ratio));
                setTitleAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
            }
        });

    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min,Math.min(value, max));
    }

    private void interpolate(View view1, View view2, float interpolation) {
        getOnScreenRect(mRect1, view1);
        getOnScreenRect(mRect2, view2);

        float scaleX = 1.0F + interpolation * (mRect2.width() / mRect1.width() - 1.0F);
        float scaleY = 1.0F + interpolation * (mRect2.height() / mRect1.height() - 1.0F);
        float translationX = 0.5F * (interpolation * (mRect2.left + mRect2.right - mRect1.left - mRect1.right));
        float translationY = 0.5F * (interpolation * (mRect2.top + mRect2.bottom - mRect1.top - mRect1.bottom));

        if(Float.isNaN(scaleX))
            scaleX = 1.0f;
        if(Float.isNaN(scaleY))
            scaleY = 1.0f;

        view1.setTranslationX(translationX);
        view1.setTranslationY(translationY - mHeader.getTranslationY());
        view1.setScaleX(scaleX);
        view1.setScaleY(scaleY);
    }

    private RectF getOnScreenRect(RectF rect, View view) {
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        return rect;
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(R.drawable.ic_transparent);
        actionBar.setDisplayHomeAsUpEnabled(false);
        //getActionBarTitleView().setAlpha(0f);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private ImageView getActionBarIconView() {
        return (ImageView) findViewById(android.R.id.home);
    }

    private void setTitleAlpha(float alpha) {
        mAlphaForegroundColorSpan.setAlpha(alpha);
        mSpannableString.setSpan(mAlphaForegroundColorSpan, 0, mSpannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActionBar().setTitle(mSpannableString);
    }

    private void setStatusColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
