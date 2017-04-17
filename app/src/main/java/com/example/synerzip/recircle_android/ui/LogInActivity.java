package com.example.synerzip.recircle_android.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.synerzip.recircle_android.R;
import com.example.synerzip.recircle_android.models.LogInRequest;
import com.example.synerzip.recircle_android.models.User;
import com.example.synerzip.recircle_android.network.ApiClient;
import com.example.synerzip.recircle_android.network.RCAPInterface;
import com.example.synerzip.recircle_android.utilities.AESEncryptionDecryption;
import com.example.synerzip.recircle_android.utilities.HideKeyboard;
import com.example.synerzip.recircle_android.utilities.NetworkUtility;
import com.example.synerzip.recircle_android.utilities.RCLog;
import com.example.synerzip.recircle_android.utilities.RCWebConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Prajakta Patil on 3/4/17.
 * Copyright © 2016 Synerzip. All rights reserved
 */
public class LogInActivity extends AppCompatActivity {

    @BindView(R.id.edit_login_email)
    public EditText mEditLogInEmail;

    @BindView(R.id.edit_login_pwd)
    public EditText mEditLogInPassword;

    @BindView(R.id.input_layout_email)
    public TextInputLayout mInputLayoutEmail;

    @BindView(R.id.input_layout_pwd)
    public TextInputLayout mInputLayoutPassword;

    private String mUserName, mPassword;

    private RCAPInterface service;

    @BindView(R.id.toolbar)
    public Toolbar mToolbar;

    String mEmail;

    @BindView(R.id.progress_bar)
    public RelativeLayout mProgressBar;

    @BindView(R.id.linear_layout)
    public LinearLayout mLinearLayout;

    public String mUserId, mUserEmail,mUserToken,mUserLastName,mUserFirstName,mAccessToken="";

    private static final String TAG = "LogInActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.log_in);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.common_white));

