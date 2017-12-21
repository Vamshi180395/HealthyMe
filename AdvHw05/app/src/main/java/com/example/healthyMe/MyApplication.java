package com.example.healthyMe;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import org.researchstack.backbone.StorageAccess;
import org.researchstack.backbone.storage.database.AppDatabase;
import org.researchstack.backbone.storage.database.sqlite.DatabaseHelper;
import org.researchstack.backbone.storage.file.EncryptionProvider;
import org.researchstack.backbone.storage.file.FileAccess;
import org.researchstack.backbone.storage.file.PinCodeConfig;
import org.researchstack.backbone.storage.file.SimpleFileAccess;
import org.researchstack.backbone.storage.file.UnencryptedProvider;

/**
 * Created by Rama Vamshi Krishna on 11/14/2017.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        loadResearchStackConfiguration();
    }

    private void loadResearchStackConfiguration() {
        // Customize encryption preferences
        PinCodeConfig pinCodeConfig = new PinCodeConfig();

        EncryptionProvider encryptionProvider = new UnencryptedProvider();

        FileAccess fileAccess = new SimpleFileAccess();

        AppDatabase database = new DatabaseHelper(this,
                DatabaseHelper.DEFAULT_NAME,
                null,
                DatabaseHelper.DEFAULT_VERSION);

        StorageAccess.getInstance().init(pinCodeConfig, encryptionProvider, fileAccess, database);
    }
}
