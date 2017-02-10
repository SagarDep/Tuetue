package tk.twpooi.tuetue;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.tuetue.image.ShowImageActivity;
import tk.twpooi.tuetue.image.UploadImageWithThread;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.ParsePHP;

public class WtInfoActivity extends AppCompatActivity {

    private static final int EDIT_PROFILE = 3000;
    private static final int EDIT_BACKGROUND = 30001;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_FINISH = 500;
    private final int MSG_MESSAGE_UPDATE_PROFILE = 501;
    private final int MSG_MESSAGE_UPDATE_PROFILE_FINISH = 502;
    private final int MSG_MESSAGE_SAVE_USER_PROFILE = 503;

    // UI
    private ImageView backgroundImage;
    private CircleImageView profileImage;
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
    private String default_img;
    private String background;
    private String name;
    private String email;
    private ArrayList<String> interest;

    private MaterialDialog progressDialog;

    private HashMap<String, Object> item;
    private boolean isEditMode;

    private boolean isProfileImageChange;
    private boolean isBackgroundImageChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wtinfo);

        initData();

        init();

        if (isEditMode) {
            progressDialog.setContent("잠시만 기다려주세요.");
            progressDialog.show();

            HashMap<String, String> map = new HashMap<>();
            map.put("service", "getUserInfo");
            map.put("id", id);

            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {
                @Override
                protected void afterThreadFinish(String data) {
                    item.clear();
                    item = AdditionalFunc.getUserInfo(data);
                    handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_UPDATE_PROFILE));
                }
            }.start();
        }

    }

    private void initData(){

        Intent intent = getIntent();
        isEditMode = intent.getBooleanExtra("edit", false);
        id = intent.getStringExtra("id");

        if (!isEditMode) {
            img = intent.getStringExtra("img");
            default_img = img;
            background = Information.PROFILE_DEFAULT_IAMGE_URL;
            name = intent.getStringExtra("name");
            email = intent.getStringExtra("email");
        }

        interest = new ArrayList<>();
        item = new HashMap<>();

    }

    private void init(){

        backgroundImage = (ImageView) findViewById(R.id.backgroundImg);
        backgroundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                redirectShowImageActivity(AdditionalFunc.getBitmapFromImageView(backgroundImage), EDIT_BACKGROUND, false, false, null);
            }
        });
        profileImage = (CircleImageView) findViewById(R.id.profileImg);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectShowImageActivity(AdditionalFunc.getBitmapFromImageView(profileImage), EDIT_PROFILE, true, true, (String) item.get("default_img"));
            }
        });
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
                progressDialog.show();
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_SAVE_USER_PROFILE));
            }
        });
        if (isEditMode) {
            nextBtn.setText("편집하기");
        }

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

        if (!isEditMode) {
            Picasso.with(getApplicationContext())
                    .load(background)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(backgroundImage);
            Picasso.with(getApplicationContext())
                    .load(img)
                    .transform(new CropCircleTransformation())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .into(profileImage);
            profileName.setText(name + "님");

            editEmail.setText(email);
            editContact.setText(email);
        }

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

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

        if (isProfileImageChange) {
            UploadImageWithThread profileUpload = new UploadImageWithThread(Information.USER_IMAGE_SAVE_ADDRESS, AdditionalFunc.getBitmapFromImageView(profileImage), id, "profile");
            profileUpload.start();
            try {
                profileUpload.join();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            img = Information.USER_PROFILE_URL + id + ".jpg";
        }
        if (isBackgroundImageChange) {
            UploadImageWithThread backgroundUpload = new UploadImageWithThread(Information.USER_IMAGE_SAVE_ADDRESS, AdditionalFunc.getBitmapFromImageView(backgroundImage), id, "background");
            backgroundUpload.start();
            try {
                backgroundUpload.join();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            background = Information.USER_BACKGROUND_URL + id + ".jpg";
        }


        email = editEmail.getText().toString();
        String nickname = AdditionalFunc.replaceNewLineString(editNickName.getText().toString());
        String intro = AdditionalFunc.replaceNewLineString(editIntro.getText().toString());
        String contact = AdditionalFunc.replaceNewLineString(editContact.getText().toString());
        String inter = AdditionalFunc.arrayListToString(interest);

        HashMap<String, String> map = new HashMap<>();
        if (isEditMode) {
            map.put("service", "updateUser");
        } else {
            map.put("service", "saveUser");
        }
        map.put("id", id);
        map.put("img", img);
        map.put("default_img", default_img);
        map.put("background", background);
        map.put("name", name);
        map.put("email", email);
        map.put("nickname", nickname);
        map.put("intro", intro);
        map.put("contact", contact);
        map.put("interest", inter);

//        progressDialog.show();
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
                            if (isEditMode) {
                                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_UPDATE_PROFILE_FINISH));
                            } else {
                                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_FINISH));
                            }
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
                case MSG_MESSAGE_UPDATE_PROFILE:
                    progressDialog.hide();
                    fillField();
                    break;
                case MSG_MESSAGE_UPDATE_PROFILE_FINISH:
                    progressDialog.hide();
                    redirectPreviousActivity();
                    break;
                case MSG_MESSAGE_SAVE_USER_PROFILE:
                    saveUserInformation();
                    break;
                default:
                    break;
            }
        }
    }

    private void fillField() {

        img = (String) item.get("img");
        default_img = (String) item.get("default_img");
        background = (String) item.get("background");
        name = (String) item.get("name");
        String nickname = (String) item.get("nickname");
        String contact = (String) item.get("contact");
        String intro = (String) item.get("intro");
        String email = (String) item.get("email");
        this.interest = (ArrayList<String>) item.get("interest");


        Picasso.with(getApplicationContext())
                .load(background)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(backgroundImage);
        Picasso.with(getApplicationContext())
                .load(img)
                .transform(new CropCircleTransformation())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(profileImage);
        profileName.setText(name + "님");

        editNickName.setText(nickname);
        editContact.setText(contact);
        editIntro.setText(intro);
        editEmail.setText(email);

        setInterestField();

        checkEditText();

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
            case EDIT_PROFILE: {
                Uri uri = data.getParcelableExtra("uri");
                if (uri != null) {
                    Picasso.with(getApplicationContext())
                            .load(uri)
                            .transform(new CropCircleTransformation())
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(profileImage);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data.getByteArrayExtra("bitmap"), 0, data.getByteArrayExtra("bitmap").length);
                    profileImage.setImageBitmap(bitmap);
                }
                isProfileImageChange = true;
                break;
            }
            case EDIT_BACKGROUND: {
                Uri uri = data.getParcelableExtra("uri");
                if (uri != null) {
                    Picasso.with(getApplicationContext())
                            .load(uri)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(backgroundImage);
                } else {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data.getByteArrayExtra("bitmap"), 0, data.getByteArrayExtra("bitmap").length);
                    backgroundImage.setImageBitmap(bitmap);
                }
                isBackgroundImageChange = true;
                break;
            }
            default:
                break;
        }
    }

    public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = scaleBitmapImage.getWidth();
        int targetHeight = scaleBitmapImage.getHeight();
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }

    private void redirectShowImageActivity(Bitmap bitmap, int code, boolean isNeedCrop, boolean isProfile, String defaultProfileImageUrl) {

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bs);

        Intent intent = new Intent(this, ShowImageActivity.class);
        intent.putExtra("code", code);
        intent.putExtra("bitmap", bs.toByteArray());
        intent.putExtra("edit", true);
        intent.putExtra("crop", isNeedCrop);
        intent.putExtra("isProfile", isProfile);
        if (defaultProfileImageUrl != null) {
            intent.putExtra("defaultProfile", defaultProfileImageUrl);
        }
        startActivityForResult(intent, 0);
    }

    private void redirectSelectActivity() {
        Intent intent = new Intent(this, SelectInterestActivity.class);
        intent.putStringArrayListExtra("interest", interest);
        startActivityForResult(intent, 0);
    }

    private void redirectMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectPreviousActivity() {
        Intent intent = new Intent();
        intent.putExtra("item", StartActivity.USER_DATA);
        setResult(ProfileActivity.EDIT_PROFILE, intent);
        finish();
    }

    private void showSnackbar(String msg) {
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
        AdditionalFunc.clearApplicationCache(getApplicationContext(), null);
    }

}
