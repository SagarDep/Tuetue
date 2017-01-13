package tk.twpooi.tuetue.util;

import android.app.Activity;
import android.content.Context;
import android.icu.text.IDNA;
import android.os.AsyncTask;
import android.util.Log;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginDefine;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.HashMap;

import tk.twpooi.tuetue.Information;

/**
 * Created by tw on 2017-01-10.
 */

public class NaverLogin {

    private static final String TAG = "NaverLogin";

    /**
     * client 정보를 넣어준다.
     */
    private static String OAUTH_CLIENT_ID = Information.NAVER_CLIENT_ID;
    private static String OAUTH_CLIENT_SECRET = Information.NAVER_CLIENT_SECRET;
    private static String OAUTH_CLIENT_NAME = "튜튜-Tuetue";

    private OAuthLogin mOAuthLoginInstance;
    private  Context mContext;

    private OAuthLoginButton mOAuthLoginButton;

    private NaverLoginSupport nls;

    public NaverLogin(Context context, OAuthLoginButton mOAuthLoginButton){

        this.mContext = context;
        this.mOAuthLoginButton = mOAuthLoginButton;

        OAuthLoginDefine.DEVELOPER_VERSION = true;

        initData();

    }

    public NaverLogin(Context context, OAuthLoginButton mOAuthLoginButton, NaverLoginSupport nls){
        this(context, mOAuthLoginButton);
        this.nls = nls;
    }

    private void initData() {
        mOAuthLoginInstance = OAuthLogin.getInstance();

        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);
		/*
		 * 2015년 8월 이전에 등록하고 앱 정보 갱신을 안한 경우 기존에 설정해준 callback intent url 을 넣어줘야 로그인하는데 문제가 안생긴다.
		 * 2015년 8월 이후에 등록했거나 그 뒤에 앱 정보 갱신을 하면서 package name 을 넣어준 경우 callback intent url 을 생략해도 된다.
		 */
        //mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME, OAUTH_callback_intent_url);
    }

    public String getOAuthAT(){
        return mOAuthLoginInstance.getAccessToken(mContext);
    }
    public String getOAuthRT(){
        return mOAuthLoginInstance.getRefreshToken(mContext);
    }
    public String getExpires(){
        return String.valueOf(mOAuthLoginInstance.getExpiresAt(mContext));
    }
    public String getOAuthTokenType(){
        return mOAuthLoginInstance.getTokenType(mContext);
    }
    public String getOAuthState(){
        return mOAuthLoginInstance.getState(mContext).toString();
    }

    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                if(nls != null){
                    nls.afterNaverLoginSuccess(getUserInformation());
                }
            } else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                if(nls != null){
                    nls.afterNaverLoginFail(errorCode, errorDesc);
                }
            }
        }
    };

    public void login(){
        mOAuthLoginInstance.startOauthLoginActivity((Activity)mContext, mOAuthLoginHandler);
    }
    public void logout(){
        mOAuthLoginInstance.logout(mContext);
    }
    public String getRequestApi(){

        String data = "";
        try {
            data = new RequestApiTask().execute().get();
        }catch (Exception e){

        }

        return data;

    }
    public String getRefreshToken(){
        String data = "";
        try{
            data = new RefreshTokenTask().execute().get();
        }catch (Exception e){

        }

        return data;

    }
    public void deleteToken(){
        new DeleteTokenTask().execute();
    }

    public HashMap<String, String> getUserInformation(){

        HashMap<String, String> returnData = new HashMap<>();

        String xml = "";
        try {
            xml = new RequestApiTask().execute().get();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader (xml));
            int eventType = parser.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        String startTag = parser.getName();
                        if(startTag.equals("eamil")) {
                            returnData.put("email", parser.nextText());
                        }
                        if(startTag.equals("profile_image")) {
                            returnData.put("img", parser.nextText());
                        }
                        if(startTag.equals("id")) {
                            returnData.put("id", parser.nextText());
                        }
                        if(startTag.equals("name")) {
                            returnData.put("name", parser.nextText());
                        }
                        if(startTag.equals("nickname")) {
                            returnData.put("nickname", parser.nextText());
                        }
                        if(startTag.equals("enc_id")) {
                            returnData.put("enc_id", parser.nextText());
                        }
                        if(startTag.equals("birthday")) {
                            returnData.put("birthday", parser.nextText());
                        }
                        if(startTag.equals("age")) {
                            returnData.put("age", parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
//                        String endTag = parser.getName();
//                        if(endTag.equals("")) {
//                            arrayList.add();
//                        }
                        break;
                }
                eventType = parser.next();
            }

        }catch (Exception e){
        }



        return returnData;

    }

    private class DeleteTokenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            boolean isSuccessDeleteToken = mOAuthLoginInstance.logoutAndDeleteToken(mContext);

            if (!isSuccessDeleteToken) {
                // 서버에서 token 삭제에 실패했어도 클라이언트에 있는 token 은 삭제되어 로그아웃된 상태이다
                // 실패했어도 클라이언트 상에 token 정보가 없기 때문에 추가적으로 해줄 수 있는 것은 없음
                Log.d(TAG, "errorCode:" + mOAuthLoginInstance.getLastErrorCode(mContext));
                Log.d(TAG, "errorDesc:" + mOAuthLoginInstance.getLastErrorDesc(mContext));
            }

            return null;
        }
        protected void onPostExecute(Void v) {

        }
    }

    private class RequestApiTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {

        }
        @Override
        protected String doInBackground(Void... params) {
            String url = "https://openapi.naver.com/v1/nid/getUserProfile.xml";
            String at = mOAuthLoginInstance.getAccessToken(mContext);
            return mOAuthLoginInstance.requestApi(mContext, at, url);
        }
        protected void onPostExecute(String content) {
            super.onPostExecute(content);

        }
    }

    private class RefreshTokenTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            return mOAuthLoginInstance.refreshAccessToken(mContext);
        }
        protected void onPostExecute(String res) {

        }
    }

}
