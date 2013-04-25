package com.example.photojournal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MyEventActivity extends Activity {
	
	protected Bundle extras;
	protected MySQLiteHelper db;
	
	
	private static final int SELECT_PICTURE = 1;
	private String selectedImagePath;
	private String filemanagerstring;
	private static final int DIALOG_ALERT = 10;
	
	protected EditText mAddFriendView;
	protected String mNewFriendName;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event);        
      
        // Each row in the list stores country name, currency and flag
        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();        
        
        extras = new Bundle(getIntent().getExtras());
        db = new MySQLiteHelper(getApplicationContext());
        Event e = db.get_event(((Event) extras.getSerializable("event")).getName(), getApplicationContext());
        String path = Environment.getExternalStorageDirectory() + "/" +
        		Integer.toString((int)e.getEvent_id()) + "/";
        Log.d("IMAGE",path);
        Log.d("IMAGE",e.getName());
        Log.d("IMAGE",e.getPublisher());
        Log.d("IMAGE",Integer.toString((int)e.getEvent_id()));
        Log.d("TIME STAMP", (new Long(e.getTime_stamp())).toString());
        //hard coded to 2 should be changed to timestamp
        for(int i=1 ; i <= e.getTime_stamp() ;i++){
        	String path_new = path + Integer.toString(i) + "/";
        	HashMap<String, String> hm = new HashMap<String,String>();
            String path_image = path_new + "1.jpg";
            String path_comment = path_new + "2.txt";
            
            BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(new File(path_comment)));
				Log.d("IMAGE",path_image);
	           
	            String tag_line = br.readLine();
	            hm.put("tag_line", tag_line);
	            Log.d("IMAGE",tag_line); 
	        	hm.put("photo", path_image);            
	            aList.add(hm);    
	            br.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            
        }
        
        // Keys used in Hashmap
        String[] from = { "photo","tag_line"};
        
        // Ids of views in listview_layout
        int[] to = { R.id.photo,R.id.tag_line};        
        
        // Instantiating an adapter to store each items
        // R.layout.listview_layout defines the layout of each item
        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.image_view, from, to);
        
        // Getting a reference to listview of main.xml layout file
        ListView listView = ( ListView ) findViewById(R.id.listview);
        
        // Setting the adapter to the listView
        listView.setAdapter(adapter);                                
    
        listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				Log.d("DEBUG_ARG3", (new Long(arg3)).toString());
				Log.d("DEBUG_ARG2", (new Integer(arg2)).toString());
				Log.d("EVENT_ID_CLICKED", (new Long(((Event)extras.getSerializable("event")).getEvent_id())).toString());
				extras.putInt("folder_number", (arg2 + 1));
				Intent intent = new Intent(getApplicationContext(), AddCommentActivity.class);
				intent.putExtras(extras);
				startActivity(intent);

