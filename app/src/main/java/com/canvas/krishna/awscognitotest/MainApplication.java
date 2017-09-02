package com.canvas.krishna.awscognitotest;

import android.app.Application;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.config.AWSConfiguration;

/**
 * Created by Krishna Chaitanya Kandula on 9/1/17.
 */

public class MainApplication extends Application {

    private static final String LOG_TAG = MainApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        initializeApplication();
    }

    private void initializeApplication() {
        AWSConfiguration awsConfiguration = new AWSConfiguration(this);

        //Create identity manager
        if (IdentityManager.getDefaultIdentityManager() == null) {
            IdentityManager identityManager = new IdentityManager(this, awsConfiguration);
            IdentityManager.setDefaultIdentityManager(identityManager);
        }
    }
}
