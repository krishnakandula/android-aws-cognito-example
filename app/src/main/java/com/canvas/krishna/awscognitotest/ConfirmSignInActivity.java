package com.canvas.krishna.awscognitotest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfirmSignInActivity extends AppCompatActivity {

    public static final int SIGN_IN_REQUEST_CODE = 1;
    public static final String SIGN_IN_CODE_KEY = "SIGN_IN_CODE_KEY";

    private static final String LOG_TAG = ConfirmSignInActivity.class.getSimpleName();

    @BindView(R.id.confirmationCodeEditText_confirmSignInActivity) EditText confirmSignOnEditText;
    @BindView(R.id.confirmationCodeConfirmBtn_confirmSignInActivity) Button confirmSignOnBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_sign_in);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.confirmationCodeConfirmBtn_confirmSignInActivity)
    public void onClickConfirmSignOnBtn() {
        Log.d(LOG_TAG, "Onclick pressed");
        final String confirmationCode = confirmSignOnEditText.getText().toString();
        Intent returnIntent = new Intent();
        returnIntent.putExtra(SIGN_IN_CODE_KEY, confirmationCode);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
