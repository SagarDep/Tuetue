package tk.twpooi.tuetue.util;


import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

/**
 * Created by tw on 2017-02-14.
 */

public interface KakaoLoginSupport {

    void kakaoSessionOpenFailed(KakaoException exception);

    void afterKakaoLoginSuccess(UserProfile userProfile);

    void afterKakaoLoginFailed(ErrorResult errorResult);

    void afterKakaoLoginSessionClosed(ErrorResult errorResult);

    void afterKakaoLoginNotSignedUp();

    void afterKakaoLogout();

    void afterKakaoLoginIsAlreadyLogin(UserProfile userProfile);

}
