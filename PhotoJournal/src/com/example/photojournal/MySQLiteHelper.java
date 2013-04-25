package com.example.photojournal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.content.ContentValues;
import android.database.Cursor;

public class MySQLiteHelper extends SQLiteOpenHelper implements Serializable {



	public static final String TABLE_EVENT = "event";
	public static final String COLUMN_NO = "_no";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_PUBLISHER = "publisher";
	public static final String COLUMN_EVENT_ID = "event_id";
	public static final String COLUMN_TIME_STAMP = "time_stamp";
	public static final String COLUMN_SUBSCRIBERS = "subscribers";

	private static final String DATABASE_NAME = "event1.db";
	private static final int DATABASE_VERSION = 1;

	static MySQLiteHelper single = null;
	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table " + TABLE_EVENT 
			+ "(" + COLUMN_NO + " integer primary key autoincrement, " 
			+ COLUMN_NAME + " TEXT , " 
			+ COLUMN_PUBLISHER + " TEXT , " 
			+ COLUMN_EVENT_ID + " integer , " 
			+ COLUMN_TIME_STAMP + " integer , " 
			+ COLUMN_SUBSCRIBERS + " TEXT );";

	private String[] allColumns = { COLUMN_NO,COLUMN_NAME,COLUMN_PUBLISHER,COLUMN_EVENT_ID,COLUMN_TIME_STAMP ,COLUMN_SUBSCRIBERS};
	//private SQLiteDatabase database;

	public MySQLiteHelper(Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d("DEBUG","SQL constructor");
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d("DEBUG","SQL On create");
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d("DEBUG", "Database upgraded");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT);
		onCreate(db);
	}

	public MySQLiteHelper getInstance(Context context) {
		if(single == null) {
			single = new MySQLiteHelper(context);
			return single;
		}
		return single;

	}

	public void add_event(Event e,Context context) {
		Log.d("DEBUG","ADD EVENT");
		SQLiteDatabase db = getInstance(context).getWritableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_EVENT_ID, e.getEvent_id());
		initialValues.put(COLUMN_NAME, e.getName());
		initialValues.put(COLUMN_TIME_STAMP, e.getTime_stamp());
		initialValues.put(COLUMN_PUBLISHER, e.getPublisher());

		initialValues.put(COLUMN_SUBSCRIBERS,e.getName());
		db.insert(TABLE_EVENT, null, initialValues);
		db.close();
		return;
	} 

	public Event get_event(String event_name,Context context) {
		SQLiteDatabase db = getInstance(context).getReadableDatabase();
		Cursor cursor = db.query(TABLE_EVENT, allColumns,
				COLUMN_NAME + "=?", new String[] {event_name}, null, null, null, null);
		cursor.moveToFirst();
		Event e = new Event(cursor.getString(1),cursor.getLong(3),cursor.getString(2),
				cursor.getInt(4),convertStringToArrayList(cursor.getString(5)));	 
		db.close();
		return e;
	}


	public Event get_event_by_id(Long event_id,Context context) {
		SQLiteDatabase db = getInstance(context).getReadableDatabase();
		Cursor cursor = db.query(TABLE_EVENT, allColumns,
				COLUMN_EVENT_ID + "=?", new String[] {String.valueOf(event_id)}, null, null, null , null);
		cursor.moveToFirst();
		Event e = new Event(cursor.getString(1),cursor.getLong(3),cursor.getString(2),
				cursor.getInt(4),convertStringToArrayList(cursor.getString(5)));	 
	    db.close();
		return e;
	}

	public ArrayList<Event> get_event_list(Context context) {
		SQLiteDatabase db = getInstance(context).getReadableDatabase();
		ArrayList<Event> events = new ArrayList<Event>();

		Cursor cursor = db.query(TABLE_EVENT,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Event e = new Event(cursor.getString(1),cursor.getLong(3),cursor.getString(2),
					cursor.getInt(4),convertStringToArrayList(cursor.getString(5)));	 
			events.add(e);
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		return events;
	}

	public void add_subscribers(String event_name,ArrayList<String> entry,Context context) {
		Event e = get_event(event_name, context);
		SQLiteDatabase db = getInstance(context).getWritableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_EVENT_ID, e.getEvent_id());
		initialValues.put(COLUMN_NAME, e.getName());
		initialValues.put(COLUMN_TIME_STAMP, e.getTime_stamp());
		initialValues.put(COLUMN_PUBLISHER, e.getPublisher());
		String new_list = convertArrayListToString(entry);
		initialValues.put(COLUMN_SUBSCRIBERS,new_list);
		
		int row = get_row(event_name, context);
		db.delete(TABLE_EVENT, COLUMN_NO + "=" + row, null);
		db.insert(TABLE_EVENT, null, initialValues);
		db.close();
	
		//db.update(TABLE_EVENT, initialValues,COLUMN_NAME + "=" + event_name, null);
	}

	public String convertArrayListToString(ArrayList<String> arraylist){
		if(arraylist == null)
			return null;
		//String array[] = arraylist.toArray(new String[arraylist.size()]);  
		String str = arraylist.get(0);
		for (int i = 1;i<arraylist.size(); i++) {
			str ="," + str;
			str = str + arraylist.get(i);
		}
		return str;
	}

	public ArrayList<String> convertStringToArrayList(String str){
		if(str.isEmpty())
			return null;
		String[] arr1 = str.split(",");
		Log.d("String to array",String.valueOf(arr1.length));
		ArrayList<String> arr = new ArrayList<String>();
		for(int i = 0; i < arr1.length;i++)
		{
			// ArrayList<String> arr = (ArrayList<String>)Arrays.asList(arr1);
			arr.add(arr1[i]);
		}
		return arr;
	}

	public int get_row(String name,Context context)
	{
		SQLiteDatabase db = getInstance(context).getReadableDatabase();
		Cursor cursor = db.query(TABLE_EVENT, allColumns,
				COLUMN_NAME + "=?", new String[] {name}, null, null, null, null);
		cursor.moveToFirst();
		//db.close();
		return (int)cursor.getInt(0);

	}

	public void update_time_stamp(String event_name,long time_stamp,Context context)
	{

		Log.d("TIME STAMP IN DATABASE", (new Long(time_stamp)).toString());
		Event e = get_event(event_name, context);
		e.setTime_stamp((int)time_stamp);
		Log.d("GET EVENT",e.getName());

		int row = get_row(event_name, context);

		SQLiteDatabase db = getInstance(context).getWritableDatabase();
		db.delete(TABLE_EVENT, COLUMN_NO + "=" + row, null);
		add_event(e, context);
		db.close();
		e = get_event(event_name, context);
		Log.d("Sooooo called update",new Long (e.time_stamp).toString());
	}
} 