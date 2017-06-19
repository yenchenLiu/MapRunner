package tw.daychen.app.maprunner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SiteDetailActivity extends AppCompatActivity {

    private TextView title;
    private TextView content;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_detail);
        intent = getIntent();
        title = (TextView) findViewById(R.id.tv_title);
        content = (TextView) findViewById(R.id.tv_content);

        Log.d("SiteActivity", intent.getStringExtra("id"));

        String markerId = intent.getStringExtra("id");
        title.setText(intent.getStringExtra("title"));
        content.setText(intent.getStringExtra("content"));
    }

    public void closeDetail(View view) {
        finish();
    }
}
