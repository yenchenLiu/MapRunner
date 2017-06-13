package tw.daychen.app.maprunner.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by daychen on 2017/6/3.
 */

public class MapRunnerContract {

    private static final String LOG_TAG ="Contract";

    public static final String CONTENT_AUTHORITY = "tw.daychen.app.maprunner";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_Setting = "setting";
    public static final String PATH_Site = "site";
    public static final String PATH_SiteN2M = "siten2m";

    public static final class SettingEntry implements BaseColumns {
        public static final String TABLE_NAME = "Setting";

        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_VALUE = "value";
    }

    public static final class SiteEntry implements BaseColumns {
        public static final String TABLE_NAME = "Site";

        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CLASS = "class";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_LATLNG = "latlng";
        public static final String COLUMN_RANGE = "range";
    }

    public static final class SiteN2MEntry implements BaseColumns {
        public static final String TABLE_NAME = "SiteN2M";

        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_NEED = "need";
    }
}
