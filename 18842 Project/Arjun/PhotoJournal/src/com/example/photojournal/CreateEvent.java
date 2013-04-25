package com.example.photojournal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CreateEvent extends Activity {
	private EditText mEventView;
	private String mEventName;
	//>>>>>>Service invoking>>>>>>
	private Handler handler = new Handler() {
	    public void handleMessage(Message message) {
	      Object path = message.obj;
	      if (message.arg1 == RESULT_OK && path != null) {
	    	  Toast.makeText(CreateEvent.this,
	            "Created New Event" + path.toString() + "ID =" + message.arg2, Toast.LENGTH_LONG)
	            .show();
	    	  Intent intent = new Intent (CreateEvent.this, Home.class);
	    	  startActivity(intent);
	    	  
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
		    //Create a new Messenger for the communication back
		    Messenger messenger = new Messenger(handler);
		    intent.putExtra("MESSENGER", messenger);
		    //TODO: Put the IP
		    intent.putExtra("request", "new_event");
		    intent.putExtra("event_name", mEventName);
		    //intent.putExtra("", value)
		    
		    //TODO: Add a generalized IP address & port
		    intent.putExtra("ip", "128.237.222.255");
		    intent.putExtra("port", 1234);
		    //intent.putExtra("filename", "simple.png");
		    startService(intent);
	  }
}
