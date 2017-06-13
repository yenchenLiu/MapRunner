package tw.daychen.app.maprunner.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import tw.daychen.app.maprunner.data.MapRunnerContract.SettingEntry;
import tw.daychen.app.maprunner.data.MapRunnerContract.SiteEntry;
import tw.daychen.app.maprunner.data.MapRunnerContract.SiteN2MEntry;
/**
 * Created by daychen on 2017/6/3.
 */

public class MapRunnerDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG ="DbHelper";
    public static final String DATABASE_NAME = "maprunner.db";
    private static final int DATABASE_VERSION = 1;

    public MapRunnerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(LOG_TAG, "onCreate");
        final String SQL_CREATE_Setting_TABLE =
                "CREATE TABLE " + SettingEntry.TABLE_NAME + " (" +
                        SettingEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SettingEntry.COLUMN_KEY        + " TEXT NOT NULL, "                 +
                        SettingEntry.COLUMN_VALUE      + " TEXT NOT NULL" + ");";

        final String SQL_CREATE_Site_TABLE =
                "CREATE TABLE " + SiteEntry.TABLE_NAME + " (" +
                        SiteEntry._ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SiteEntry.COLUMN_SERVER_ID     + " TEXT NOT NULL, "                 +
                        SiteEntry.COLUMN_TITLE         + " TEXT NOT NULL, "                 +
                        SiteEntry.COLUMN_CONTENT       + " TEXT NOT NULL, "                 +
                        SiteEntry.COLUMN_CLASS         + " TEXT NOT NULL, "                 +
                        SiteEntry.COLUMN_LATLNG        + " TEXT NOT NULL, "                 +
                        SiteEntry.COLUMN_RANGE         + " INTEGER NOT NULL"+ ");";

        final String SQL_CREATE_SiteN2M_TABLE =
                "CREATE TABLE " + SiteN2MEntry.TABLE_NAME + " (" +
                        SiteN2MEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SiteN2MEntry.COLUMN_SITE       + " TEXT NOT NULL, "                 +
                        SiteN2MEntry.COLUMN_NEED       + " TEXT NOT NULL" + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_Setting_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_Site_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SiteN2M_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SettingEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SiteEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SiteN2MEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
