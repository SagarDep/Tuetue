package tk.twpooi.tuetue.util;

import java.util.HashMap;

/**
 * Created by tw on 2017-01-11.
 */

public interface NaverLoginSupport {

    void afterNaverLoginSuccess(HashMap<String, String> data);
    void afterNaverLoginFail(String errorCode, String errorDesc);

}
