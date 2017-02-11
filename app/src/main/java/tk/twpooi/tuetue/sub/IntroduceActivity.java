package tk.twpooi.tuetue.sub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.Theme;
import com.flyco.animation.FadeEnter.FadeEnter;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.MaterialDialog;
import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.tuetue.Information;
import tk.twpooi.tuetue.ProfileActivity;
import tk.twpooi.tuetue.R;
import tk.twpooi.tuetue.StartActivity;
import tk.twpooi.tuetue.TuteeListFragment;
import tk.twpooi.tuetue.TutorListFragment;
import tk.twpooi.tuetue.UserTuetueListFragment;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.CustomViewPager;
import tk.twpooi.tuetue.util.FacebookLogin;
import tk.twpooi.tuetue.util.NaverLogin;
import tk.twpooi.tuetue.util.ParsePHP;


public class IntroduceActivity extends AppCompatActivity {

    private ImageView closeBtn;
    private DotIndicator dotIndicator;
    private CustomViewPager viewPager;
    private NavigationAdapter mPagerAdapter;

    private int code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduce);

        code = getIntent().getIntExtra("code", -1);

        initUI();

    }

    private void initUI() {

        closeBtn = (ImageView) findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        dotIndicator = (DotIndicator) findViewById(R.id.main_indicator_ad);
        dotIndicator.setSelectedDotColor(Color.parseColor("#FF4081"));
        dotIndicator.setUnselectedDotColor(Color.parseColor("#CFCFCF"));
        dotIndicator.setNumberOfItems(Information.INTRODUCE_IMAGE.length);
        dotIndicator.setSelectedItem(0, false);
        viewPager = (CustomViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(Information.INTRODUCE_IMAGE.length);
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager(), Information.INTRODUCE_IMAGE);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                dotIndicator.setSelectedItem(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private static class NavigationAdapter extends FragmentPagerAdapter {

        private int size;
        private String[] list;

        public NavigationAdapter(FragmentManager fm, String[] list) {
            super(fm);
            this.list = list;
            this.size = list.length;
        }

        @Override
        public Fragment getItem(int position) {

            Fragment f;
            final int pattern = position % size;

            f = new IntroduceFragment();
            Bundle bdl = new Bundle(1);
            bdl.putSerializable("url", list[pattern]);
            f.setArguments(bdl);

            return f;
        }

        @Override
        public int getCount() {
            return size;
        }
    }

    @Override
    public void onBackPressed() {
        if (code >= 0) {
            setResult(code);
        }
        finish();
//        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
