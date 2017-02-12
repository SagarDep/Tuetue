package tk.twpooi.tuetue.sub;

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
import tk.twpooi.tuetue.FileManager;
import tk.twpooi.tuetue.Information;
import tk.twpooi.tuetue.ProfileCustomAdapter;
import tk.twpooi.tuetue.R;
import tk.twpooi.tuetue.ShowTuetueActivity;
import tk.twpooi.tuetue.StartActivity;
import tk.twpooi.tuetue.TuteeListCustomAdapter;
import tk.twpooi.tuetue.TutorListCustomAdapter;
import tk.twpooi.tuetue.WtInfoActivity;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.AlphaForegroundColorSpan;
import tk.twpooi.tuetue.util.OnAdapterSupport;
import tk.twpooi.tuetue.util.ParsePHP;

public class TuetueListActivity extends Activity implements OnAdapterSupport {

    private MyHandler handler = new MyHandler();

    // User Data
    private ArrayList<HashMap<String, Object>> list;

    // Basic UI
    private FrameLayout root;
    private MaterialDialog progressDialog;

    // RecycleView
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView.Adapter adapter;

    private int type;
    public final static int TYPE_TUTOR_LIST = 0;
    public final static int TYPE_TUTEE_LIST = 1;
    public final static int TYPE_USER_TUTOR_LIST = 2;
    public final static int TYPE_USER_TUTEE_LIST = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuetue_list);

        Intent intent = getIntent();
        type = intent.getIntExtra("type", -1);
        list = (ArrayList<HashMap<String, Object>>) intent.getSerializableExtra("list");

        setBasicUI();

        makeList();

    }

    private void setBasicUI() {

        progressDialog = new MaterialDialog.Builder(this)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();

        root = (FrameLayout) findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);

    }

    public void makeList() {

        if (isTutorContents()) {
            adapter = new TutorListCustomAdapter(getApplicationContext(), list, rv, this, TYPE_TUTOR_LIST);
        } else {
            adapter = new TuteeListCustomAdapter(getApplicationContext(), list, rv, this, TYPE_TUTEE_LIST);
        }

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        adapter.notifyItemChanged(0);

    }


    private boolean isTutorContents() {
        if (type == TYPE_TUTOR_LIST || type == TYPE_USER_TUTOR_LIST) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void showView() {

    }

    @Override
    public void hideView() {

    }

    @Override
    public void redirectActivityForResult(Intent intent) {
        startActivityForResult(intent, 0);
    }

    @Override
    public void redirectActivity(Intent intent) {
        startActivity(intent);
    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                default:
                    break;
            }
        }
    }

    private void updateList(int index, HashMap<String, Object> data) {
        if (index >= 0 && index < list.size()) {
            list.set(index, data);
            adapter.notifyItemChanged(index);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case TYPE_TUTOR_LIST: {
                int index = data.getIntExtra("index", -1);
                HashMap<String, Object> item = (HashMap<String, Object>) data.getSerializableExtra("item");
                updateList(index, item);
                break;
            }
            case TYPE_TUTEE_LIST: {
                int index = data.getIntExtra("index", -1);
                HashMap<String, Object> item = (HashMap<String, Object>) data.getSerializableExtra("item");
                updateList(index, item);
                break;
            }
            default:
                break;
        }

    }

    public void showSnackbar(String msg) {
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
