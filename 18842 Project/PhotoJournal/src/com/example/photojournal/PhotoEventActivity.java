package com.example.photojournal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class PhotoEventActivity extends Activity {

	protected EditText mAddTaglineView;
	protected String mNewTagline;
	protected ArrayAdapter<String> listAdapter;
	protected ListView listView;
	private Bitmap bitmap;
	private ImageView imageView;
	Bundle extras;

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {

			Bundle temp1 = new Bundle(message.getData());

			if ((temp1.getInt("result") == RESULT_OK) ) {
				Toast.makeText(PhotoEventActivity.this,
						"Image Added" + temp1.getString("filename"), Toast.LENGTH_LONG)
						.show();
				Intent tmp_intent = new Intent(getBaseContext(), MyEventActivity.class);
				tmp_intent.putExtras(temp1);
				startActivity(tmp_intent);
				finish();
			} else {
				Toast.makeText(PhotoEventActivity.this, "Send failed.",
						Toast.LENGTH_LONG).show();
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_event);
		imageView = (ImageView) findViewById(R.id.photoView);
		extras = new Bundle(getIntent().getExtras());
		bitmap = BitmapFactory.decodeFile(extras.getString("filepath"));
		imageView.setImageBitmap(bitmap);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_photo_event, menu);
		return true;
	}
	
	public void onClickTagline(View view){
		
		mAddTaglineView = (EditText) findViewById(R.id.addTagline);
		mNewTagline = mAddTaglineView.getText().toString();
		extras.putString("tagline", mNewTagline);
		Intent intent = new Intent(this, SendService.class);
		intent.putExtras(extras);
	    Messenger messenger = new Messenger(handler);
	    intent.putExtra("MESSENGER", messenger);
		startService(intent);
		Log.d("DEBUG", "TAGLINE ADDED");
	}

}
