package tk.twpooi.tuetue;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.tuetue.util.FacebookLogin;
import tk.twpooi.tuetue.util.NaverLogin;
import tk.twpooi.tuetue.util.ParsePHP;

import static tk.twpooi.tuetue.ShowTuetueActivity.EDIT_CONTENTS;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public final static int RESULT_CODE_TUTOR_LIST_FRAGMENT = 3000;
    public final static int RESULT_CODE_TUTEE_LIST_FRAGMENT = 3001;
    public final static int RESULT_CODE_USER_TUTOR_LIST_FRAGMENT = 3002;
    public final static int RESULT_CODE_USER_TUTEE_LIST_FRAGMENT = 3003;

    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_FINISH = 500;


    private NavigationView navigationView;

    private String[] menuList = {"nav_show_profile", "nav_tutor", "nav_tutee", "nav_my_tutor", "nav_my_tutee", "nav_info", "nav_report", "nav_help", "nav_open_source"};

    // Logout
    private ProgressDialog progressDialog;
    private FacebookLogin facebookLogin;
    private NaverLogin naverLogin;
    private SharedPreferences setting;
    private SharedPreferences.Editor editor;
    private String login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facebookLogin = new FacebookLogin(this);
        setContentView(R.layout.activity_main);

        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();
        login = setting.getString("login", null);

        naverLogin = new NaverLogin(this, new OAuthLoginButton(this));
        progressDialog = new ProgressDialog(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        String nickname = (String)StartActivity.USER_DATA.get("nickname");
        String email = (String)StartActivity.USER_DATA.get("email");
        String img = (String)StartActivity.USER_DATA.get("img");

        View headerView = navigationView.getHeaderView(0);
        ImageView backgroundImg = (ImageView)headerView.findViewById(R.id.img_background);
        Picasso.with(this)
                .load(Information.PROFILE_DEFAULT_IAMGE_URL)
                .into(backgroundImg);
        ImageView profileImg = (ImageView)headerView.findViewById(R.id.profileImg);
        Picasso.with(this)
                .load(img)
                .transform(new CropCircleTransformation())
                .into(profileImg);
        TextView tv_nickname = (TextView)headerView.findViewById(R.id.tv_nickname);
        tv_nickname.setText(nickname);
        TextView tv_email = (TextView)headerView.findViewById(R.id.tv_email);
        tv_email.setText(email);


        showFragment("nav_tutor", new TutorListFragment());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("재능나눔");
        }
        navigationView.setCheckedItem(R.id.nav_tutor);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        String title = null;

        if (id == R.id.nav_show_profile) {

            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("userId", StartActivity.USER_ID);
            startActivity(intent);

        } else if (id == R.id.nav_tutor) {

            showFragment("nav_tutor", new TutorListFragment());
            title = "재능나눔";

        } else if (id == R.id.nav_tutee) {

            showFragment("nav_tutee", new TuteeListFragment());
            title = "재능기부";

        } else if(id == R.id.nav_my_tutor){

            Fragment fragment = new UserTuetueListFragment();
            Bundle bdl = new Bundle(1);
            bdl.putBoolean("type", true);
            fragment.setArguments(bdl);
            showFragment("nav_my_tutor", fragment);
            title = "내글보기(재능나눔)";

        } else if(id == R.id.nav_my_tutee){

            Fragment fragment = new UserTuetueListFragment();
            Bundle bdl = new Bundle(1);
            bdl.putBoolean("type", false);
            fragment.setArguments(bdl);
            showFragment("nav_my_tutee", fragment);
            title = "내글보기(재능기부)";

        } else if(id == R.id.nav_logout){

            if("facebook".equals(login)){
                facebookLogin.logout();
            }else{
                naverLogin.logout();
            }

            editor.remove("login");
            editor.commit();

            redirectStartPage();

        } else if(id == R.id.nav_logout_delete){
            if("facebook".equals(login)){

                facebookLogin.logout();
                editor.remove("login");
                editor.commit();
                removeUser(StartActivity.USER_ID);

            }else if("naver".equals(login)){

                naverLogin.logout();
                editor.remove("login");
                editor.commit();
                removeUser(StartActivity.USER_ID);

            }
        } else if(id == R.id.nav_info){
//            showFragment("nav_info", new Fragment());
            showSnackbar("정보");
        } else if(id == R.id.nav_report){
//            showFragment("nav_report", new Fragment());
            showSnackbar("오류제보");
        } else if(id == R.id.nav_help){
//            showFragment("nav_help", new Fragment());
            showSnackbar("도움말");
        } else if(id == R.id.nav_open_source){
//            showFragment("nav_open_source", new Fragment());
            showSnackbar("Open source");
        }

        if (getSupportActionBar() != null && title != null) {
            getSupportActionBar().setTitle(title);
        }

        navigationView.setCheckedItem(id);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFragment(String tag, Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();

        if(fragmentManager.findFragmentByTag(tag) != null){
            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag(tag)).commit();
        }else{
            fragmentManager.beginTransaction().add(R.id.content_fragment_layout, fragment, tag).commit();
        }
        hideFragment(fragmentManager,tag);

    }

    private void hideFragment(FragmentManager fragmentManager, String name){

        for(String s : menuList){

            if(!s.equals(name)) {
                if (fragmentManager.findFragmentByTag(s) != null) {
                    fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag(s)).commit();
                }
            }

        }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_CODE_TUTOR_LIST_FRAGMENT: {
                int index = data.getIntExtra("index", -1);
                HashMap<String, Object> item = (HashMap<String, Object>) data.getSerializableExtra("item");

                TutorListFragment fragment = (TutorListFragment) getSupportFragmentManager().findFragmentByTag("nav_tutor");
                fragment.updateList(index, item);
                break;
            }
            case RESULT_CODE_TUTEE_LIST_FRAGMENT: {
                int index = data.getIntExtra("index", -1);
                HashMap<String, Object> item = (HashMap<String, Object>) data.getSerializableExtra("item");

                TuteeListFragment fragment = (TuteeListFragment) getSupportFragmentManager().findFragmentByTag("nav_tutee");
                fragment.updateList(index, item);
                break;
            }
            case RESULT_CODE_USER_TUTOR_LIST_FRAGMENT: {
                int index = data.getIntExtra("index", -1);
                HashMap<String, Object> item = (HashMap<String, Object>) data.getSerializableExtra("item");

                UserTuetueListFragment fragment = (UserTuetueListFragment) getSupportFragmentManager().findFragmentByTag("nav_my_tutor");
                fragment.updateList(index, item);
                break;
            }
            case RESULT_CODE_USER_TUTEE_LIST_FRAGMENT: {
                int index = data.getIntExtra("index", -1);
                HashMap<String, Object> item = (HashMap<String, Object>) data.getSerializableExtra("item");

                UserTuetueListFragment fragment = (UserTuetueListFragment) getSupportFragmentManager().findFragmentByTag("nav_my_tutee");
                fragment.updateList(index, item);
                break;
            }
            default:
                break;
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
