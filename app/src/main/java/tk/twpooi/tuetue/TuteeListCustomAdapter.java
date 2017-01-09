package tk.twpooi.tuetue;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;


/**
 * Created by tw on 2016-08-16.
 */
public class TuteeListCustomAdapter extends RecyclerView.Adapter<TuteeListCustomAdapter.ViewHolder> {

    // UI
    private Toolbar mToolbar;
    private Context context;

    private TuteeListFragment f;
    private FileManager fileManager;

    public ArrayList<HashMap<String, Object>> attractionList;
    private ArrayList<String> interestList;

    // 생성자
    public TuteeListCustomAdapter(Context context, ArrayList<HashMap<String,Object>> attractionList, RecyclerView recyclerView, Toolbar toolbar, TuteeListFragment f) {
        this.context = context;
        this.attractionList = attractionList;
        this.f = f;

        fileManager = new FileManager(context);
        interestList = fileManager.readInterestListFile();

        mToolbar = toolbar;

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tutor_list_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,Object> noticeData = attractionList.get(position);
        final int pos = position;
        final String id = (String)noticeData.get("id");
        final String userId = (String)noticeData.get("userid");
        String isFinish = (String)noticeData.get("isFinish");

        String profileImg = (String)noticeData.get("img");
        Picasso.with(context)
                .load(profileImg)
                .transform(new CropCircleTransformation())
                .into(holder.profileImg);

        String nickname = (String)noticeData.get("nickname");
        holder.tv_nickname.setText(nickname);

        holder.tv_interest.setVisibility(View.GONE);
        holder.tv_count.setVisibility(View.GONE);
        holder.interestBtn.setVisibility(View.GONE);

        int cost = (int)noticeData.get("cost");
        holder.tv_cost.setText("비용 : " + cost + "원");

        final String contents = (String)noticeData.get("contents");
        holder.tv_contents.setText(contents);


        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ShowTuteeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("id", id);
                intent.putExtra("index", pos);
                context.startActivity(intent);

            }
        });
//        holder.tv_title.setText(noticeData.get("title")); //제목

        holder.rl_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userId", userId);
                context.startActivity(intent);
            }
        });

        if("0".equals(isFinish)){
            holder.success_frame.setVisibility(View.GONE);
        }else{
            holder.success_frame.setVisibility(View.VISIBLE);
        }

    }


    @Override
    public int getItemCount() {
        return this.attractionList.size();
    }

    private void hideViews() {
        if(f == null){
            if(mToolbar != null)
                mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        }else {
            f.hideViews();
        }
    }

    private void showViews() {
        if(f == null){
            if(mToolbar != null)
                mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        }else{
            f.showViews();
        }
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
        RelativeLayout success_frame;
        RelativeLayout rl_profile;
        TextView tv_nickname;
        TextView tv_email;
        //LinearLayout contentLayout;
        CardView cv;
        ImageView profileImg;
        //RelativeLayout rl_image;
        TextView tv_contents;
        TextView tv_count;
        TextView tv_cost;
        TextView tv_interest;
        ShineButton interestBtn;
        //RelativeLayout rl_button;


        public ViewHolder(View v) {
            super(v);
            success_frame = (RelativeLayout)v.findViewById(R.id.success_frame);
            cv = (CardView) v.findViewById(R.id.cv);
            rl_profile = (RelativeLayout)v.findViewById(R.id.rl_profile);
            tv_nickname = (TextView) v.findViewById(R.id.nickname);
            tv_email = (TextView) v.findViewById(R.id.email);
            profileImg = (ImageView)v.findViewById(R.id.rl_profile_img);
            tv_contents = (TextView)v.findViewById(R.id.contents);
            tv_count = (TextView)v.findViewById(R.id.count);
            tv_cost = (TextView)v.findViewById(R.id.cost);
            tv_interest = (TextView)v.findViewById(R.id.interest);
            interestBtn = (ShineButton)v.findViewById(R.id.interest_btn);
        }
    }

}
