package com.hello.firstapplication;

import java.io.Serializable;
import java.util.ArrayList;

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
 
  private static final String DATABASE_NAME = "event.db";
  private static final int DATABASE_VERSION = 1;

  static MySQLiteHelper single = null;
  // Database creation sql statement
  private static final String DATABASE_CREATE = "create table " + TABLE_EVENT 
	  + "(" + COLUMN_NO
		+ " integer primary key autoincrement," + COLUMN_NAME
      + " TEXT ," + COLUMN_PUBLISHER
      + " TEXT ," + COLUMN_EVENT_ID
      + " integer , " + COLUMN_TIME_STAMP + " integer ) ; ";

  private String[] allColumns = { COLUMN_NO,COLUMN_NAME,COLUMN_PUBLISHER,COLUMN_EVENT_ID,COLUMN_TIME_STAMP};
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
 
      db.insert(TABLE_EVENT, null, initialValues);
      db.close();
      return;
  } 
  
  public Event get_event(String event_name,Context context) {
	  SQLiteDatabase db = getInstance(context).getReadableDatabase();
	  Cursor cursor = db.query(TABLE_EVENT, allColumns,
				COLUMN_NAME + "=?", new String[] {event_name}, null, null, null, null);
	  cursor.moveToFirst();
	  Event e = new Event(cursor.getString(1),cursor.getInt(3),cursor.getString(2),cursor.getInt(4));	 
	  //db.close();
	  return e;
  }
  
  public ArrayList<Event> get_event_list(Context context) {
	  SQLiteDatabase db = getInstance(context).getReadableDatabase();
	  ArrayList<Event> events = new ArrayList<Event>();

	    Cursor cursor = db.query(TABLE_EVENT,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	Event e = new Event(cursor.getString(1),cursor.getInt(3),cursor.getString(2),cursor.getInt(4));	 
	      events.add(e);
	      cursor.moveToNext();
	    }
	    cursor.close();
	    //db.close();
	    return events;
  }

} 