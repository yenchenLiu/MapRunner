package tw.daychen.app.maprunner;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import tw.daychen.app.maprunner.data.MapRunnerContract;

public class AddSiteActivity extends AppCompatActivity {

    private Uri contacts_uri = Uri.parse("content://tw.daychen.app.maprunner/site/");

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

        ContentValues[] cv_list = new ContentValues[1];
        cv_list[0] = new ContentValues();
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_TITLE, title.getText().toString());
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_CLASS, cla);
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_LATLNG, latlng.getText().toString());
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_CONTENT, content.getText().toString());
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_RANGE, 100);
        cv_list[0].put(MapRunnerContract.SiteEntry.COLUMN_SERVER_ID, "test");

        getContentResolver().bulkInsert(contacts_uri, cv_list);
        this.finish();
    }
}