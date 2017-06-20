package tw.daychen.app.maprunner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tw.daychen.app.maprunner.data.MapRunnerContract;
import tw.daychen.app.maprunner.utilities.JsonUtils;
import tw.daychen.app.maprunner.utilities.NetUtils;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = "LoginActivity";
    private static int loginCODE = 2;
    private Uri contacts_uri = Uri.parse("content://tw.daychen.app.maprunner/setting/");
    private static final String ACCESS_TOKEN_STR = "server_access_token";
    private static final String USERNAME_STR = "username";


    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private SignInTestTask mSignInTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.username);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        showProgress(true);
        mSignInTask = new SignInTestTask();
        mSignInTask.execute((Void) null);
    }

    private boolean tryAccessCode() {

        return false;
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            String urlQuery = "auth/token";
            URL Url = NetUtils.buildUrl(urlQuery, LoginActivity.this);
            mAuthTask = new UserLoginTask(email, password, Url);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class SignInTestTask extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... params) {
            String searchResult = null;
            String urlQuery = "maprunner/users/get_username/";
            URL Url = NetUtils.buildUrl(urlQuery, LoginActivity.this);
            try {
                searchResult = NetUtils.getResponseFromAccessCode(Url, "GET", null);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            if (searchResult == null){
                return false;
            }
            String username;
            try {
                username = JsonUtils.getUsernameFromJson(LoginActivity.this, searchResult);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            if (username != null){
                return true;
            }
            return false;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            mSignInTask = null;
            showProgress(false);

            if (success) {
                setResult(loginCODE, getIntent());
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            mSignInTask = null;
            showProgress(false);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final URL mUrl;

        UserLoginTask(String email, String password, URL url) {
            mEmail = email;
            mPassword = password;
            mUrl = url;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String searchResult;
            try {
                HashMap<String, String> userpass = new HashMap<>();
                userpass.put("username", mEmail);
                userpass.put("password", mPassword);
                searchResult = NetUtils.getResponseFromLogin(mUrl, userpass);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            if (searchResult == null){
                return false;
            }
            Log.d(LOG_TAG, searchResult);
            HashMap<String, String> loginData = null;
            try {
                loginData = JsonUtils.getLoginDataFromJson(LoginActivity.this, searchResult);
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
            String selection = "key=?";
            String[] selectionArgs = new String[1];
            selectionArgs[0] = ACCESS_TOKEN_STR;

            try (Cursor cursor = getContentResolver().query(contacts_uri, null, selection, selectionArgs, null)) {


                Log.d(LOG_TAG, String.valueOf(cursor.getCount()));
                if (cursor.getCount() == 0){
                    ContentValues[] cv_list = new ContentValues[1];
                    cv_list[0] = new ContentValues();
                    cv_list[0].put(MapRunnerContract.SettingEntry.COLUMN_KEY, ACCESS_TOKEN_STR);
                    cv_list[0].put(MapRunnerContract.SettingEntry.COLUMN_VALUE, loginData.get("access_token"));
                    getContentResolver().bulkInsert(contacts_uri, cv_list);
                }
                else {
                    cursor.moveToFirst();
                    int id = cursor.getInt(cursor.getColumnIndex(MapRunnerContract.SettingEntry._ID));
                    String[] selectionArgsId = new String[1];
                    selectionArgsId[0] = String.valueOf(id);

                    ContentValues cv = new ContentValues();
                    cv.put(MapRunnerContract.SettingEntry.COLUMN_VALUE, loginData.get("access_token"));
                    getContentResolver().update(contacts_uri, cv, MapRunnerContract.SettingEntry._ID +" = " + String.valueOf(id), null);
                }
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
            // get Username
            try {
                String urlQuery = "maprunner/users/get_username/";
                URL Url = NetUtils.buildUrl(urlQuery, LoginActivity.this);
                searchResult = NetUtils.getResponseFromAccessCode(Url, "GET", null);
                String username;
                username = JsonUtils.getUsernameFromJson(LoginActivity.this, searchResult);
                selectionArgs[0] = USERNAME_STR;
                try (Cursor cursor = getContentResolver().query(contacts_uri, null, selection, selectionArgs, null)) {


                    Log.d(LOG_TAG, "username_count:" + String.valueOf(cursor.getCount()));
                    if (cursor.getCount() == 0){
                        ContentValues[] cv_list = new ContentValues[1];
                        cv_list[0] = new ContentValues();
                        cv_list[0].put(MapRunnerContract.SettingEntry.COLUMN_KEY, USERNAME_STR);
                        cv_list[0].put(MapRunnerContract.SettingEntry.COLUMN_VALUE, username);
                        getContentResolver().bulkInsert(contacts_uri, cv_list);
                    }
                    else {
                        cursor.moveToFirst();
                        Log.d(LOG_TAG, "key:" + cursor.getString(cursor.getColumnIndex(MapRunnerContract.SettingEntry.COLUMN_KEY)));
                        Log.d(LOG_TAG, "value:" + cursor.getString(cursor.getColumnIndex(MapRunnerContract.SettingEntry.COLUMN_VALUE)));
                        int id = cursor.getInt(cursor.getColumnIndex(MapRunnerContract.SettingEntry._ID));
                        String[] selectionArgsId = new String[1];
                        selectionArgsId[0] = String.valueOf(id);

                        ContentValues cv = new ContentValues();
                        cv.put(MapRunnerContract.SettingEntry.COLUMN_VALUE, username);
                        getContentResolver().update(contacts_uri, cv, MapRunnerContract.SettingEntry._ID +" = " + String.valueOf(id), null);
                    }
                }

            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                setResult(loginCODE, getIntent());
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

