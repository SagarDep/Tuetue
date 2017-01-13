package tk.twpooi.tuetue;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.gigamole.navigationtabbar.ntb.NavigationTabBar;
import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.squareup.picasso.Picasso;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class MainActivity extends AppCompatActivity {

    // UI
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private Button profileBtn;
    private CustomViewPager viewPager;
    private NavigationAdapter mPagerAdapter;
    private String[] toolbarTitleList = {"튜터", "튜티"};
    private static final int startPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

    }

    private void init(){

        toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        toolbarTitle.setText("재능나눔");
        profileBtn = (Button)findViewById(R.id.profileBtn);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", StartActivity.USER_ID);
                startActivity(intent);
            }
        });

        viewPager = (CustomViewPager) findViewById(R.id.vp_horizontal_ntb);
        viewPager.setOffscreenPageLimit(toolbarTitleList.length);
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager(), toolbarTitleList.length);
        viewPager.setAdapter(mPagerAdapter);


        final String[] colors = getResources().getStringArray(R.array.default_preview);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        navigationTabBar.setTitleSize(30);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_star_outline_black_48dp),
                        Color.parseColor(colors[0]))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.ic_image_area_black_48dp),
                        Color.parseColor(colors[1]))
                        .build()
        );


        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, startPage);
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                navigationTabBar.getModels().get(position).hideBadge();
                switch (position){
                    case 0: {
                        toolbarTitle.setText("재능나눔");
                        break;
                    }
                    case 1: {
                        toolbarTitle.setText("재능기부");
                        break;
                    }
                    default:
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                //showToolbar();
            }
        });

        navigationTabBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < navigationTabBar.getModels().size(); i++) {
                    final NavigationTabBar.Model model = navigationTabBar.getModels().get(i);
                    navigationTabBar.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            model.showBadge();
                        }
                    }, i * 100);
                }
            }
        }, 500);

    }


    // Tab Adapter Class (used initUI())
    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        private int size;

        public NavigationAdapter(FragmentManager fm, int size){
            super(fm);
            this.size = size;
        }

        @Override
        protected Fragment createItem(int position){
            Fragment f;
            final int pattern = position %size;
            switch (pattern){
                case 0:{
                    f = new TutorListFragment();
                    break;
                }
                case 1:{
                    f = new TuteeListFragment();
                    break;
                }
                default:{
                    f = new Fragment();
                    break;
                }
            }
            return f;
        }

        @Override
        public int getCount(){
            return size;
        }

    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

}
