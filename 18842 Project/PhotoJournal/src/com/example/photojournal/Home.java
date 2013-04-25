package com.example.photojournal;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Home extends Activity {

	//Network Information
	protected int port;
	protected String ip;
	protected Socket client;
	protected ServerSocket server;
	protected BufferedReader in;
	protected PrintWriter out;
	protected String reply;
	protected JSONObject elements;
	protected boolean loginverified;
	

	protected ArrayAdapter<String> listAdapter;
	protected ListView listView;
	protected MySQLiteHelper db;
	
	//Bundle
	protected Bundle extras;
	//Event id
	protected int event_id;
	protected String mUsername;
	protected final int VALUE = 0;
	protected ArrayList<Event> events;
	
	
	@SuppressLint("UseValueOf")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		/* I GET THE BUNDLE FROM THE LOGIN SCREEN 
		 * IT HAS USERNAME, IP, PORT 
		 */
		 extras = new Bundle(getIntent().getExtras());
		 Log.d("USERNAME-HOME",extras.getString("username"));
		 Log.d("USERNAME-IP",extras.getString("ip"));
		 Log.i("USERNAME-PORT",new Integer(extras.getInt("port")).toString());

	        listAdapter = new ArrayAdapter<String>(this, R.layout.device_name, R.id.textView1);
			listView = (ListView) findViewById(R.id.listView1);
			listView.setAdapter(listAdapter);
			

			
			db = new MySQLiteHelper(getApplicationContext());
			Log.d("USERNAME-HOME","After SQL Constructor");
			 events = db.get_event_list(getApplicationContext());
			
			for(int i = 0;i<events.size();i++)
				listAdapter.add(events.get(i).getName());
			
			/**
			 * WHEN U PRESS ONE OF THE EXISTING EVENTS TO GO INTO IT
			 */
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {

					TextView tv = (TextView) arg1;
					Log.d("DEBUG", tv.getText().toString());
					Event event = new Event();
					event = db.get_event(tv.getText().toString(), getApplicationContext());
					
					//Go to the event main screen
					Intent intent = new Intent(getApplicationContext(), MyEventActivity.class);
					//Bundle b = new Bundle();
					extras.putString("tagline", tv.getText().toString());
					extras.putSerializable("event", event);
					intent.putExtras(extras);
					startActivity(intent);
				}
			});


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_home, menu);
		
		return true;
		
	}
	
	/**
	 * Create EVENT CLASS WHEN U press + sign
	 * @param view
	 */
	public void onClick(View view){
		Intent intent = new Intent(this, CreateEvent.class);
		//extras.putSerializable("db", db);
		//extras.putParcelable("db", (Parcelable) db);
		intent.putExtra("username_intent", extras.getString("username"));
		intent.putExtras(extras);
		startActivityForResult(intent, VALUE);
	}
	
	protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
		Log.i("ON ACTIVITY RESULT", "IAM HERE");
		 
		
    	events = db.get_event_list(getApplicationContext());
		listAdapter.clear();
    	for(int i = 0;i<events.size();i++)
			listAdapter.add(events.get(i).getName());
    
        if (requestCode == VALUE) {

            if (resultCode == RESULT_OK) {
                // A contact was picked.  Here we will just display it
                // to the user.
            	//TODO:Take care of this
                startActivity(new Intent(getApplicationContext(), Home.class));
            }
        }
    }

}
