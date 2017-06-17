package tw.daychen.app.maprunner;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import tw.daychen.app.maprunner.data.MapRunnerContract;
import tw.daychen.app.maprunner.data.MapRunnerProvider;

public class SiteDetailActivity extends AppCompatActivity {

    private TextView title;
    private TextView content;
//    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_detail);

        title = (TextView) findViewById(R.id.tv_title);
        content = (TextView) findViewById(R.id.tv_content);

        Log.d("SiteActivity", getIntent().getStringExtra("marker_id"));

        String markerId = getIntent().getStringExtra("marker_id");

        Uri uri = MapRunnerContract.BASE_CONTENT_URI;
        uri = uri.buildUpon()
                .appendPath(MapRunnerContract.PATH_Site)
                .appendPath(markerId).build();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            title.setText(cursor.getString(cursor.getColumnIndex(MapRunnerContract.SiteEntry.COLUMN_TITLE)));
            content.setText(cursor.getString(cursor.getColumnIndex(MapRunnerContract.SiteEntry.COLUMN_CONTENT)));
        } else {
            Log.e("cursor", "cursor is null");
        }
    }

    public void closeDetail(View view) {
        finish();
    }
}
