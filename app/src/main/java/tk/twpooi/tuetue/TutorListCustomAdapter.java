package tk.twpooi.tuetue;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.tuetue.util.AdditionalFunc;
import tk.twpooi.tuetue.util.OnLoadMoreListener;
import tk.twpooi.tuetue.util.OnAdapterSupport;


/**
 * Created by tw on 2016-08-16.
 */
public class TutorListCustomAdapter extends RecyclerView.Adapter<TutorListCustomAdapter.ViewHolder> {

    // UI
    private Context context;

//    private MaterialNavigationDrawer activity;
private OnAdapterSupport onAdapterSupport;
    private FileManager fileManager;

    private int type;
    public ArrayList<HashMap<String, Object>> attractionList;

    // 무한 스크롤
    private OnLoadMoreListener onLoadMoreListener;
    private int visibleThreshold = 3;
    private int lastVisibleItem, totalItemCount;
    private boolean loading = false;

    // 생성자
    public TutorListCustomAdapter(Context context, ArrayList<HashMap<String, Object>> attractionList, RecyclerView recyclerView, OnAdapterSupport listener, int type) {
        this.context = context;
        this.attractionList = attractionList;
        this.onAdapterSupport = listener;
        this.type = type;
//        this.activity = activity;

        fileManager = new FileManager(context);

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tuetue_list_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,Object> noticeData = attractionList.get(position);
        final int pos = position;
        final String id = (String)noticeData.get("id");
        final String userId = (String)noticeData.get("userid");
        ArrayList<String> par = (ArrayList<String>)noticeData.get("participant");

        final String profileImg = (String)noticeData.get("img");
        Picasso.with(context)
                .load(profileImg)
                .transform(new CropCircleTransformation())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(holder.profileImg);

        String nickname = (String)noticeData.get("nickname");
        holder.tv_nickname.setText(nickname);
        holder.tv_email.setText((String)noticeData.get("email"));

        int interest = (int)noticeData.get("interest");
        holder.tv_interest.setText(Integer.toString(interest));

        int count = (int)noticeData.get("count");
        holder.tv_count.setText(par.size() + "/" + count);

        final String contents = (String)noticeData.get("contents");
        holder.tv_contents.setText(contents);

        int dday = AdditionalFunc.getDday((Long)noticeData.get("limit"));
        if(dday < 0){
            holder.tv_dday.setText("D+"+Math.abs(dday));
        }else if(dday == 0){
            holder.tv_dday.setText("D-day");
        }else{
            holder.tv_dday.setText("D-"+dday);
        }


        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ShowTuetueActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("index", pos);
                intent.putExtra("type", type);
//                context.startActivity(intent);

                if (onAdapterSupport != null) {
                    onAdapterSupport.redirectActivityForResult(intent);
                }

            }
        });

        holder.rl_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("userId", userId);
//                context.startActivity(intent);

                if (onAdapterSupport != null) {
                    onAdapterSupport.redirectActivity(intent);
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return this.attractionList.size();
    }

    private void hideViews() {
        if (onAdapterSupport != null) {
            onAdapterSupport.hideView();
        }
    }

    private void showViews() {
        if (onAdapterSupport != null) {
            onAdapterSupport.showView();
        }
    }

    // 무한 스크롤
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }
    public void setLoaded() {
        loading = false;
    }

    public abstract class ScrollListener extends RecyclerView.OnScrollListener {
        private static final int HIDE_THRESHOLD = 20;
        private int scrolledDistance = 0;
        private boolean controlsVisible = true;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            totalItemCount = linearLayoutManager.getItemCount();
            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                // End has been reached
                // Do something
                loading = true;
                if (onLoadMoreListener != null) {
                    onLoadMoreListener.onLoadMore();
                }
            }
            // 여기까지 무한 스크롤

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

        CardView cv;
        RelativeLayout rl_profile;
        ImageView profileImg;
        TextView tv_nickname;
        TextView tv_email;
        TextView tv_dday;
        TextView tv_contents;
        TextView tv_interest;
        TextView tv_count;
        boolean isContentExpand;

        public ViewHolder(View v) {
            super(v);
            cv = (CardView)v.findViewById(R.id.cv);
            rl_profile = (RelativeLayout)v.findViewById(R.id.rl_profile);
            profileImg = (ImageView)v.findViewById(R.id.profileImg);
            tv_nickname = (TextView)v.findViewById(R.id.tv_nickname);
            tv_email = (TextView)v.findViewById(R.id.tv_email);
            tv_dday = (TextView)v.findViewById(R.id.tv_dday);
            tv_contents = (TextView)v.findViewById(R.id.tv_contents);
            tv_interest = (TextView)v.findViewById(R.id.tv_interest);
            tv_count = (TextView)v.findViewById(R.id.tv_count);
        }
    }

}
