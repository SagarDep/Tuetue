package tk.twpooi.tuetue.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.kakao.auth.AuthType;
import com.kakao.auth.Session;
import com.kakao.usermgmt.LoginButton;
import com.kakao.util.helper.StoryProtocol;
import com.kakao.util.helper.TalkProtocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tk.twpooi.tuetue.R;

/**
 * Created by tw on 2017-02-14.
 */

public class KakaoLoginButton extends LoginButton {

    public KakaoLoginButton(Context context) {
        super(context);
    }

    public KakaoLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KakaoLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 로그인 버튼 클릭시 세션을 오픈하도록 설정한다.
     */
    @Override
    protected void onAttachedToWindow() {
        //super.onAttachedToWindow();
//        inflate(getContext(), R.layout.custom_kakao_login, this);
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // 카톡 또는 카스가 존재하면 옵션을 보여주고, 존재하지 않으면 바로 직접 로그인창.
                final List<AuthType> authTypes = getAuthTypes();
                if (authTypes.size() == 1) {
                    Session.getCurrentSession().open(authTypes.get(0), (Activity) getContext());
                } else {
                    onClickLoginButton(authTypes);
                }
            }
        });
    }

    private List<AuthType> getAuthTypes() {
        final List<AuthType> availableAuthTypes = new ArrayList<AuthType>();
        if (TalkProtocol.existCapriLoginActivityInTalk(getContext(), Session.getCurrentSession().isProjectLogin())) {
            availableAuthTypes.add(AuthType.KAKAO_TALK);
            availableAuthTypes.add(AuthType.KAKAO_TALK_EXCLUDE_NATIVE_LOGIN);
        }
        if (StoryProtocol.existCapriLoginActivityInStory(getContext(), Session.getCurrentSession().isProjectLogin())) {
            availableAuthTypes.add(AuthType.KAKAO_STORY);
        }
        availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);

        final AuthType[] selectedAuthTypes = Session.getCurrentSession().getAuthTypes();
        availableAuthTypes.retainAll(Arrays.asList(selectedAuthTypes));

        // 개발자가 설정한 것과 available 한 타입이 없다면 직접계정 입력이 뜨도록 한다.
        if (availableAuthTypes.size() == 0) {
            availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);
        }
        return availableAuthTypes;
    }

    private void onClickLoginButton(final List<AuthType> authTypes) {
        final List<KakaoLoginButton.Item> itemList = new ArrayList<KakaoLoginButton.Item>();
        if (authTypes.contains(AuthType.KAKAO_TALK)) {
            itemList.add(new KakaoLoginButton.Item(com.kakao.usermgmt.R.string.com_kakao_kakaotalk_account, com.kakao.usermgmt.R.drawable.kakaotalk_icon, com.kakao.usermgmt.R.string.com_kakao_kakaotalk_account_tts, AuthType.KAKAO_TALK));
        }
        if (authTypes.contains(AuthType.KAKAO_STORY)) {
            itemList.add(new KakaoLoginButton.Item(com.kakao.usermgmt.R.string.com_kakao_kakaostory_account, com.kakao.usermgmt.R.drawable.kakaostory_icon, com.kakao.usermgmt.R.string.com_kakao_kakaostory_account_tts, AuthType.KAKAO_STORY));
        }
        if (authTypes.contains(AuthType.KAKAO_ACCOUNT)) {
            itemList.add(new KakaoLoginButton.Item(com.kakao.usermgmt.R.string.com_kakao_other_kakaoaccount, com.kakao.usermgmt.R.drawable.kakaoaccount_icon, com.kakao.usermgmt.R.string.com_kakao_other_kakaoaccount_tts, AuthType.KAKAO_ACCOUNT));
        }
        itemList.add(new KakaoLoginButton.Item(com.kakao.usermgmt.R.string.com_kakao_account_cancel, 0, com.kakao.usermgmt.R.string.com_kakao_account_cancel_tts, null)); //no icon for this one

        final KakaoLoginButton.Item[] items = itemList.toArray(new KakaoLoginButton.Item[itemList.size()]);

        final ListAdapter adapter = new ArrayAdapter<KakaoLoginButton.Item>(
                getContext(),
                android.R.layout.select_dialog_item,
                android.R.id.text1, items) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                TextView tv = (TextView) v.findViewById(android.R.id.text1);

                tv.setText(items[position].textId);
                tv.setTextColor(Color.BLACK);
                tv.setTextSize(15);
                tv.setGravity(Gravity.CENTER);
                tv.setContentDescription(getContext().getString(items[position].contentDescId));
                if (position == itemList.size() - 1) {
                    tv.setBackgroundResource(com.kakao.usermgmt.R.drawable.kakao_cancel_button_background);
                } else {
                    tv.setBackgroundResource(com.kakao.usermgmt.R.drawable.kakao_account_button_background);
                }
                tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                tv.setCompoundDrawablePadding(dp5);

                return v;
            }
        };


        new AlertDialog.Builder(getContext())
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int position) {
                        final AuthType authType = items[position].authType;
                        if (authType != null) {
                            Session.getCurrentSession().open(authType, (Activity) getContext());
                        }

                        dialog.dismiss();
                    }
                }).create().show();

    }

    private static class Item {
        public final int textId;
        public final int icon;
        public final int contentDescId;
        public final AuthType authType;

        public Item(final int textId, final Integer icon, final int contentDescId, final AuthType authType) {
            this.textId = textId;
            this.icon = icon;
            this.contentDescId = contentDescId;
            this.authType = authType;
        }
    }
}
