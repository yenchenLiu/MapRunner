package tw.daychen.app.maprunner;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import tw.daychen.app.maprunner.data.MapRunnerContract;
import tw.daychen.app.maprunner.utilities.JsonUtils;
import tw.daychen.app.maprunner.utilities.NetUtils;

public class AddSiteActivity extends AppCompatActivity {

    private Uri contacts_uri = Uri.parse("content://tw.daychen.app.maprunner/site/");
    private AddSiteNetworkTask mAddSiteTask = null;
    private double mLat;
    private double mLng;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_site);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> lunchList = ArrayAdapter.createFromResource(this,
                R.array.class_name,
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(lunchList);
        Intent intent = getIntent();
        setResult(3, intent); //requestCode需跟A.class的一樣
        mLat = intent.getDoubleExtra("lat", 0.0);
        mLng = intent.getDoubleExtra("long", 0.0);
        username = intent.getStringExtra("username");
        String latlong = String.valueOf(intent.getDoubleExtra("lat", 0.0)) + "," + String.valueOf(intent.getDoubleExtra("long", 0.0));
        TextView latlong_show = (TextView) findViewById(R.id.latlang_show);
        latlong_show.setText(latlong);
        Button btn_no = (Button) findViewById(R.id.button_no);
        Button btn_yes = (Button) findViewById(R.id.button_yes);
        btn_no.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddSiteActivity.this.finish();
            }
        });
        btn_yes.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                check_data();
            }
        });
    }

    public class AddSiteNetworkTask extends AsyncTask<Void, Void, Boolean> {

        private final double mLongitude;
        private final double mLatitude;
        private final int mRange;
        private final String mSiteClass;
        private final String mTitle;
        private final String mContent;
        private final URL mUrl;
        private int serverID = -1;

        AddSiteNetworkTask(double longitude, double latitude, int range, String siteClass, String title, String content, URL url) {
            mLongitude = longitude;
            mLatitude = latitude;
            mRange = range;
            mSiteClass = siteClass;
            mTitle = title;
            mContent = content;
            mUrl = url;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // TODO: attempt authentication against a network service.
            String searchResult;
            try {
                HashMap<String, String> postData = new HashMap<>();
                postData.put("title", mTitle);
                postData.put("content", mContent);
                postData.put("site_class", mSiteClass);
                postData.put("latitude", String.valueOf(mLatitude));
                postData.put("longitude", String.valueOf(mLongitude));
                postData.put("range", String.valueOf(mRange));
                searchResult = NetUtils.getResponseFromAccessCode(mUrl, "POST", postData);
                try {
                    serverID = JsonUtils.getServerIDFromJson(AddSiteActivity.this, searchResult);
                } catch (Exception e) {
                    serverID = -1;
                    e.printStackTrace();
                }
                Log.d("Addsite", searchResult);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAddSiteTask = null;

            if (success) {
//                insertData();
                if (serverID > 0){
                    insertData(serverID);
                }
                finish();
            } else {
                Button mButton = (Button) findViewById(R.id.button_yes);
                mButton.setError(getString(R.string.error_network));
                mButton.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAddSiteTask = null;
        }
    }

    private void check_data() {
        EditText title = (EditText) findViewById(R.id.editTitle);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String cla = spinner.getSelectedItem().toString();
        EditText content = (EditText) findViewById(R.id.editContent);
        TextView latlng = (TextView) findViewById(R.id.latlang_show);
        if (title.getText().length() == 0) {
            Toast.makeText(this, "請填寫標題。", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (content.getText().length() == 0) {
            Toast.makeText(this, "請填寫內容。", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        String urlQuery = "maprunner/site/";
        URL Url = NetUtils.buildUrl(urlQuery, AddSiteActivity.this);
        mAddSiteTask = new AddSiteNetworkTask(mLng, mLat, 0, cla, title.getText().toString(), content.getText().toString(), Url);
        mAddSiteTask.execute((Void) null);
    }

    private void insertData(int serverID) {
        EditText title = (EditText) findViewById(R.id.editTitle);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String cla = spinner.getSelectedItem().toString();
        EditText content = (EditText) findViewById(R.id.editContent);
        TextView latlng = (TextView) findViewById(R.id.latlang_show);

        ContentValues[] cv_list = new ContentValues[1];
        cv_list[0] = new ContentValues();
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_TITLE, title.getText().toString());
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_USERNAME, username);
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_CLASS, cla);
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_LATLNG, latlng.getText().toString());
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_CONTENT, content.getText().toString());
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_RANGE, 100);
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_SERVER_ID, serverID);

        getContentResolver().bulkInsert(contacts_uri, cv_list);
    }
}