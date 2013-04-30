package com.example.photojournal;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AddCommentActivity extends Activity {
	protected Bundle extras;
	protected Long event_id;
	protected Integer folder_number;
	protected ArrayAdapter<String> listAdapter;
	protected ListView listView;
	protected Bitmap bitmap;
	protected ImageView imageView;
	protected EditText mAddCommentView;
	protected String mNewComment;
	

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {

			Bundle temp1 = new Bundle(message.getData());

			if ((temp1.getInt("result") == RESULT_OK) ) {
				Toast.makeText(AddCommentActivity.this,
						"Comment Added" + temp1.getString("filename"), Toast.LENGTH_LONG)
						.show();
				Intent tmp_intent = new Intent(getBaseContext(), AddCommentActivity.class);
				tmp_intent.putExtras(temp1);
				startActivity(tmp_intent);
				finish();
			} else {
				Toast.makeText(AddCommentActivity.this, "Send failed.",
						Toast.LENGTH_LONG).show();
			}
		};
	};
	
	

	//protected MySQLiteHelper db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_comment);
		extras = new Bundle(getIntent().getExtras());

		//Toast.makeText(getApplicationContext(), "The value of arg is = " + extras.getInt("folder_number"), Toast.LENGTH_LONG).show();
		event_id = ((Event)extras.getSerializable("event")).getEvent_id();
		folder_number = extras.getInt("folder_number");
		Log.d("DEBUG", "Entered comment activity");
		File folder = new File(Environment.getExternalStorageDirectory() + "/" +  event_id + "/" + folder_number);
		Log.d("DEBUG", folder.getAbsolutePath());
		Log.d("DEBUG FILE INFO", folder.getAbsolutePath());
		Log.d("DEBUG FILE INFO", folder_number.toString());
		File[] listOfFiles = folder.listFiles();
		
		
		//>>>>>>>>>>>>>>>>>>>>>

		listAdapter = new ArrayAdapter<String>(this, R.layout.add_comment_null, R.id.textViewComment);
		listView = (ListView) findViewById(R.id.commentListView);
		listView.setAdapter(listAdapter);

		//READ TAGLINE
		String tagLine = new String();
		BufferedReader breader;
		try {
			breader = new BufferedReader(new FileReader(listOfFiles[1].getAbsolutePath()));
			tagLine = (String) breader.readLine();
			TextView tv = (TextView) findViewById(R.id.photoTagLine);
			tv.setText(tagLine);

			//IMAGE ADD
			FileInputStream fstream = null;
			BufferedInputStream bstream = null;
			//Size of file
			byte[] imageArray = new byte[(int) listOfFiles[0].getAbsoluteFile().length()];

			try{
				//Read File & Write Image
				fstream = new FileInputStream(listOfFiles[0].getAbsoluteFile());
				bstream = new BufferedInputStream(fstream);
				bstream.read(imageArray, 0, imageArray.length);		
				imageView = (ImageView) findViewById(R.id.photoCommentView);
				//extras = new Bundle(getIntent().getExtras());
				bitmap = BitmapFactory.decodeFile(listOfFiles[0].getAbsolutePath());
				imageView.setImageBitmap(bitmap);
			}
			catch(Exception e){
				e.printStackTrace();
			}


			///for(int i = 2;i<listOfFiles.length;i++){
				File fileEntry = listOfFiles[1];

				breader = new BufferedReader(new FileReader(fileEntry.getAbsolutePath()));
				String data = new String();
				breader.readLine();
				StringBuilder actualData = new StringBuilder();
				while((data = breader.readLine()) != null){
					actualData.append(data);
					listAdapter.add(data);
				} 
		//	}	

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.activity_add_comment, menu);
			return true;
		}
		public void onClickAddComment(View view){
			
			Log.d("onClickAddComment", "IAM HERE");
			//Get the event name
			mAddCommentView = (EditText) findViewById(R.id.addComment);
			mNewComment = mAddCommentView.getText().toString();
			extras.putString("comment", /*"\n"*/  mNewComment);
			extras.putString("request", "add_comment");
			Intent intent = new Intent(this, SendService.class);
			intent.putExtras(extras);
		    Messenger messenger = new Messenger(handler);
		    extras.putParcelable("MESSENGER", messenger);
		    intent.putExtra("MESSENGER", messenger);
		    //Socket s = new Socket(extras.get("ip"), () extras.get("port"));
		    Log.d("onClickAddComment", "I CALLED SERVICE");
			startService(intent);
			
		}

	}
