package tk.twpooi.tuetue;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;
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
import tk.twpooi.tuetue.util.OnLoadMoreListener;
import tk.twpooi.tuetue.util.OnAdapterSupport;
import tk.twpooi.tuetue.util.ParsePHP;

/**
 * Created by tw on 2016-08-16.
 */
public class TuteeListFragment extends Fragment implements OnAdapterSupport {


    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_MAKE_LIST = 500;
    private final int MSG_MESSAGE_MAKE_ENDLESS_LIST = 501;
    private final int MSG_MESSAGE_PROGRESS_HIDE = 502;

    private ProgressDialog progressDialog;
    private AVLoadingIndicatorView loading;

    int page = 0;
    private ArrayList<HashMap<String, Object>> tempList;
    private ArrayList<HashMap<String, Object>> list;
    private String category;

    // UI
    private View view;
    private Context context;
    private FloatingActionsMenu menu;
    private FloatingActionButton addTutor;
    private FloatingActionButton orderCategory;


    protected PtrFrameLayout mPtrFrameLayout;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private TuteeListCustomAdapter adapter;
    private boolean isLoadFinish;

    private static boolean isTuteeUpdate;
    private static String updateId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_tuetue_list, container, false);
        context = container.getContext();

        initUI();

        return view;

    }


    public void initUI(){

        list = new ArrayList<>();
        tempList = new ArrayList<>();

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
                initLoadValue();
                getTuteeList();
            }
        });

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("조회 중입니다.");
        loading = (AVLoadingIndicatorView)view.findViewById(R.id.loading);

        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);


//        loading.show();
        getTuteeList();

    }

    private void getTuteeList(){

        if(!isLoadFinish) {
            loading.show();
            HashMap<String, String> map = new HashMap<>();
            map.put("service", "getTuteeList");
            map.put("page", Integer.toString(page));
            if(category != null && (!"".equals(category))){
                map.put("category", category);
            }

            new ParsePHP(Information.MAIN_SERVER_ADDRESS, map) {

                @Override
                protected void afterThreadFinish(String data) {

                    if(page <= 0) {
                        list.clear();
                        list = AdditionalFunc.getTuteeList(data);
                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_LIST));
                    }else{

                        tempList.clear();
                        tempList = AdditionalFunc.getTuteeList(data);
                        if (tempList.size() < StartActivity.LIST_SIZE) {
                            isLoadFinish = true;
                        }
                        handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_MAKE_ENDLESS_LIST));

                    }

                }
            }.start();
        }else{
            if(adapter != null){
                adapter.setLoaded();
            }
        }

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
        addTutor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddTuteeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                menu.toggle();
            }
        });
        addTutor.setTitle("재능기부 모집");

        orderCategory = (FloatingActionButton)view.findViewById(R.id.order_category);
        orderCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(orderCategory.getTitle().equals("카테고리로 조회")){
                    selectCategory();
                    orderCategory.setTitle("전체조회");
                }else{
                    initLoadValue();
                    category = null;
                    progressDialog.show();
                    getTuteeList();
                    orderCategory.setTitle("카테고리로 조회");
                }
                menu.toggle();
            }
        });
        orderCategory.setTitle("카테고리로 조회");

   }

    private void selectCategory() {
        final NormalListDialog dialog = new NormalListDialog(context, StartActivity.CATEGORY_LIST);
        dialog.title("카테고리 선택")//
                .titleTextSize_SP(14.5f)//
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                category = StartActivity.CATEGORY_LIST[position];
                initLoadValue();
                progressDialog.show();
                getTuteeList();

                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                orderCategory.setTitle("카테고리로 조회");
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                orderCategory.setTitle("카테고리로 조회");
            }
        });
    }

    private void initLoadValue(){
        page = 0;
        isLoadFinish = false;
    }

    public void makeList(){

        int scrollX = rv.computeHorizontalScrollOffset();
        int scrollY = rv.computeVerticalScrollOffset();

        adapter = new TuteeListCustomAdapter(context, list, rv, this, ShowTuetueActivity.TYPE_TUTEE_LIST);

        rv.setAdapter(adapter);

        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                page+=1;
                getTuteeList();
            }
        });

        adapter.notifyDataSetChanged();

        mPtrFrameLayout.refreshComplete();

        rv.smoothScrollBy(scrollX, scrollY);

    }

    @Override
    public void showView() {
        menu.setVisibility(View.VISIBLE);
//        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    @Override
    public void hideView() {
        menu.setVisibility(View.INVISIBLE);
//        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
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
                case MSG_MESSAGE_MAKE_ENDLESS_LIST:
                    loading.hide();
                    addList();
                    break;
                case MSG_MESSAGE_PROGRESS_HIDE:
                    progressDialog.hide();
                    break;
                default:
                    break;
            }
        }
    }

    private void addList(){

        for(int i=0; i<tempList.size(); i++){
            list.add(tempList.get(i));
            adapter.notifyItemInserted(list.size());
        }
        adapter.setLoaded();

    }

    public static void setTuteeUpdate(String id){
        isTuteeUpdate = true;
        updateId = id;
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
        if(isTuteeUpdate){
            try{
                isTuteeUpdate = false;
                getTuteeList();

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
