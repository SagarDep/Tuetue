package tk.twpooi.tuetue;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

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
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import in.srain.cube.views.ptr.util.PtrLocalDisplay;

/**
 * Created by tw on 2016-08-16.
 */
public class TuteeListFragment extends Fragment {


    private MyHandler handler = new MyHandler();
    private final int MSG_MESSAGE_LOAD_DATA_START = 1001;
    private final int MSG_MESSAGE_LOAD_DATA_FINISH = 1002;
    private final int MSG_MESSAGE_LOAD_DATA_FINISH2 = 1003;
    private final int MSG_MESSAGE_LOAD_DATA_ERROR = 1004;

    private SweetAlertDialog pDialog;

    private GetTutee getTutee;
    private GetTuteeInfoByCategory getTuteeInfoByCategory;

    // UI
    private View view;
    private Toolbar mToolbar;
    private Context context;
    private FloatingActionsMenu menu;
    private FloatingActionButton addTutor;
    private FloatingActionButton orderCategory;


    protected PtrFrameLayout mPtrFrameLayout;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private TuteeListCustomAdapter adapter;

    private static boolean isTuteeUpdate;
    private static String updateId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_tutor_list, container, false);
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
                getTutee = new GetTutee();
                getTutee.start();
            }
        });

        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);

        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);


        getTutee = new GetTutee();
        getTutee.start();

    }

    public void floationMenu(){

        menu = (FloatingActionsMenu)view.findViewById(R.id.multiple_actions);

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
                    getTutee = new GetTutee();
                    getTutee.start();
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
                String ca = StartActivity.CATEGORY_LIST[position];
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("category", ca);
                getTuteeInfoByCategory = new GetTuteeInfoByCategory(map);
                getTuteeInfoByCategory.start();
                dialog.dismiss();
            }
        });
    }

    public void makeList(ArrayList<HashMap<String, Object>> list){

        int scrollX = rv.computeHorizontalScrollOffset();
        int scrollY = rv.computeVerticalScrollOffset();

        adapter = new TuteeListCustomAdapter(context, list, rv, mToolbar, this);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        mPtrFrameLayout.refreshComplete();

        rv.smoothScrollBy(scrollX, scrollY);

    }


    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_LOAD_DATA_START:
                    pDialog.show();
                    break;
                case MSG_MESSAGE_LOAD_DATA_FINISH:
                    pDialog.hide();
                    makeList(getTutee.getResult());
                    break;
                case MSG_MESSAGE_LOAD_DATA_FINISH2:
                    pDialog.hide();
                    makeList(getTuteeInfoByCategory.getResult());
                    break;
                case MSG_MESSAGE_LOAD_DATA_ERROR:
                    pDialog.hide();
                    showSnackbar("Error 잠시 후 다시 시도해주세요.");
                default:
                    break;
            }
        }
    }

    class GetTutee extends Thread{

        private ArrayList<HashMap<String, Object>> result = new ArrayList<>();

        public void run(){


            Message msg;
            msg = handler.obtainMessage(MSG_MESSAGE_LOAD_DATA_START);
            handler.sendMessage(msg);

            String url = Information.MAIN_SERVER_ADDRESS + "getTuteeList.php";

            StringBuilder jsonHtml = new StringBuilder();
            try {
                URL phpUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)phpUrl.openConnection();

                if ( conn != null ) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);

                    if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        while ( true ) {
                            String line = br.readLine();
                            if ( line == null )
                                break;
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }

            String str = jsonHtml.toString();
            try {
                // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                JSONObject jObject = new JSONObject(str);
                // results라는 key는 JSON배열로 되어있다.
                JSONArray results = jObject.getJSONArray("result");
                String countTemp = (String)jObject.get("num_result");
                int count = Integer.parseInt(countTemp);

                for ( int i = 0; i < count; ++i ) {
                    JSONObject temp = results.getJSONObject(i);

                    HashMap<String, Object> hashTemp = new HashMap<>();
                    hashTemp.put("id", (String)temp.get("id"));
                    hashTemp.put("userid", (String)temp.get("userid"));
                    hashTemp.put("img", (String)temp.get("img"));
                    hashTemp.put("nickname", (String)temp.get("nickname"));
                    hashTemp.put("category", (String)temp.get("category"));
                    hashTemp.put("cost", Integer.parseInt((String)temp.get("cost")));
                    hashTemp.put("time", (String)temp.get("time"));
                    hashTemp.put("contents", (String)temp.get("contents"));
                    hashTemp.put("isFinish", (String)temp.get("isFinish"));
                    hashTemp.put("tutorId", (String)temp.get("tutorId"));

                    String participant = (String)temp.get("participant");

                    ArrayList<String> par = new ArrayList<>();

                    if(!participant.equals("")){
                        String[] p = participant.split(",");

                        for(String s : p){
                            par.add(s);
                        }

                    }
                    hashTemp.put("participant", par);

                    result.add(hashTemp);

                }

                msg = handler.obtainMessage(MSG_MESSAGE_LOAD_DATA_FINISH);
                handler.sendMessage(msg);

                return;

            } catch (JSONException e) {
                e.printStackTrace();
            }


            msg = handler.obtainMessage(MSG_MESSAGE_LOAD_DATA_ERROR);
            handler.sendMessage(msg);

        }

        public ArrayList<HashMap<String, Object>> getResult(){
            return result;
        }

    }

    private class GetTuteeInfoByCategory extends Thread{

        private ArrayList<HashMap<String, Object>> result = new ArrayList<>();
        private HashMap<String, String> map;

        public GetTuteeInfoByCategory(HashMap<String, String> map){
            this.map = map;
        }

        public void run(){

            Message msg;
            msg = handler.obtainMessage(MSG_MESSAGE_LOAD_DATA_START);
            handler.sendMessage(msg);

            String addr = Information.MAIN_SERVER_ADDRESS + "getTuteeListByCategory.php";
            String response = new String();

            try {
                URL url = new URL(addr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 해당 URL에 연결

                conn.setConnectTimeout(10000); // 타임아웃: 10초
                conn.setUseCaches(false); // 캐시 사용 안 함
                conn.setRequestMethod("POST"); // POST로 연결
                conn.setDoInput(true);
                conn.setDoOutput(true);

                if (map != null) { // 웹 서버로 보낼 매개변수가 있는 경우우
                    OutputStream os = conn.getOutputStream(); // 서버로 보내기 위한 출력 스트림
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8")); // UTF-8로 전송
                    bw.write(getPostString(map)); // 매개변수 전송
                    bw.flush();
                    bw.close();
                    os.close();
                }

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 연결에 성공한 경우
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())); // 서버의 응답을 읽기 위한 입력 스트림

                    while ((line = br.readLine()) != null) // 서버의 응답을 읽어옴
                        response += line;
                }

                conn.disconnect();
            } catch (MalformedURLException me) {
                me.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String str = response.toString();
            try {
                // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                JSONObject jObject = new JSONObject(str);
                // results라는 key는 JSON배열로 되어있다.
                JSONArray results = jObject.getJSONArray("result");
                String countTemp = (String)jObject.get("num_result");
                int count = Integer.parseInt(countTemp);

                for ( int i = 0; i < count; ++i ) {
                    JSONObject temp = results.getJSONObject(i);

                    HashMap<String, Object> hashTemp = new HashMap<>();
                    hashTemp.put("id", (String)temp.get("id"));
                    hashTemp.put("userid", (String)temp.get("userid"));
                    hashTemp.put("img", (String)temp.get("img"));
                    hashTemp.put("nickname", (String)temp.get("nickname"));
                    hashTemp.put("category", (String)temp.get("category"));
                    hashTemp.put("cost", Integer.parseInt((String)temp.get("cost")));
                    hashTemp.put("time", (String)temp.get("time"));
                    hashTemp.put("contents", (String)temp.get("contents"));
                    hashTemp.put("isFinish", (String)temp.get("isFinish"));
                    hashTemp.put("tutorId", (String)temp.get("tutorId"));

                    String participant = (String)temp.get("participant");

                    ArrayList<String> par = new ArrayList<>();

                    if(!participant.equals("")){
                        String[] p = participant.split(",");

                        for(String s : p){
                            par.add(s);
                        }

                    }
                    hashTemp.put("participant", par);

                    result.add(hashTemp);

                }


                msg = handler.obtainMessage(MSG_MESSAGE_LOAD_DATA_FINISH2);
                handler.sendMessage(msg);

                return;

            } catch (JSONException e) {
                e.printStackTrace();
            }


            msg = handler.obtainMessage(MSG_MESSAGE_LOAD_DATA_ERROR);
            handler.sendMessage(msg);

        }

        private String getPostString(HashMap<String, String> map) {
            StringBuilder result = new StringBuilder();
            boolean first = true; // 첫 번째 매개변수 여부

            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (first)
                    first = false;
                else // 첫 번째 매개변수가 아닌 경우엔 앞에 &를 붙임
                    result.append("&");

                try { // UTF-8로 주소에 키와 값을 붙임
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException ue) {
                    ue.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return result.toString();
        }


        public ArrayList<HashMap<String, Object>> getResult(){
            return result;
        }

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

    public void hideViews() {
//        menu.setVisibility(View.INVISIBLE);
//        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    public void showViews() {
//        menu.setVisibility(View.VISIBLE);
//        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }


    @Override
    public void onResume(){
        super.onResume();
        if(isTuteeUpdate){
            try{
                isTuteeUpdate = false;
                getTutee = new GetTutee();
                getTutee.start();
//                int index = -1;
//                for(int i=0; i<adapter.attractionList.size(); i++){
//                    HashMap<String, Object> h = adapter.attractionList.get(i);
//                    if(updateId.equals((String)h.get("id"))){
//                        index = i;
//                        break;
//                    }
//                }
//
//                GetTutee getTutee = new GetTutee();
//                getTutee.start();
//                try{
//                    getTutee.join();
//                }catch (Exception e){
//
//                }
//                adapter.attractionList = getTutee.getResult();
//                adapter.notifyItemChanged(index);

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
