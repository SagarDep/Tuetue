package tk.twpooi.tuetue.util;

import com.facebook.FacebookException;
import com.facebook.Profile;

import java.util.HashMap;

/**
 * Created by tw on 2017-01-11.
 */

public interface FacebookLoginSupport {

    void afterFBLoginSuccess(Profile profile, HashMap<String, String> data);
    void afterFBLoginCancel();
    void afterFBLoginError(FacebookException error);
    void afterFBLogout();

}
