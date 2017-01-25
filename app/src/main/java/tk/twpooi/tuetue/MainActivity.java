package tk.twpooi.tuetue;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialAccountListener;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.FacebookLogin;
import tk.twpooi.tuetue.util.NaverLogin;
import tk.twpooi.tuetue.util.ParsePHP;

/**
 * Created by neokree on 18/01/15.
 */
public class MainActivity extends MaterialNavigationDrawer {

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_FINISH = 500;

    // UI
    private ImageView profile;
    private TextView tv_nickname;
    private TextView tv_email;
    private ProgressDialog progressDialog;

    // User Account
    private MaterialAccount userAccount;
    private FacebookLogin facebookLogin;
    private NaverLogin naverLogin;
    private List<Target> targets = new ArrayList<>();

    private SharedPreferences setting;
    private SharedPreferences.Editor editor;
    private String login;

    // Section
    private static MaterialSection tutorListSection;
    private static TutorListFragment tutorListFragment;
    private static TuteeListFragment tuteeListFragment;

    @Override
    public void init(Bundle savedInstanceState) {

        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();
        login = setting.getString("login", null);

        facebookLogin = new FacebookLogin(this);
        naverLogin = new NaverLogin(this, new OAuthLoginButton(this));
        progressDialog = new ProgressDialog(this);

        setAccount();
        setSection();

        this.disableLearningPattern();
        this.setDefaultSectionLoaded(1);
        this.setBackPattern(MaterialNavigationDrawer.BACKPATTERN_BACK_TO_FIRST);
    }

    private void setAccount(){

        String nickname = (String)StartActivity.USER_DATA.get("nickname");
        String email = (String)StartActivity.USER_DATA.get("email");
        String img = (String)StartActivity.USER_DATA.get("img");

        // create and set the header
//        View view = LayoutInflater.from(this).inflate(R.layout.custom_menubar_drawer,null);
//        profile = (ImageView)view.findViewById(R.id.img);
//        tv_nickname = (TextView)view.findViewById(R.id.nickname);
//        tv_email = (TextView)view.findViewById(R.id.email);
//        setDrawerHeaderCustom(view);

//        Picasso.with(getApplicationContext())
//                .load(img)
//                .transform(new CropCircleTransformation())
//                .into(profile);
//        tv_nickname.setText(nickname);
//        tv_email.setText(email);

        userAccount = new MaterialAccount(this.getResources(), nickname, email, null, null);
        this.addAccount(userAccount);

        Target target1 = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                userAccount.setPhoto(bitmap);
                targets.remove(this);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        targets.add(target1);
        Picasso.with(this)
                .load(img)
                .transform(new CropCircleTransformation())
                .into(target1);

        Target target2 = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                userAccount.setBackground(bitmap);
                targets.remove(this);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        targets.add(target2);
        Picasso.with(this)
                .load(Information.PROFILE_DEFAULT_IAMGE_URL)
                .into(target2);
        // set listener

//        Intent tutorIntent = new Intent(getApplicationContext(), UserTuetueListFragment.class);
//        tutorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        tutorIntent.putExtra("type", true);
//
//        Intent tuteeIntent = new Intent(getApplicationContext(), UserTuetueListFragment.class);
//        tuteeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        tuteeIntent.putExtra("type", false);

        UserTuetueListFragment tutor = new UserTuetueListFragment();
//        Bundle tutorBdl = new Bundle(1);
//        tutorBdl.putBoolean("type", true);
//        tutor.setArguments(tutorBdl);

        UserTuetueListFragment tutee = new UserTuetueListFragment();
//        Bundle tuteeBdl = new Bundle(1);
//        tuteeBdl.putBoolean("type", false);
//        tutee.setArguments(tuteeBdl);

        // add account sections
        this.addAccountSection(newSection("나의 글 보기(재능나눔)", R.drawable.ic_account_star_grey600_24dp, tutor));
        this.addAccountSection(newSection("나의 글 보기(재능기부)", R.drawable.ic_account_star_variant_grey600_24dp, tutee));
        this.addAccountSection(newSection("프로필 설정",R.drawable.ic_settings_black_24dp,new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                Toast.makeText(getApplicationContext(),"Account settings clicked",Toast.LENGTH_SHORT).show();

                // for default section is selected when you click on it
                section.unSelect(); // so deselect the section if you want
            }
        }));
        this.addAccountSection(newSection("로그아웃", new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {

                if("facebook".equals(login)){
                    facebookLogin.logout();
                }else{
                    naverLogin.logout();
                }

                editor.remove("login");
                editor.commit();

//                AdditionalFunc.restartApp(getApplicationContext());
                redirectStartPage();

            }
        }));

        if("facebook".equals(login)){

            this.addAccountSection(newSection("로그아웃 및 데이터 삭제", new MaterialSectionListener() {
                @Override
                public void onClick(MaterialSection section) {
                    facebookLogin.logout();
                    editor.remove("login");
                    editor.commit();
//                    AdditionalFunc.restartApp(getApplicationContext());
//                    redirectStartPage();
                    removeUser(StartActivity.USER_ID);
                }
            }));

        }else if("naver".equals(login)){

            this.addAccountSection(newSection("계정 및 데이터 삭제", new MaterialSectionListener() {
                @Override
                public void onClick(MaterialSection section) {
                    naverLogin.logout();
                    editor.remove("login");
                    editor.commit();
//                    AdditionalFunc.restartApp(getApplicationContext());
//                    redirectStartPage();
                    removeUser(StartActivity.USER_ID);
                }
            }));

        }

    }
    private void setSection(){

        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("userId", StartActivity.USER_ID);


        tutorListFragment = new TutorListFragment();
        tutorListSection = newSection("재능나눔", R.drawable.ic_account_star_grey600_24dp, tutorListFragment);
        tuteeListFragment = new TuteeListFragment();

        // create sections
        this.addSection(newSection("프로필 보기",R.drawable.ic_account_grey600_24dp, intent));
        this.addSection(tutorListSection);
        this.addSection(newSection("재능기부", R.drawable.ic_account_star_variant_grey600_24dp, tuteeListFragment));
        this.addSection(newSection("나의 글 보기(재능나눔)", R.drawable.ic_account_star_grey600_24dp, new UserTuetueListFragment()));
        this.addSection(newSection("정보", new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                showSnackbar("정보");
            }
        }));
        this.addSection(newSection("오류제보", new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                showSnackbar("오류제보");
            }
        }));
        this.addSection(newSection("도움말", new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                showSnackbar("도움말");
            }
        }));
        this.addSection(newSection("Open source", new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                showSnackbar("Open source");
            }
        }));

//        this.addSection(newSection("Section",R.drawable.ic_hotel_grey600_24dp,new FragmentButton()).setSectionColor(Color.parseColor("#03a9f4")));

        // create bottom section
        this.addBottomSection(newSection("설정", R.drawable.ic_settings_black_24dp, new MaterialSectionListener() {
            @Override
            public void onClick(MaterialSection section) {
                showSnackbar("설정");
            }
        }));

    }

    private void removeUser(String userId){
        HashMap<String, String> map = new HashMap<>();
        map.put("service", "deleteUser");
        map.put("id", userId);

        progressDialog.setMessage("데이터 삭제 중입니다.");
        progressDialog.show();
        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map){
            @Override
            protected void afterThreadFinish(String data) {
                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_FINISH));
            }
        }.start();
    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_FINISH:
                    redirectStartPage();
                    break;
                default:
                    break;
            }
        }
    }

    private void redirectStartPage(){
        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
