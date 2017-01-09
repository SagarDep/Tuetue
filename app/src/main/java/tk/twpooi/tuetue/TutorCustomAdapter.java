package tk.twpooi.tuetue;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tw on 2016-08-16.
 */
public class TutorCustomAdapter extends RecyclerView.Adapter<TutorCustomAdapter.ViewHolder> {

    // UI
    private Context context;
    private View mainView;

    // Data
    private ArrayList<HashMap<String, String>> list;
    private ArrayList<String> participantList;


    // 생성자
    public TutorCustomAdapter(Context context, ArrayList<HashMap<String,String>> list, View view) {
        this.context = context;
        this.list = list;
        this.mainView = view;
    }

    // 생성자
    public TutorCustomAdapter(Context context, ArrayList<HashMap<String,String>> list, ArrayList<String> pa, View view) {
        this.context = context;
        this.list = list;
        this.mainView = view;
        this.participantList = pa;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tutor_detail_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,String> noticeData = list.get(position);
        final int pos = position;

        String title = noticeData.get("title");
        String content = noticeData.get("content");

        holder.title.setText(title);
        holder.content.setText(content);

        if(title.equals("참여한 튜터")){
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ParticipantListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("participant", participantList);
                    context.startActivity(intent);
                }
            });
        }

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
        return this.list.size();
    }


    public abstract class ScrollListener extends RecyclerView.OnScrollListener {
        private static final int HIDE_THRESHOLD = 20;
        private int scrolledDistance = 0;
        private boolean controlsVisible = true;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                onHide();
                controlsVisible = false;
                scrolledDistance = 0;
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                onShow();
                controlsVisible = true;
                scrolledDistance = 0;
            }

            if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
                scrolledDistance += dy;
            }
            // 여기까지 툴바 숨기기
        }

        public abstract void onHide();
        public abstract void onShow();

    }

    public final static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout root;
        TextView title;
        TextView content;


        public ViewHolder(View v) {
            super(v);
            root = (LinearLayout)v.findViewById(R.id.root);
            title = (TextView) v.findViewById(R.id.title);
            content = (TextView) v.findViewById(R.id.content);
        }
    }

}
