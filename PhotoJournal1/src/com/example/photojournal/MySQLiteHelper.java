package com.example.photojournal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
		ArrayList<String> list = e.getSubcribers();
		if(list == null)
		{
			list = new ArrayList<String>();
			list.add(e.getName());
		}
		String str = convertArrayListToString(list);
		initialValues.put(COLUMN_SUBSCRIBERS,str);
		db.insert(TABLE_EVENT, null, initialValues);
		db.close();
		return;
	} 

	public Event get_event(String event_name,Context context) {
		SQLiteDatabase db = getInstance(context).getReadableDatabase();
		Cursor cursor = db.query(TABLE_EVENT, allColumns,
				COLUMN_NAME + "=?", new String[] {event_name}, null, null, null, null);
		//cursor.moveToFirst();
		
		if(!cursor.moveToFirst())
		{
			db.close();
			return null;
		}
		
		Event e = new Event(cursor.getString(1),cursor.getLong(3),cursor.getString(2),
				cursor.getInt(4),convertStringToArrayList(cursor.getString(5)));	 
		db.close();
		return e;
	}


	public Event get_event_by_id(Long event_id,Context context) {
		SQLiteDatabase db = getInstance(context).getReadableDatabase();
		Cursor cursor = db.query(TABLE_EVENT, allColumns,
				COLUMN_EVENT_ID + "=?", new String[] {String.valueOf(event_id)}, null, null, null , null);
		
		if(!cursor.moveToFirst())
		{
			db.close();
			return null;
		}
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
		int row = get_row(event_name, context);
		for(int i = 0; i< entry.size();i++)
		{
		Log.d("ADD SUBSCRIBER DATABASE",entry.get(i));
		}
		Event e = get_event(event_name, context);
		SQLiteDatabase db = getInstance(context).getWritableDatabase();
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_EVENT_ID, e.getEvent_id());
		initialValues.put(COLUMN_NAME, e.getName());
		initialValues.put(COLUMN_TIME_STAMP, e.getTime_stamp());
		initialValues.put(COLUMN_PUBLISHER, e.getPublisher());
		String new_list = convertArrayListToString(entry);
		initialValues.put(COLUMN_SUBSCRIBERS,new_list);
		
		
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
			str =str + ",";
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
			Log.d("String to Array",arr1[i]);
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
	public TimeStampData multicast_time_stamp(long event_id, String serverIp, Integer port, Context context, String mUsername)
	{
		// Get the list of subscriber ip's from database and execute in a loop for each ip
		TimeStampData ob = new TimeStampData();
		Socket client;
		JSONObject send;
		String reply;
		BufferedReader breader;
		PrintWriter pwriter;
		Long serverTs = new Long(0);
		JSONParser parser = new JSONParser();
		JSONObject tmp = new JSONObject();
		Long time_stamp;
		Long maxTs = new Long(0);
		String maxIp =  null;
		OutputStream os = null;
		InputStream is = null;
		Scanner sc = null;
		/*
		 * Read and populate this list from the db for the event_id given as input
		 */
		ArrayList<String> SubscriberList =  new ArrayList<String>();
		Event subListEvent = this.get_event_by_id(event_id, context);
		SubscriberList = subListEvent.getSubcribers();
		Log.d("MYSQLHELPER-ARRAY LIST", SubscriberList.toString());
		//Log.d()
		//--Get Server's TimeStamp--//
		Log.d("INSIDE UPDATE IP", serverIp);
		Log.d("INSIDE UPDATE PORT", port.toString());
		try {
			client = new Socket(serverIp, port);
			breader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			pwriter = new PrintWriter(client.getOutputStream());
			send = new JSONObject();
			Log.d("INSIDE UPDATE", "HERE UPDATE 1");
			/* 
			 * Send the request to get the timeStamp
			 */
			send.put("request", "get_time_stamp");
			send.put("event_id", event_id);
			os = client.getOutputStream();
			is = client.getInputStream();
			sc = new Scanner(is);
			PrintStream printStream = new PrintStream(os);
			printStream.print(send.toString());
			//printStream.print("\n");
			printStream.flush();
			os.flush();

			//os.close();
			//pwriter.flush();
			//pwriter.write(send.toString());
			Log.d("INSIDE ON CLICK", "HERE 2");

			/* 
			 * Receive the timeStamp
			 */
			reply = new String((String) breader.readLine());
			//reply = new String(sc.nextLine());
			Log.d("INSIDE ON CLICK REPLY FROM SERVER", reply);
			tmp = (JSONObject) parser.parse(reply);
			String string_time_stamp = new String(tmp.get("response").toString());
			serverTs = (new Double((Double.parseDouble(string_time_stamp)))).longValue();
			Log.d("INSIDE ON CLICK", "HERE 3");
			Log.d("INSIDE ON CLICK PRINT SERVER TS", serverTs.toString());

			/*
			 * Close the connection to server
			 */
			pwriter.flush();
			os.flush();
			printStream.close();
			is.close();
			os.close();
			breader.close();
			pwriter.close();
			client.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		int clientPort = 4321;
		Socket friendClient = null;
		BufferedReader breader1 = null;
		PrintWriter pwriter1 = null;
		reply = null;
		//--Execute in a loop--//
		if(SubscriberList.size() > 1){
			Log.d("SubscriberList = ", SubscriberList.toString());
			Log.d("SubscriberList Size = ", (new Integer(SubscriberList.size())).toString());
			for (String subscriberIp : SubscriberList)
			{
				String[] subscriberArray = subscriberIp.split("#");
				if(subscriberArray[0].equals(mUsername)){
					Log.d("SENDING TO MYSELF", "MYSELF...");
					continue;
				}
					
				
				subscriberIp = subscriberArray[1];
				Log.d("Current friend's IP", subscriberIp);
				try {
					friendClient = new Socket(subscriberIp, clientPort);
					breader1 = new BufferedReader(new InputStreamReader(friendClient.getInputStream()));
					pwriter1 = new PrintWriter(friendClient.getOutputStream(), true);
//					pwriter1.flush();
					send = new JSONObject();
//					InputStream is1 = friendClient.getInputStream();
//					Scanner sc1 = new Scanner(is1);
					//DataOutputStream dos = new DataOutputStream(friendClient.getOutputStream());
					//dos.flush();
					/* 
					 * Send the request to get the timeStamp
					 */
					Log.d("In MYSQL UPDATE", "HERE 1");
					send.put("request", "get_time_stamp");
					send.put("event_id", event_id);
					Log.d("SENDING THE JSON", send.toString());
					pwriter1.write(send.toString());
					pwriter1.write("\n");
					pwriter1.flush();
					
					//					os = client.getOutputStream();
					//is = client.getInputStream();
					//sc1 = new Scanner(is1);
					//					pwriter.flush();
					//					os.flush();
					//					PrintStream printStream = new PrintStream(os);
					//					printStream.print(send.toString());
					//					printStream.flush();
					//					os.flush();
					Log.d("In MYSQL UPDATE", "HERE 2");
					//pwriter.write(send.toString());
					/* 
					 * Receive the timeStamp
					 */
					reply = new String((String) breader1.readLine());
//					while(sc1.hasNextLine()){
//						Log.d("IN WHILE", "Looping...");
//						reply = new String(sc1.nextLine());
//					}
					Log.d("INSIDE ON CLICK REPLY FROM client", reply);
					tmp = (JSONObject) parser.parse(reply);
					String string_time_stamp = new String(tmp.get("response").toString());
					time_stamp = (new Double((Double.parseDouble(string_time_stamp)))).longValue();
					Log.d("INSIDE ON CLICK PRINT ClientTS", time_stamp.toString());
					//pwriter1.flush();
//					is1.close();
//					sc1.close();
					breader1.close();
					pwriter1.close();
					friendClient.close();

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
		}

		Log.d("MYSQL", "EXITED LOOP OF CLIENT");

		if (serverTs > maxTs)
		{
			ob.setIp(serverIp);
			ob.setTimeStamp(serverTs);
			ob.setPort(port);
		}
		else
		{
			ob.setIp(maxIp);
			ob.setTimeStamp(maxTs);
			ob.setPort(clientPort);
		}
		Log.d("INSIDE ON CLICK PRINT MAX", ob.getTimeStamp().toString());
		//		if(ob.getIp().toString() != null)
		//			Log.d("INSIDE ON CLICK PRINT IP", ob.getIp().toString());
		return ob;
	}

} 