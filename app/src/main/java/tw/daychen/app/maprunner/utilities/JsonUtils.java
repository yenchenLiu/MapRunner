package tw.daychen.app.maprunner.utilities;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by daychen on 2017/6/12.
 */

public final class JsonUtils {
    public static HashMap<String, String> getLoginDataFromJson(Context context, String loginJsonStr) throws JSONException{
        final String ERROR_MESSAGE_CODE = "error";
        final String TOKEN_TYPE_CODE = "token_type";
        final String FREFRESH_TOKEN_CODE = "refresh_token";
        final String ACCESS_TOKEN_CODE = "access_token";

        JSONObject resultJson = new JSONObject(loginJsonStr);
        HashMap<String, String> parsedLoginData = new HashMap<>();

        if (resultJson.has(ERROR_MESSAGE_CODE)){
            parsedLoginData.put(ERROR_MESSAGE_CODE, resultJson.getString(ERROR_MESSAGE_CODE));
            return parsedLoginData;
        }
        parsedLoginData.put(TOKEN_TYPE_CODE, resultJson.getString(TOKEN_TYPE_CODE));
        parsedLoginData.put(FREFRESH_TOKEN_CODE, resultJson.getString(FREFRESH_TOKEN_CODE));
        parsedLoginData.put(ACCESS_TOKEN_CODE, resultJson.getString(ACCESS_TOKEN_CODE));
        return parsedLoginData;
    }
    public static String getUsernameFromJson(Context context, String loginJsonStr) throws JSONException{
        final String USERNAME_CODE = "username";
        JSONObject resultJson = new JSONObject(loginJsonStr);
        return resultJson.getString(USERNAME_CODE);
    }
    public static int getServerIDFromJson(Context context, String loginJsonStr) throws JSONException{
        final String ID_CODE = "id";
        JSONObject resultJson = new JSONObject(loginJsonStr);
        return resultJson.getInt(ID_CODE);
    }
}
