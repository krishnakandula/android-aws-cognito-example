package com.canvas.krishna.awscognitotest;

import android.app.Application;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Region;
import com.amazonaws.services.cognitoidentityprovider.AmazonCognitoIdentityProviderClient;

/**
 * Created by Krishna Chaitanya Kandula on 9/1/17.
 */

public class MainApplication extends Application {

    private static final String LOG_TAG = MainApplication.class.getSimpleName();

    private IdentityManager mIdentityManager;
    private CognitoUserPool mCognitoUserPool;
    private AmazonCognitoIdentityProviderClient mAmazonCognitoIdentityProviderClient;
    private AWSCredentials mAWSCredentials;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeApplication();
    }

    private void initializeApplication() {
        AWSConfiguration awsConfiguration = new AWSConfiguration(this);

        //Create identity manager
        if (mIdentityManager== null) {
            mIdentityManager= new IdentityManager(this, awsConfiguration);
            IdentityManager.setDefaultIdentityManager(mIdentityManager);
        }
    }

    public IdentityManager getIdentityManager() {
        return mIdentityManager;
    }

    public CognitoUserPool getCognitoUserPool() {
        if (mCognitoUserPool == null) {
            mCognitoUserPool = new CognitoUserPool(this,
                    Constants.AWS_POOL_ID,
                    Constants.AWS_CLIENT_ID,
                    Constants.AWS_CLIENT_SECRET,
                    getCognitoIdentityProviderClient());
        }

        return mCognitoUserPool;
    }

    private AmazonCognitoIdentityProviderClient getCognitoIdentityProviderClient() {
        if(mAmazonCognitoIdentityProviderClient == null) {
            mAmazonCognitoIdentityProviderClient = new AmazonCognitoIdentityProviderClient(getAwsCredentials());
            mAmazonCognitoIdentityProviderClient.setRegion(Region.getRegion("us-west-2"));
        }
        return mAmazonCognitoIdentityProviderClient;
    }

    private AWSCredentials getAwsCredentials() {
        if(mAWSCredentials == null) {
            mAWSCredentials = new AWSCredentials() {
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

        return mAWSCredentials;
    }
}
