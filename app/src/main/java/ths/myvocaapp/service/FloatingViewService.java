package ths.myvocaapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import ths.myvocaapp.utility.FloatingViewManager;

/**
 * Created by HungS7 on 1/26/2018.
 */

public class FloatingViewService extends Service {
    private FloatingViewManager fviewManager;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        fviewManager = new FloatingViewManager(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fviewManager.removeFloatingView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        fviewManager.clearVocaList();
    }
}