//				TextView tv = (Line) arg1;
//				Log.d("DEBUG", tv.getText().toString());
//				Event event = new Event();
//				event = db.get_event(tv.getText().toString(), getApplicationContext());
//				//Go to the event main screen
//				Intent intent = new Intent(getApplicationContext(), AddCommentActivity.class);
//				//Bundle b = new Bundle();
//				extras.putString("tagline", tv.getText().toString());
//				extras.putSerializable("event", event);
//				intent.putExtras(extras);
//				startActivity(intent);
			}
		});

    
    }
    
    public void onClickListener(View view) {
    	extras.putString("request", "add_image");
		// in onCreate or any event where your want the user to
		// select a file
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("DEBUG IN RESULT", "I HAVE SELECTED IMAGE AND WANT TO SEND");
		if (resultCode == RESULT_OK) {
			Log.d("DEBUG IN RESULT1", "I HAVE SELECTED IMAGE AND WANT TO SEND");

			if (requestCode == SELECT_PICTURE) {
				Log.d("DEBUG IN RESULT2", "I HAVE SELECTED IMAGE AND WANT TO SEND");

				Uri selectedImageUri = data.getData();

				//OI FILE Manager
				filemanagerstring = selectedImageUri.getPath();

				//MEDIA GALLERY
				selectedImagePath = getPath(selectedImageUri);
				
				//HERE IS WHAT WE WANT
				Log.d("filepath", selectedImagePath);
				Log.d("filemanager", filemanagerstring);
				
								
//				//START SERVICE AND INITILAIZE PARAMETERS
				Intent sendImageIntent = new Intent(this, PhotoEventActivity.class);
//				Messenger messenger = new Messenger(handler);
//			    sendImageIntent.putExtra("MESSENGER", messenger);
				extras.putString("filepath", selectedImagePath);
				sendImageIntent.putExtras(extras);
//				Log.d("jashfjkasfhjksafh", "CALLED SERVICE");
//				startService(sendImageIntent);
				
				//GO TO MINI-EVENT(PHOTO) OF MAIN EVENT 
				startActivityForResult(sendImageIntent, 99);
				Log.d("done", "kaam ho gaya... path bhi mil gaya aur photo bhi aa gaya! :D");
				
			}
		

			// Each row in the list stores country name, currency and flag
	        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();        
	        
	        extras = new Bundle(getIntent().getExtras());
	        
	        Event e = db.get_event(((Event) extras.getSerializable("event")).getName(), getApplicationContext());
	        String path = Environment.getExternalStorageDirectory() + "/" +
	        		Integer.toString((int)e.getEvent_id()) + "/";
	        Log.d("IMAGE",path);
	        Log.d("IMAGE",e.getName());
	        Log.d("IMAGE",e.getPublisher());
	        Log.d("IMAGE",Integer.toString((int)e.getEvent_id()));
	        Log.d("TIME STAMP IN RESULT ACT", (new Long(e.getTime_stamp())).toString());
	        //hard coded to 2 should be changed to timestamp
	        for(int i=1 ; i <= extras.getLong("time_stamp") ;i++){
	        	String path_new = path + Integer.toString(i) + "/";
	        	HashMap<String, String> hm = new HashMap<String,String>();
	            String path_image = path_new + "1.jpg";
	            String path_comment = path_new + "2.txt";
	            
	            BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(new File(path_comment)));
					Log.d("IMAGE",path_image);
		           
		            String tag_line = br.readLine();
		            hm.put("tag_line", tag_line);
		            Log.d("IMAGE",tag_line); 
		        	hm.put("photo", path_image);            
		            aList.add(hm);    
		            br.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	            
	        }
	        
	        // Keys used in Hashmap
	        String[] from = { "photo","tag_line"};
	        
	        // Ids of views in listview_layout
	        int[] to = { R.id.photo,R.id.tag_line};        
	        
	        // Instantiating an adapter to store each items
	        // R.layout.listview_layout defines the layout of each item
	        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.image_view, from, to);
	        
	        // Getting a reference to listview of main.xml layout file
	        ListView listView = ( ListView ) findViewById(R.id.listview);
	        
	        // Setting the adapter to the listView
	        listView.setAdapter(adapter);                                
	    
		}
	}
	
	public String getPath(Uri contentUri) {
	    String res = null;
	    String[] proj = { MediaStore.Images.Media.DATA };
	    Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
	    if(cursor.moveToFirst()){;
	       int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	       res = cursor.getString(column_index);
	    }
	    cursor.close();
	    return res;
	}
	
	/**
	 * THE ADD FRIEND BUTTON
	 * @param view
	 */
	//When you want to add friends
	@SuppressWarnings("deprecation")
	public void onClickAddSubscriber(View view){
		showDialog(DIALOG_ALERT);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		//Set View Reference
		View dialogView = findViewById(R.layout.dialog_add_friend);

		// Create out AlterDialog
		Builder builder = new AlertDialog.Builder(this);
	    LayoutInflater inflater = this.getLayoutInflater();
	    builder.setView(inflater.inflate(R.layout.dialog_add_friend, null));
//		builder.setView(dialogView);
		builder.setMessage("Add Your Friend!!");
		builder.setCancelable(true);
		builder.setPositiveButton("Add", new OkOnClickListener());
		builder.setNegativeButton("Cancel", new CancelOnClickListener());
		AlertDialog dialog = builder.create();
		dialog.show();
		mAddFriendView = (EditText) dialog.findViewById(R.id.addfriends);
		return super.onCreateDialog(id);
	}

	private final class CancelOnClickListener implements
	DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			Toast.makeText(getApplicationContext(), "Request To Add Friends Cancelled",
					Toast.LENGTH_LONG).show();
		}
	}

	private final class OkOnClickListener implements
	DialogInterface.OnClickListener {
		public void onClick(DialogInterface dialog, int which) {
			
			mNewFriendName = mAddFriendView.getText().toString();
			Intent intent = new Intent(getApplicationContext(), SendService.class);
			extras.putString("request", "add_subscriber");
			extras.putString("subscriber_name", mNewFriendName);
			intent.putExtras(extras);
			startService(intent);
			Toast.makeText(getApplicationContext(), "Added Friend : Username "+ mNewFriendName,
					Toast.LENGTH_LONG).show();
			MyEventActivity.this.finish();
			Intent refresh = new Intent(getApplicationContext(), MyEventActivity.class);
			refresh.putExtras(extras);
			startActivity(refresh);
		}
	} 



    
}