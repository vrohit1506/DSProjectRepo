package com.hello.firstapplication;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class DeviceNameActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_name, menu);
        return true;
    }
}
