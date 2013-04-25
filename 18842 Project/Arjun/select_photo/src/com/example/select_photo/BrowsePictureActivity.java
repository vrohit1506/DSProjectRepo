package com.example.select_photo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
public class BrowsePictureActivity extends Activity {


	private static final int SELECT_PICTURE = 1;
	private String selectedImagePath;
	private String filemanagerstring;
	private Bitmap bitmap;
	private ImageView imageView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse_picture);
		imageView = (ImageView) findViewById(R.id.imageView1);

		((Button) findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				// in onCreate or any event where your want the user to
				// select a file
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {

				Uri selectedImageUri = data.getData();

				//OI FILE Manager
				filemanagerstring = selectedImageUri.getPath();

				//MEDIA GALLERY
				selectedImagePath = getPath(selectedImageUri);

				//DEBUG PURPOSE
				if(selectedImagePath == null)
					System.out.println("selectedImagePath is null");
				if(filemanagerstring == null)
					System.out.println("filemanagerstring is null");

				//HERE IS WHAT WE WANT
				Log.d("filepath", selectedImagePath);
				Log.d("filemanager", filemanagerstring);

				bitmap = BitmapFactory.decodeFile(selectedImagePath);
				imageView.setImageBitmap(bitmap);

				Log.d("done", "kaam ho gaya... path bhi mil gaya aur photo bhi aa gaya! :D");
			}
		}
	}
/*
 	// this also works but here, managedQuery() is deprecated
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		if(cursor != null)
		{
			//HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
			//THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		else return null;
	}
*/	
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
}