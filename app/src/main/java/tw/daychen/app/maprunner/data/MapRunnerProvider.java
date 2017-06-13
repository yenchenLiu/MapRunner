package tw.daychen.app.maprunner.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by daychen on 2017/6/4.
 */

public class MapRunnerProvider extends ContentProvider {
    private static final String LOG_TAG ="ContentProvider";
    public static final int CODE_SETTING = 100;
    public static final int CODE_SITE = 101;
    public static final int CODE_SITEN2M = 102;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private MapRunnerDbHelper mOpenHelper;

    public static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MapRunnerContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MapRunnerContract.PATH_Setting, CODE_SETTING);
        matcher.addURI(authority, MapRunnerContract.PATH_Site, CODE_SITE);
        matcher.addURI(authority, MapRunnerContract.PATH_SiteN2M, CODE_SITEN2M);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate");
        mOpenHelper = new MapRunnerDbHelper(getContext());
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        Log.d(LOG_TAG, "bulkInsert");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {

            case CODE_SETTING:
                return setting_insert(db, uri, values);
            case CODE_SITE:
                return site_insert(db, uri, values);
            case CODE_SITEN2M:
                return siten2m_insert(db, uri, values);
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private int setting_insert(SQLiteDatabase db, @NonNull Uri uri, @NonNull ContentValues[] values) {
        db.beginTransaction();
        int rowsInserted = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(MapRunnerContract.SettingEntry.TABLE_NAME, null, value);
                if (_id != -1) {
                    rowsInserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsInserted;
    }

    private int site_insert(SQLiteDatabase db, @NonNull Uri uri, @NonNull ContentValues[] values) {
        db.beginTransaction();
        int rowsInserted = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(MapRunnerContract.SiteEntry.TABLE_NAME, null, value);
                if (_id != -1) {
                    rowsInserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsInserted;
    }

    private int siten2m_insert(SQLiteDatabase db, @NonNull Uri uri, @NonNull ContentValues[] values) {
        db.beginTransaction();
        int rowsInserted = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(MapRunnerContract.SiteN2MEntry.TABLE_NAME, null, value);
                if (_id != -1) {
                    rowsInserted++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (rowsInserted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsInserted;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query");
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_SETTING: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        MapRunnerContract.SettingEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        MapRunnerContract.SettingEntry._ID
                );
                break;
            }
            case CODE_SITE: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        MapRunnerContract.SiteEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        MapRunnerContract.SiteEntry._ID
                );
                break;
            }
            case CODE_SITEN2M: {
                cursor = mOpenHelper.getReadableDatabase().query(
                        MapRunnerContract.SiteN2MEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        MapRunnerContract.SiteN2MEntry._ID
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete");
        int numRowsDeleted;
        if (null == selection) selection = "1";
        switch (sUriMatcher.match(uri)) {

            case CODE_SETTING:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MapRunnerContract.SettingEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;
            case CODE_SITE:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MapRunnerContract.SiteEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;
            case CODE_SITEN2M:
                numRowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        MapRunnerContract.SiteN2MEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("We are not implementing getType in MapRunner.");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new RuntimeException(
                "We are not implementing insert in MapRunner. Use bulkInsert instead");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int numRowsUpdated;
        if (null == selection) selection = "1";
        switch (sUriMatcher.match(uri)) {

            case CODE_SETTING:
                numRowsUpdated = mOpenHelper.getWritableDatabase().update(
                        MapRunnerContract.SettingEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);

                break;
            case CODE_SITE:
                numRowsUpdated = mOpenHelper.getWritableDatabase().update(
                        MapRunnerContract.SiteEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);

                break;
            case CODE_SITEN2M:
                numRowsUpdated = mOpenHelper.getWritableDatabase().update(
                        MapRunnerContract.SiteN2MEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsUpdated;
    }
}
