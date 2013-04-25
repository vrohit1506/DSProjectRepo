package com.hello.firstapplication;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.widget.ImageView;

public class ViewEvent extends Activity {

	MySQLiteHelper db;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);
        
        Try db = (Try)getIntent().getSerializableExtra("Database");
        
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		//String fileName  = baseDir + File.separator + String.valueOf(e.getEvent_id()) + File.separator + "ic_launcher.png";
		String fileName  = baseDir + File.separator + "ic_launcher.png";
		
		ImageView pngView = (ImageView)findViewById(R.id.imageView1);
		Bitmap bm = BitmapFactory.decodeFile(fileName);
		pngView.setImageBitmap(bm);	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_view_event, menu);
        return true;
    }
}
