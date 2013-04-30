package com.example.photojournal;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.json.simple.JSONObject;

import android.media.AudioRecord.OnRecordPositionUpdateListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
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
import android.widget.Toast;

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

	protected Event helperEvent;
	//>>>>>>>>

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			Log.d("HOME", "GOT RESULT FROM TIMESTAMP SERVICE");
			Bundle temp1 = new Bundle(message.getData());
			/*
			 * max_timeStamp is the largest timeStamp
			 */
			//TimeStampData ob = db.multicast_time_stamp(event.getEvent_id(), extras.getString("ip"), extras.getInt("port"), getApplicationContext());
			TimeStampData ob = (TimeStampData) temp1.getSerializable("object");
			if (ob.getTimeStamp() != helperEvent.getTime_stamp())
			{
				/*
				 * Client is not up-to-date with this event. Call get_update method of SendService.
				 */	
				Intent intent = new Intent(getApplicationContext(), SendService.class);
				extras.putString("request", "get_update");
				extras.putString("maxIp", ob.getIp());
				extras.putLong("startTs", helperEvent.getTime_stamp() +1);
				extras.putLong("endTs", ob.getTimeStamp());
				intent.putExtras(extras);
				Log.d("DUBUG", "Get update called");
				startService(intent);

				//get_update(ip, event.getTime_stamp() +1, ob.getTimeStamp());
			}


			/*
			 * Now that local SD card for the event is updated. Go to the event main screen.
			 */
			Intent intent = new Intent(getApplicationContext(), MyEventActivity.class);
			//Bundle b = new Bundle();
			extras.putSerializable("event", helperEvent);
			intent.putExtras(extras);
			startActivity(intent);

		};
	};


	@SuppressLint("UseValueOf")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		//Get Bundle
		extras = new Bundle(getIntent().getExtras());

		//Call Async Service
		Intent callAsyncIntent = new Intent(getApplicationContext(), AsyncService.class);
		callAsyncIntent.putExtras(extras);
		startService(callAsyncIntent);

		//Call Async Server Comm
		Intent callAsyncServerIntent = new Intent(getApplicationContext(), AsyncServerComm.class);
		callAsyncServerIntent.putExtras(extras);
		startService(callAsyncServerIntent);
		/* I GET THE BUNDLE FROM THE LOGIN SCREEN 
		 * IT HAS USERNAME, IP, PORT 
		 */

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
		 * *************************************************
		 * *************************************************
		 * *************************************************
		 * WHEN U PRESS ONE OF THE EXISTING EVENTS TO GO INTO IT
		 * 
		 * 
		 * BEFORE YOU CLICK ON IT, YOU HAVE TO UPDATE YOUR LOCAL SD CARD WITH THE LATEST INFORMATION
		 * 
		 * 1. CALL TIMESTAMP METHOD WITH PARAMETERS AS SUBSCRIBER LIST.(THAT METHOD RETURNS HIGHEST TIMESTAMP VALUE AND CORRESPOINDING IP AFTER DOING A MULTICAST)
		 * 2. COMPARE LOCALTS AND VALUE RETRIVED, IF SAME THEN CONTINUE WITH THE ON-CLICK CODE ELSE
		 * 3. SEND get_update(ip, localTs +1, requiredTs) TO THAT IP.
		 * 4. BY THE TIME get_update() returns, SD card should be updated.
		 * 5. CONTINUE WITH ON-CLICK CODE.
		 */
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.d("INSIDE ON CLICK", "HERE 1");

				/*
				 * Get the Event details of the particular event clicked on.
				 */
				Intent getUpdate = new Intent(getApplicationContext(), SendService.class);
				TextView tv = (TextView) arg1;
				extras.putString("tagline", tv.getText().toString());
				Log.d("DEBUG", tv.getText().toString());
				helperEvent = new Event();
				helperEvent = db.get_event(tv.getText().toString(), getApplicationContext());
				extras.putString("request", "get_timeStamp");
				extras.putSerializable("EVENT_UPDATE", helperEvent);
				Messenger messenger = new Messenger(handler);
				getUpdate.putExtra("MESSENGER", messenger);
				getUpdate.putExtras(extras);
				Log.d("INSIDE ON CLICK", "HERE 2");
				startService(getUpdate);
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
