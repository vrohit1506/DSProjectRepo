package com.example.photojournal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
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

	public int get_row(String name,Context context)
	{
		SQLiteDatabase db = getInstance(context).getReadableDatabase();
		Cursor cursor = db.query(TABLE_EVENT, allColumns,
				COLUMN_NAME + "=?", new String[] {name}, null, null, null, null);
		cursor.moveToFirst();
		db.close();
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

	
	/*
	 * *************************************************
	 * *************************************************
	 * *************************************************
	 * Input : Event ID
	 * Output : Largest timeStamp and IP of the process that has this timeStamp
	 * 
	 * First we retrieve the subscriber list for the input event_id. This method multicasts a get_time_stamp request to all 
	 * the peers on the subscriber list and to the server.
	 * It finally computed the largest ts and the corresponding ip associated with it.
	 */
	public TimeStampData multicast_time_stamp(long event_id, String serverIp, int port)
	{
		// Get the list of subscriber ip's from database and execute in a loop for each ip
		TimeStampData ob = new TimeStampData();
		Socket client;
		JSONObject send;
		String reply;
		BufferedReader breader;
		Long serverTs = new Long(0);
		JSONParser parser = new JSONParser();
		JSONObject tmp = new JSONObject();
		Long time_stamp;
		Long maxTs = new Long(0);
		String maxIp =  null;
		
		/*
		 * Read and populate this list from the db for the event_id given as input
		 */
		ArrayList<String> SubscriberList =  new ArrayList<String>();
		
		//--Get Server's TimeStamp--//
		try {
			client = new Socket(serverIp, port);
			breader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			send = new JSONObject();
			
			/* 
			 * Send the request to get the timeStamp
			 */
			send.put("request", "get_time_stamp");
			send.put("event_id", event_id);
			
			/* 
			 * Receive the timeStamp
			 */
			reply = new String((String) breader.readLine());
			tmp = (JSONObject) parser.parse(reply);
			String string_time_stamp = new String(tmp.get("response").toString());
			serverTs = (new Double((Double.parseDouble(string_time_stamp)))).longValue();
			
			/*
			 * Close the connection to server
			 */
			client.close();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		//--Execute in a loop--//
		for (String subscriberIp : SubscriberList)
		{
			try {
				client = new Socket(subscriberIp, port);
				breader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				
				send = new JSONObject();
				
				/* 
				 * Send the request to get the timeStamp
				 */
				send.put("request", "get_time_stamp");
				send.put("event_id", event_id);
				
				/* 
				 * Receive the timeStamp
				 */
				reply = new String((String) breader.readLine());
				tmp = (JSONObject) parser.parse(reply);
				String string_time_stamp = new String(tmp.get("response").toString());
				time_stamp = (new Double((Double.parseDouble(string_time_stamp)))).longValue();
				
				/*
				 * Close the connection to this peer
				 */
				client.close();
								
				if (time_stamp > maxTs)
				{
					maxTs = time_stamp;
					maxIp = subscriberIp;
				}
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}			
		}		
		
		
		if (serverTs > maxTs)
		{
			ob.setIp(serverIp);
			ob.setTimeStamp(serverTs);
		}
		else
		{
			ob.setIp(maxIp);
			ob.setTimeStamp(maxTs);
		}
		return ob;
	}
} 