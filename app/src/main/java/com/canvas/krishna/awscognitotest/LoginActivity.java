package com.canvas.krishna.awscognitotest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.usernameEditText_loginActivity) TextInputEditText usernameEditText;
    @BindView(R.id.passwordEditText_loginActivity) TextInputEditText passwordEditText;
    @BindView(R.id.emailEditText_loginActivity) TextInputEditText emailEditText;
    @BindView(R.id.loginBtn_loginActivity) Button loginBtn;

    private Unbinder mUnbinder;
    private CognitoUser mCognitoUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUnbinder = ButterKnife.bind(this);
    }

    private ClientConfiguration getAwsClientConfiguration() {
        return new ClientConfiguration();
    }

    private CognitoUserPool getCognitoUserPool() {
        return ((MainApplication) getApplication()).getCognitoUserPool();
    }

    private CognitoUserAttributes getCognitoUserAttributes() {
        final String email = emailEditText.getText().toString();

        CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        userAttributes.addAttribute("email", email);
        return userAttributes;
    }

    private void signUpUser(String userId, String password, CognitoUserPool userPool, CognitoUserAttributes userAttributes, SingleEmitter<CognitoUser> emitter) {
        userPool.signUp(userId, password, userAttributes, null, new SignUpHandler() {
            @Override
            public void onSuccess(CognitoUser user, boolean signUpConfirmationState, CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                if (!signUpConfirmationState) {
                    confirmUser(user);
                }

                Log.i(LOG_TAG, String.format("User with id %s successfully signed up!", user.getUserId()));
                emitter.onSuccess(user);
            }

            @Override
            public void onFailure(Exception exception) {
                Log.i(LOG_TAG, "Could not sign up user.");
                Log.e(LOG_TAG, exception.getMessage());
                emitter.onError(exception);
            }
        });
    }

    @OnClick(R.id.loginBtn_loginActivity)
    public void onClickLoginButton() {
        final String username = usernameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        final CognitoUserPool cognitoUserPool = getCognitoUserPool();

        Single<CognitoUser> userSingle = Single.create(emitter -> signUpUser(username, password, cognitoUserPool, getCognitoUserAttributes(), emitter));
        userSingle.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((cognitoUser, throwable) -> {
                    if (cognitoUser != null) {
                        Snackbar.make(LoginActivity.this.findViewById(R.id.parent_loginActivity),
                                String.format("Created user %s", cognitoUser.getUserId()),
                                Snackbar.LENGTH_SHORT)
                                .show();
                        Log.d(LOG_TAG, "Current user is: " + cognitoUserPool.getCurrentUser().getUserId());
                        mCognitoUser = cognitoUser;
                    }
                });
    }

    private void confirmUser(CognitoUser user) {
        Intent intent = new Intent(this, ConfirmSignInActivity.class);
        startActivityForResult(intent, ConfirmSignInActivity.SIGN_IN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ConfirmSignInActivity.SIGN_IN_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                //Confirm Sign in
                final String confirmationCode = data.getStringExtra(ConfirmSignInActivity.SIGN_IN_CODE_KEY);
                confirmUserSignIn(confirmationCode, mCognitoUser);
            }
        }
    }

    private void confirmUserSignIn(final String confirmationCode, final CognitoUser cognitoUser) {
        boolean forcedAliasCreation = false;
        Completable confirmSignInCompletable = Completable.create(emitter -> {
            cognitoUser.confirmSignUp(confirmationCode, forcedAliasCreation, new GenericHandler() {
                @Override
                public void onSuccess() {
                    emitter.onComplete();
                }

                @Override
                public void onFailure(Exception exception) {
                    emitter.onError(exception);
                }
            });
        });

        confirmSignInCompletable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Toast.makeText(LoginActivity.this, "User was signed in", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
