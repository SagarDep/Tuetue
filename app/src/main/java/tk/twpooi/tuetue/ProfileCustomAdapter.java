package tk.twpooi.tuetue;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.GONE;

/**
 * Created by tw on 2016-08-16.
 */
public class ProfileCustomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TYPE_HEADER = 0;
    private final int TYPE_ITEM = 2;

    // UI
    private Activity activity;
    private Context context;
    private View mainView;
    private View header;

    // Data
    private ArrayList<HashMap<String, String>> list;
    private ArrayList<String> participantList;


    // 생성자
    public ProfileCustomAdapter(Context context, ArrayList<HashMap<String, String>> list, RecyclerView recyclerView, View view, View header, Activity activity) {
        this.context = context;
        this.list = list;
        this.mainView = view;
        this.header = header;
        this.activity = activity;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            recyclerView.addOnScrollListener(new ScrollListener() {
                @Override
                public void onHide() {
                    hideViews();
                }

                @Override
                public void onShow() {
                    showViews();
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        if(viewType == TYPE_ITEM){
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.basic_list_custom_item,null);
            return new ItemViewHolder(v);
        }else if(viewType == TYPE_HEADER){
            return new HeaderViewHolder(header);
        }else{
            return null;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ItemViewHolder){

            ItemViewHolder itemHolder = (ItemViewHolder)holder;

            final HashMap<String,String> noticeData = list.get(position-1);
            final int pos = position;

            String title = noticeData.get("title");
            String content = noticeData.get("content");

            if("관심분야".equals(title)){

                itemHolder.title.setText(title);
                itemHolder.content.setText("");

                if(content == null ||"".equals(content)){
                }else{
                    itemHolder.loading.hide();
                    itemHolder.loading.setVisibility(GONE);

                    ArrayList<String> temp = new ArrayList<>();
                    for(String s : content.split(",")){
                        temp.add(s);
                    }

                    setInterestField(itemHolder.interestField, temp);
                }

            }else{

                itemHolder.title.setText(title);
                itemHolder.content.setText(content);

                if(content == null ||"".equals(content)){
                }else{
                    itemHolder.loading.hide();
                    itemHolder.loading.setVisibility(GONE);
                }

            }

        }else if(holder instanceof HeaderViewHolder){

            HeaderViewHolder headerHolder = (HeaderViewHolder)holder;

        }


    }

    private void setInterestField(LinearLayout interestField, final ArrayList<String> interest){

        if(interest.size() <= 0){
            return;
        }

        interestField.removeAllViews();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpWidth -= 130;

        final float scale = context.getResources().getDisplayMetrics().density;
        int width = (int) (dpWidth * scale + 0.5f);
        int dp5 = (int) (5 * scale + 0.5f);
        int dp3 = (int) (3 * scale * 0.5f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp3, dp3, dp3, dp3);
        int mButtonsSize = 0;
        Rect bounds = new Rect();

        boolean isAdd = false;
        TextView finalText = new TextView(context);
        finalText.setPadding(dp5*2, dp5, dp5*2, dp5);
        finalText.setBackgroundResource(R.drawable.round_button_blue);
        finalText.setTextColor(ContextCompat.getColor(context, R.color.white));
        finalText.setText("+" + interest.size());
        finalText.setLayoutParams(params);
        finalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] list = new String[interest.size()];
                for(int i=0; i<interest.size(); i++){
                    list[i] = interest.get(i);
                }
                ((ProfileActivity)activity).showListDialog("관심분야", list);
            }
        });

        for(int i=0; i<interest.size(); i++){

            int remainCount = interest.size() - i;
            String remainString = "+" + remainCount;
            finalText.setText(remainString);

            String s = interest.get(i);
            TextView mBtn = new TextView(context);
            mBtn.setPadding(dp5*2, dp5, dp5*2, dp5);
            mBtn.setBackgroundResource(R.drawable.round_button_blue_line);
            mBtn.setTextColor(ContextCompat.getColor(context, R.color.pastel_blue));
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

    public void updateList(int position, String data){

        HashMap<String, String> map = list.get(position);
        map.put("content", data);
        list.set(position, map);
        this.notifyItemChanged(position+1);

    }

    private void setClipBoardLink(Context context , String link){

        ClipboardManager clipboardManager = (ClipboardManager)context.getSystemService(context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", link);
        clipboardManager.setPrimaryClip(clipData);
        showSnackbar("클립보드에 저장되었습니다.");

    }

    private void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(mainView, msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_color));
        snackbar.show();
    }

    @Override
    public int getItemCount() {
        return this.list.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    private void hideViews() {
        if (activity != null) {
            ((ProfileActivity) activity).hideView();
        }
    }

    private void showViews() {
        if (activity != null) {
            ((ProfileActivity) activity).showView();
        }
    }

    public abstract class ScrollListener extends RecyclerView.OnScrollListener {
        private static final int HIDE_THRESHOLD = 20;
        private int scrolledDistance = 0;
        private boolean controlsVisible = true;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                onHide();
                controlsVisible = false;
                scrolledDistance = 0;
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                onShow();
                controlsVisible = true;
                scrolledDistance = 0;
            }

            if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
                scrolledDistance += dy;
            }
            // 여기까지 툴바 숨기기
        }

        public abstract void onHide();

        public abstract void onShow();

    }

    private final static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View view) {
            super(view);
        }

    }

    public final static class ItemViewHolder extends RecyclerView.ViewHolder {

        LinearLayout root;
        TextView title;
        TextView content;
        AVLoadingIndicatorView loading;
        LinearLayout interestField;

        public ItemViewHolder(View v) {
            super(v);
            root = (LinearLayout)v.findViewById(R.id.root);
            title = (TextView) v.findViewById(R.id.title);
            content = (TextView) v.findViewById(R.id.content);
            loading = (AVLoadingIndicatorView)v.findViewById(R.id.loading);
            interestField = (LinearLayout)v.findViewById(R.id.li_interest_field);
        }
    }

}
