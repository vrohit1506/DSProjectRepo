package com.example.photojournal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class DialogActivity extends Activity {

	Bundle inDialogActivity;
	//Bundle bundle;
	TextView msg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dialog);
		inDialogActivity = getIntent().getExtras();
		String publisher = inDialogActivity.getString("publisher");
		String eventName = inDialogActivity.getString("event_name");
		String dialogMessage = publisher + " wants to add you to the event '" + eventName + "':";
		msg = (TextView) findViewById(R.id.dialog_message);
		msg.setText(dialogMessage);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dialog, menu);
		return true;
	}

	public void onClickAccept (View view) {
		
		Log.d("helloclass", "i am here");
		Intent i = new Intent(this, DialogService.class);
		Log.d("helloclass", "i am here");
		i.putExtras(inDialogActivity);
		Log.d("helloclass", "i am here");
		startService(i);
		Log.d("helloclass", "i am over");
		finish();

	}

	public void onClickReject (View view) {
		finish();
	}
}
