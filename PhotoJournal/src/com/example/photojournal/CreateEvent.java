package com.example.photojournal;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CreateEvent extends Activity {
	private EditText mEventView;
	private String mEventName;
	protected String mUsername;
	protected Bundle testing_bundle;
	protected Intent critical_intent;
	Bundle extras;
	//>>>>>>Service invoking>>>>>>
	
	private Handler handler = new Handler() {
	    public void handleMessage(Message message) {
	     
	      Bundle temp1 = new Bundle(message.getData());
	      
	      if ((temp1.getInt("result") == RESULT_OK) && (temp1.getString("filename") != null)) {
	    	  Toast.makeText(CreateEvent.this,
	            "Created New Event" + temp1.getString("filename") + "ID =" + message.arg2, Toast.LENGTH_LONG)
	            .show();
	    	  finish();
	      } else {
	        Toast.makeText(CreateEvent.this, "Send failed.",
	            Toast.LENGTH_LONG).show();
	      }
	    };
	  };
	//>>>>>>Service ending>>>>>>>>

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_event);
		critical_intent = new Intent(getIntent());
		mUsername = new String(critical_intent.getStringExtra("username_intent"));
		extras = new Bundle(getIntent().getExtras());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_create_event, menu);
		return true;
	}
	
		//Create New Event Button
	  public void OnClick(View view) {
		    Intent intent = new Intent(this, SendService.class);
		    
		    //Get the event name
		    mEventView = (EditText) findViewById(R.id.createnewevet);
		    mEventName = mEventView.getText().toString();
		    
		    //Add event to database
		    //Create a new Messenger for the communication back
		    Messenger messenger = new Messenger(handler);
		    intent.putExtra("MESSENGER", messenger);
		    
		    //Creating Bundle
		    extras.putString("request", "new_event");
		    extras.putString("event_name", mEventName);
//		    Bundle b = new Bundle();
//		    b.putString("request", "new_event");
//		    b.putString("event_name", mEventName);
//		    b.putString("username", mUsername);
//		    b.putString("ip", extras.getString("ip"));
//		    b.putString("port", extras.getString("port"));
//		    b.putString("event_name", mEventName);
		    
		    Log.d("USERNAME-CREATE-EVENT-BELOW", mUsername );
		    
		    //Putting into intent
		    intent.putExtras(extras);
		    //intent.putExtra("db", critical_intent.getSerializableExtra("db"));
		    //Start Service
		    startService(intent);
		    
		  }
	  

}
