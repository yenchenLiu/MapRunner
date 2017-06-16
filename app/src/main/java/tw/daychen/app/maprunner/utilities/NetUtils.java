package tw.daychen.app.maprunner.utilities;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import tw.daychen.app.maprunner.R;
import tw.daychen.app.maprunner.data.MapRunnerContract;

/**
 * Created by daychen on 2017/6/6.
 */

public class NetUtils {
    private static String QUERY_URL = "";
    private static Uri contactsSettingUri = Uri.parse("content://tw.daychen.app.maprunner/setting/");
    private static Context context = null;
    private static final String ACCESS_TOKEN_STR = "server_access_token";
    private static final String USERNAME_STR = "username";

    public static URL buildUrl(String path, Context context) {
        NetUtils.context = context;

        QUERY_URL = context.getString(R.string.query_url);
        Log.d("net", path);
        Uri builtUri = Uri.parse(QUERY_URL).buildUpon()
                .appendEncodedPath(path)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
            Log.d("net", url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponseFromLogin(URL url, HashMap<String, String> postDataParams) throws IOException {
        postDataParams.put("grant_type", "password");
        postDataParams.put("client_id", context.getString(R.string.client_id));
        postDataParams.put("client_secret", context.getString(R.string.client_secret));
        return getResponseFromHttpUrl(url, "POST", postDataParams, null);
    }

    public static String getResponseFromAccessCode(URL url, String method, HashMap<String, String> postDataParams) throws IOException {
        String selection = "key=?";
        String[] selectionArgs = new String[1];
        selectionArgs[0] = ACCESS_TOKEN_STR;
        String accessToken = null;

        try (Cursor cursor = context.getContentResolver().query(contactsSettingUri, null, selection, selectionArgs, null)) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            accessToken = cursor.getString(cursor.getColumnIndex(MapRunnerContract.SettingEntry.COLUMN_VALUE));
        }
        Log.d("Net", "access token:" + accessToken);
        HashMap<String, String> headerParams = new HashMap<>();
        if (accessToken == null) {
            return null;
        }

        headerParams.put("Authorization", "Bearer " + accessToken);

        return getResponseFromHttpUrl(url, method, postDataParams, headerParams);
    }

    public static String getResponseFromHttpUrl(URL url, String method, HashMap<String, String> postDataParams, HashMap<String, String> headerParams) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String response = "";
        Log.d("Net", "get response");
        try {
            urlConnection.setRequestMethod(method);
            if (method == "POST") {
                urlConnection.setDoOutput(true);
            }
            urlConnection.setDoInput(true);
            if (headerParams != null) {
                for (Map.Entry<String, String> header : headerParams.entrySet()) {
                    urlConnection.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            if (method == "POST") {
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                if (postDataParams != null) {
                    writer.write(getPostDataString(postDataParams));
                }
                writer.flush();
                writer.close();

                os.close();
            }
            urlConnection.connect();
            Log.d("Net", "method :" + urlConnection.getRequestMethod());
            int responseCode = urlConnection.getResponseCode();
            Log.d("Net", "responseCode:" + String.valueOf(responseCode));
            if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == HttpsURLConnection.HTTP_CREATED) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
            } else {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                while ((line = br.readLine()) != null) {
                    response += line;
                }
                Log.d("net", "error:"+response);
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
        return response;
    }

    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

}
