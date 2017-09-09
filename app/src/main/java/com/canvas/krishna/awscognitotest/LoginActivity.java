package com.canvas.krishna.awscognitotest;

import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUnbinder = ButterKnife.bind(this);
    }

    private ClientConfiguration getAwsClientConfiguration() {
        return new ClientConfiguration();
    }

    private AWSCredentials getAwsCredentials() {
        return new AWSCredentials() {
            @Override
            public String getAWSAccessKeyId() {
                return Constants.AWS_ACCESS_KEY;
            }

            @Override
            public String getAWSSecretKey() {
                return Constants.AWS_SECRET_KEY;
            }
        };
    }

    private AmazonCognitoIdentityProviderClient getCognitoIdentityProviderClient() {
        AmazonCognitoIdentityProviderClient cognitoIdentityProviderClient = new AmazonCognitoIdentityProviderClient(getAwsCredentials());
        cognitoIdentityProviderClient.setRegion(Region.getRegion("us-west-2"));
        return cognitoIdentityProviderClient;
    }

    private CognitoUserPool getCognitoUserPool() {
        return new CognitoUserPool(this,
                Constants.AWS_POOL_ID,
                Constants.AWS_CLIENT_ID,
                Constants.AWS_CLIENT_SECRET,
                getCognitoIdentityProviderClient());
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
                Log.i(LOG_TAG, String.format("User with id %s successfully signed up!", user.getUserId()));
                emitter.onSuccess(user);
            }

            @Override
            public void onFailure(Exception exception) {
                Log.i(LOG_TAG, "Could not sign up user.");
                emitter.onError(exception);
            }
        });
    }

    @OnClick(R.id.loginBtn_loginActivity)
    public void onClickLoginButton() {
        final String username = usernameEditText.getText().toString();
        final String password = passwordEditText.getText().toString();

        Single<CognitoUser> userSingle = Single.create(emitter -> signUpUser(username, password, getCognitoUserPool(), getCognitoUserAttributes(), emitter));
        userSingle.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((cognitoUser, throwable) -> {
                    if (cognitoUser != null) {
                        Snackbar.make(LoginActivity.this.findViewById(R.id.parent_loginActivity),
                                String.format("Created user %s", cognitoUser.getUserId()),
                                Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