        mEditLogInEmail.addTextChangedListener(new MyTextWatcher(mEditLogInEmail));
        mEditLogInPassword.addTextChangedListener(new MyTextWatcher(mEditLogInPassword));
    }

    /**
     * login submit button
     *
     * @param v
     */
    @OnClick(R.id.btn_user_log_in)
    public void btnLogIn(View v) {

        submitForm();
        HideKeyboard.hideKeyBoard(LogInActivity.this);
        if (NetworkUtility.isNetworkAvailable(this)) {
            mProgressBar.setVisibility(View.VISIBLE);
            mLinearLayout.setAlpha((float) 0.6);
            mUserName = mEditLogInEmail.getText().toString().trim();
            mPassword = mEditLogInPassword.getText().toString().trim();
            getUserLogIn();

        } else {
            mProgressBar.setVisibility(View.GONE);
            mLinearLayout.setAlpha((float) 1.0);

            RCLog.showToast(this, getResources().getString(R.string.err_network_available));
        }
    }

    /**
     * api call for user login
     */
    private void getUserLogIn() {

        LogInRequest logInRequest = new LogInRequest(mUserName, mPassword);

        service = ApiClient.getClient().create(RCAPInterface.class);
        Call<User> userCall = service.userLogIn(logInRequest);
        userCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                mProgressBar.setVisibility(View.GONE);
                mLinearLayout.setAlpha((float) 1.0);

                if(response.isSuccessful()){

                        mUserId = response.body().getUser_id();
                        mUserName = response.body().getEmail();
                        mUserToken = response.body().getToken();
                        mUserFirstName = response.body().getFirst_name();
                        mUserLastName = response.body().getLast_name();
                        mAccessToken=response.body().getToken();
                        Log.v(TAG,mAccessToken);
                    if(null!=mUserId && null!=mUserName && null!=mUserToken &&
                            null!=mUserFirstName && null!=mUserLastName && null!=mAccessToken) {

                        saveUserData();
                        Intent intent = new Intent(LogInActivity.this, SearchActivity.class);
                        startActivity(intent);
                    }else{
                        RCLog.showToast(LogInActivity.this, getString(R.string.please_try_again));
                    }
                }else {
                    if (response.code() == RCWebConstants.RC_ERROR_UNAUTHORISED) {
                        RCLog.showToast(LogInActivity.this, getString(R.string.err_credentials));
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                mProgressBar.setVisibility(View.GONE);
                mLinearLayout.setAlpha((float) 1.0);
            }
        });
    }

    /**
     * textview for create account
     *
     * @param view
     */
    @OnClick(R.id.txt_create_acc)
    public void txtCreateAcc(View view) {
        startActivity(new Intent(LogInActivity.this, SignUpActivity.class));
    }

    /**
     * forgot password textview
     *
     * @param view
     */
    @OnClick(R.id.txt_forgot_pwd)
    public void txtForgotPwd(View view) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.forgot_pwd_dialog);
        dialog.setTitle(getString(R.string.reset_pwd));

        final EditText mEditTxtEmail = (EditText) dialog.findViewById(R.id.edit_forgot_pwd_otp_email);
        mEditTxtEmail.getText().toString();

        Button btnSendOtp = (Button) dialog.findViewById(R.id.btn_send_otp);
        btnSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                mLinearLayout.setAlpha((float) 0.6);
                mEmail = mEditTxtEmail.getText().toString();

                service = ApiClient.getClient().create(RCAPInterface.class);
                Call<User> call = service.otpForgotPassword(mEmail);
                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {

                        mProgressBar.setVisibility(View.GONE);
                        mLinearLayout.setAlpha((float) 1.0);
                        RCLog.showToast(getApplicationContext(), getString(R.string.toast_send_otp));
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if(null!=mEmail) {
                                startActivity(new Intent(LogInActivity.this, ForgotPwdActivity.class));
                            }else {
                                RCLog.showToast(LogInActivity.this, getString(R.string.please_try_again));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        mProgressBar.setVisibility(View.GONE);
                        mLinearLayout.setAlpha((float) 1.0);
                    }
                });
            }
        });

        dialog.show();
    }

    /**
     * Validating form
     */
    private void submitForm() {

        if (!validateEmail()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }
    }

    /**
     * validate email
     *
     * @return
     */
    private boolean validateEmail() {
        String email = mEditLogInEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            mInputLayoutEmail.setError(getString(R.string.validate_email));
            requestFocus(mEditLogInEmail);
            return false;
        } else {
            mInputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * validate password
     *
     * @return
     */
    private boolean validatePassword() {
        if (mEditLogInPassword.getText().toString().trim().isEmpty() || mEditLogInPassword.length() < 8) {
            mInputLayoutPassword.setError(getString(R.string.validate_pwd));
            requestFocus(mEditLogInPassword);
            return false;
        } else {
            mInputLayoutPassword.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.edit_login_email:
                    validateEmail();
                    break;

                case R.id.edit_login_pwd:
                    validatePassword();
                    break;
            }
        }
    }

    /**
     * action bar back button
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /**
     * saves user password in encrypted format in Shared Preferences
     */
    private void saveUserData() {

        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        SharedPreferences sharedPreferences = getSharedPreferences(RCWebConstants.RC_SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        try {
            String encryptedPassword = AESEncryptionDecryption.encrypt(android_id, mPassword);
            editor.putString(RCWebConstants.RC_SHARED_PREFERENCES_ACCESS_TOKEN, mUserToken);
            editor.putString(RCWebConstants.RC_SHARED_PREFERENCES_USERID, mUserId);
            editor.putString(RCWebConstants.RC_SHARED_PREFERENCES_PASSWORD, encryptedPassword);
            editor.putBoolean(RCWebConstants.RC_SHARED_PREFERENCES_LOGIN_STATUS, true);
            editor.putString(RCWebConstants.RC_SHARED_PREFERENCES_LOGIN_USERNAME,mUserEmail);
            editor.putString(RCWebConstants.RC_SHARED_PREFERENCES_LOGIN_FIRST_USERNAME,mUserFirstName);

            editor.apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}