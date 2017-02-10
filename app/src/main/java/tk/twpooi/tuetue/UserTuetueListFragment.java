package tk.twpooi.tuetue;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import in.srain.cube.views.ptr.util.PtrLocalDisplay;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.OnAdapterSupport;
import tk.twpooi.tuetue.util.ParsePHP;

/**
 * Created by tw on 2016-08-16.
 */
public class UserTuetueListFragment extends Fragment implements OnAdapterSupport {


    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_PROGRESS_HIDE = 502;

    private MaterialDialog progressDialog;
    private AVLoadingIndicatorView loading;

    // UI
    private View view;
    private Context context;
    private FloatingActionsMenu menu;
    private FloatingActionButton addTutor;
    private FloatingActionButton orderCategory;
    private FloatingActionButton searchBtn;
    protected PtrFrameLayout mPtrFrameLayout;


    private boolean type; // true : tutor, false : tutee
    private ArrayList<HashMap<String, Object>> list;
    private String search;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView.Adapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        type = getArguments().getBoolean("type", true);
//        type = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_tuetue_list, container, false);
        context = container.getContext();

        initUI();

        return view;

    }


    public void initUI(){

        floationMenu();

        mPtrFrameLayout = (PtrFrameLayout) view.findViewById(R.id.material_style_ptr_frame);

        // header
        final MaterialHeader header = new MaterialHeader(getContext());
        int[] colors = getResources().getIntArray(R.array.default_preview);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, PtrLocalDisplay.dp2px(15), 0, 0);
        header.setPtrFrameLayout(mPtrFrameLayout);

        mPtrFrameLayout.setLoadingMinTime(10);
        mPtrFrameLayout.setDurationToCloseHeader(1500);
        mPtrFrameLayout.setHeaderView(header);
        mPtrFrameLayout.addPtrUIHandler(header);

        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, rv, header);
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                progressDialog.show();
                getTuetueList();
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);

        list = new ArrayList<>();

        progressDialog = new MaterialDialog.Builder(context)
                .content("잠시만 기다려주세요.")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .theme(Theme.LIGHT)
                .build();
//        progressDialog = new ProgressDialog(context);
//        progressDialog.setMessage("조회 중입니다.");
        loading = (AVLoadingIndicatorView)view.findViewById(R.id.loading);

//        loading.show();
        getTuetueList();

    }

    private void getTuetueList() {
        loading.show();
        HashMap<String, String> map = new HashMap<>();
        if(type) {
            map.put("service", "getUserTutorList");
        }else{
            map.put("service", "getUserTuteeList");
        }
        map.put("id", StartActivity.USER_ID);
        if (search != null && (!"".equals(search))) {
            map.put("search", search);
        }

        new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

            @Override
            protected void afterThreadFinish(String data) {

                list.clear();

                if(type) {
                    list = AdditionalFunc.getTutorList(data);
                }else{
                    list = AdditionalFunc.getTuteeList(data);
                }

                handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));

            }
        }.start();
    }

    public void floationMenu(){

        menu = (FloatingActionsMenu)view.findViewById(R.id.multiple_actions);

        FloatingActionButton gotoUp = (FloatingActionButton)view.findViewById(R.id.gotoUp);
        gotoUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rv != null){
                    rv.smoothScrollToPosition(0);
                }
                menu.toggle();
            }
        });
        gotoUp.setTitle("맨위로");

        addTutor = (FloatingActionButton)view.findViewById(R.id.addTutor);
        addTutor.setVisibility(View.GONE);
        orderCategory = (FloatingActionButton)view.findViewById(R.id.order_category);
        orderCategory.setVisibility(View.GONE);

        searchBtn = (FloatingActionButton) view.findViewById(R.id.search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(context)
                        .title("검색")
                        .inputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                                InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                        .theme(Theme.LIGHT)
                        .positiveText("검색")
                        .negativeText("취소")
                        .neutralText("초기화")
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                search = "";
                                progressDialog.show();
                                getTuetueList();
                            }
                        })
                        .input("검색어를 입력해주세요", search, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                search = input.toString();
                                progressDialog.show();
                                getTuetueList();
                            }
                        })
                        .show();
                menu.toggle();
            }
        });
        searchBtn.setTitle("검색");

   }

    public void makeList(){

        if(type) {
            adapter = new TutorListCustomAdapter(context, list, rv, this, ShowTuetueActivity.TYPE_USER_TUTOR_LIST);
        }else{
            adapter = new TuteeListCustomAdapter(context, list, rv, this, ShowTuetueActivity.TYPE_USER_TUTEE_LIST);
        }

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        mPtrFrameLayout.refreshComplete();

    }

    @Override
    public void showView() {
        menu.setVisibility(View.VISIBLE);
        setFadeInAnimation(menu);
    }

    @Override
    public void hideView() {
        menu.setVisibility(View.INVISIBLE);
        setFadeOutAnimation(menu);
    }

    private void setFadeInAnimation(View view) {
        Animation animation = new AlphaAnimation(0, 1);
        animation.setDuration(500);
        view.setAnimation(animation);
    }

    private void setFadeOutAnimation(View view) {
        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        view.setAnimation(animation);
    }

    @Override
    public void redirectActivityForResult(Intent intent) {
        getActivity().startActivityForResult(intent, 0);
    }

    @Override
    public void redirectActivity(Intent intent) {
        getActivity().startActivity(intent);
    }

    public void updateList(int index, HashMap<String, Object> data) {
        if (index >= 0 && index < list.size()) {
            list.set(index, data);
            adapter.notifyItemChanged(index);
        }
    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_MAKE_LIST:
                    progressDialog.hide();
                    loading.hide();
                    makeList();
                    break;
                case MSG_MESSAGE_PROGRESS_HIDE:
                    progressDialog.hide();
                    break;
                default:
                    break;
            }
        }
    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_color));
        snackbar.show();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
