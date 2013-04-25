package com.hello.firstapplication;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	ArrayAdapter<String> listAdapter;
	ListView listView;
	MySQLiteHelper db;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        listAdapter = new ArrayAdapter<String>(this, R.layout.device_name, R.id.TextView1);
		listView = (ListView) findViewById(R.id.listView1);
		listView.setAdapter(listAdapter);
		
		
		db = new MySQLiteHelper(getApplicationContext());
		Log.d("DEBUG","After creating");
		db.add_event(new Event("Ruchi", 1000, "Ruchi",02),getApplicationContext());
		
		File direct = new File(Environment.getExternalStorageDirectory() + "/1000");
			Log.d("DEBUG",direct.getAbsolutePath());
		
		   if(!direct.exists())
		    {
		        
			   if(direct.mkdir()) 
		          {
		           Log.d("DEBUG","Directory made");
		          }

		    }
	
		   ArrayList<Event> events = db.get_event_list(getApplicationContext());
		
		for(int i = 0;i<events.size();i++)
			listAdapter.add(events.get(i).getName());
    
		listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				
				Log.e("DEBUG","Onclick");
				//Log.d("DEBUG",Environment.getExternalStorageDirectory().getAbsolutePath());
				//Log.d("DEBUG",((TextView)arg1).toString() + String.valueOf(arg2) +String.valueOf(arg3));
				
				Intent intent = new Intent(MainActivity.this, DatabaseTrial.class);
				Try t = new Try();
				//intent.putExtra("Database",t);
				startActivity(intent);
						
				
//				TextView tv = (TextView) arg1;
//				Log.d("DEBUG", tv.getText().toString());
//				Event e = db.get_event(tv.getText().toString());
//				
//				String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
//				//String eventName = e.getName();
//				String fileName = "1.txt";
//
//				// Not sure if the / is on the path or not
//				File f = new File(baseDir + File.separator + String.valueOf(e.getEvent_id()) + File.separator +fileName);
//				Log.d("DEBUG",f.getAbsolutePath());
//				FileWriter fiStream;
//				try {
//					fiStream = new FileWriter(f);
//					fiStream.write("Testing\n");
//					fiStream.close();
//					
//					
//					String baseDir1 = Environment.getExternalStorageDirectory().getAbsolutePath();
//					String fileName1 = baseDir1 + "/" +  String.valueOf(e.getEvent_id()) + "/" + "1.txt";
//					Log.d("DEBUG",fileName1);
//					f = new File(fileName1);
//					if (f.isFile())
//					{
//						Log.d("DEBUG", "Is a TXT");
//					        // make something with the name
//					}
//					String fileName2 = baseDir1 + "/" +  String.valueOf(e.getEvent_id()) + "/"  + "1.jpg";
//					f = new File(fileName2);
//					Log.d("DEBUG",fileName2);
//					if (f.isFile())
//					{
//						Log.d("DEBUG", "Is a PHOTO");
//					        // make something with the name
//					}
//					
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
			}
		});
			
    
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
