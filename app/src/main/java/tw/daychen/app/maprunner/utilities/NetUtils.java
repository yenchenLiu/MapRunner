package tw.daychen.app.maprunner.utilities;

import android.content.Context;
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

/**
 * Created by daychen on 2017/6/6.
 */

public class NetUtils{
    private static String QUERY_URL = "";
    private static Context context = null;
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }
    public static String getResponseFromLogin(URL url, HashMap<String, String> postDataParams) throws IOException{
        postDataParams.put("grant_type", "password");
        postDataParams.put("client_id", context.getString(R.string.client_id));
        postDataParams.put("client_secret", context.getString(R.string.client_secret));
        return getResponseFromHttpUrl(url, postDataParams, null);
    }

    public static String getResponseFromAccessCode(URL url, HashMap<String, String> postDataParams) throws IOException{
        return null;
    }

    public static String getResponseFromHttpUrl(URL url, HashMap<String, String> postDataParams, HashMap<String, String> headerParams) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        String response = "";
        try {
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            if (headerParams != null){
                for (Map.Entry<String, String> header: headerParams.entrySet()) {
                    urlConnection.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            urlConnection.connect();
            int responseCode=urlConnection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
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
        for(Map.Entry<String, String> entry : params.entrySet()){
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
