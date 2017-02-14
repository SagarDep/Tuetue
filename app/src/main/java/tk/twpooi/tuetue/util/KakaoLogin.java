package tk.twpooi.tuetue.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

/**
 * Created by tw on 2017-02-14.
 */

/**
 * # build.gradle(Project)
 * Add this content maven { url 'http://devrepo.kakao.com:8088/nexus/content/groups/public/' }
 * <p>
 * # build.gradle(app)
 * Add this content compile group: project.KAKAO_SDK_GROUP, name: 'usermgmt', version: project.KAKAO_SDK_VERSION
 * <p>
 * # gradle.properties
 * Add this content
 * KAKAO_SDK_GROUP=com.kakao.sdk
 * KAKAO_SDK_VERSION=1.1.33
 * <p>
 * # strings.xml
 * Add this content <string name = "kakao_app_key">PLEASE ENTER YOUR APP KEY</string>
 * <p>
 * # AndroidManifest.sml
 * Modify this content
 * <application
 * android:name=".GlobalApplication"
 * <p>
 * <meta-data
 * android:name="com.kakao.sdk.AppKey"
 * android:value="@string/kakao_app_key" />
 */

public class KakaoLogin {

    private SessionCallback callback;
    private KakaoLoginSupport support;

    private UserProfile userProfile;
    private boolean isAlreadyLogin;

    public KakaoLogin() {

        this.support = null;

        String token = Session.getCurrentSession().getRefreshToken();

        if (token != null && !"".equals(token)) {
            isAlreadyLogin = true;
        } else {
            isAlreadyLogin = false;
        }

        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);

    }

    public KakaoLogin(KakaoLoginSupport support) {
        this();
        this.support = support;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
    }

    public boolean isAlreadyLogin() {

        return isAlreadyLogin;

    }

    public void setAlreadyLogin(boolean b) {
        this.isAlreadyLogin = b;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void login() {
        callback.onSessionOpened();
    }

    public void logout() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                if (support != null) {
                    support.afterKakaoLogout();
                }
            }
        });
    }

    public void unlink() {
        UserManagement.requestUnlink(new UnLinkResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                System.out.println("fail");
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                System.out.println("session close");
            }

            @Override
            public void onNotSignedUp() {
                System.out.println("not sign up");
            }

            @Override
            public void onSuccess(Long userId) {
                System.out.println("success");
            }
        });
    }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {

            UserManagement.requestMe(new MeResponseCallback() {

                @Override
                public void onFailure(ErrorResult errorResult) {
                    if (support != null) {
                        support.afterKakaoLoginFailed(errorResult);
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    if (support != null) {
                        support.afterKakaoLoginSessionClosed(errorResult);
                    }
                }

                @Override
                public void onNotSignedUp() {
                    if (support != null) {
                        support.afterKakaoLoginNotSignedUp();
                    }
                }

                @Override
                public void onSuccess(UserProfile profile) {
                    userProfile = profile;
                    if (isAlreadyLogin) {
                        if (support != null) {
                            support.afterKakaoLoginIsAlreadyLogin(profile);
                        }
                    } else {
                        isAlreadyLogin = true;
                        if (support != null) {
                            support.afterKakaoLoginSuccess(profile);
                        }
                    }
                }
            });

        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if (support != null) {
                support.kakaoSessionOpenFailed(exception);
            }
        }
    }

    public static class KakaoSDKAdapter extends KakaoAdapter {

        /**
         *      * 로그인을 위해 Session을 생성하기 위해 필요한 옵션을 얻기위한 abstract class.
         *      * 기본 설정은 KakaoAdapter에 정의되어있으며, 설정 변경이 필요한 경우 상속해서 사용할 수 있다.
         *      
         */
        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    return new AuthType[]{AuthType.KAKAO_LOGIN_ALL};
                }


                // 로그인시 인증받을 타입을 지정한다. 지정하지 않을 시 가능한 모든 옵션이 지정된다.

                //1.KAKAO_TALK :  kakaotalk으로 login을 하고 싶을때 지정.
                //2.KAKAO_STORY : kakaostory으로 login을 하고 싶을때 지정.
                //3.KAKAO_ACCOUNT :  웹뷰 Dialog를 통해 카카오 계정연결을 제공하고 싶을경우 지정.
                //4.KAKAO_TALK_EXCLUDE_NATIVE_LOGIN : 카카오톡으로만 로그인을 유도하고 싶으면서 계정이 없을때 계정생성을 위한
                //버튼도 같이 제공을 하고 싶다면 지정.KAKAO_TALK과 중복 지정불가.
                //5.KAKAO_LOGIN_ALL : 모든 로그인방식을 사용하고 싶을때 지정.

                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                @Override
                public boolean isSecureMode() {
                    return false;
                }
                // SDK 로그인시 사용되는 WebView에서 pause와 resume시에 Timer를 설정하여 CPU소모를 절약한다.
                // true 를 리턴할경우 webview로그인을 사용하는 화면서 모든 webview에 onPause와 onResume
                // 시에 Timer를 설정해 주어야 한다. 지정하지 않을 시 false로 설정된다.

                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }
                //일반 사용자가 아닌 Kakao와 제휴된 앱에서 사용되는 값으로, 값을 채워주지 않을경우 ApprovalType.INDIVIDUAL 값을 사용하게 된다.

                @Override
                public boolean isSaveFormData() {
                    return true;
                }
                // Kakao SDK 에서 사용되는 WebView에서 email 입력폼에서 data를 save할지여부를 결정한다. Default true.
            };
        }

        //Application이 가지고있는 정보를 얻기위한 interface.
        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {

                public Activity getTopActivity() {
                    return GlobalApplication.getCurrentActivity();
                }
                // 현재 최상단에 위치하고 있는 Activity. topActivity가 아니거나 ApplicationContext를 넣는다면
                // SDK내에서의 Dialog Popup등이 동작하지 않을 수 있습니다.


                @Override
                public Context getApplicationContext() {
                    return GlobalApplication.getGlobalApplicationContext();
                }
            };
        }
    }

}
