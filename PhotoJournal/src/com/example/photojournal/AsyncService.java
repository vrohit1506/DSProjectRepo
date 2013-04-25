package com.example.photojournal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;


public class AsyncService extends IntentService {

	ServerSocket serverListen;
	Socket clientSocket;
	String reply;
	Bundle b1;
	private int result = Activity.RESULT_CANCELED;
	protected MySQLiteHelper db;
	public AsyncService() {
		super("AysncService");
	}

	// Will be called asynchronously be Android
	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle b1 = new Bundle(intent.getExtras());
		while(true){
			//Initialize streams
			PrintWriter pw = null;
			BufferedReader bw = null;
			/**
			 * All Asynchronous Tasks are Handled here
			 */
			try {
				serverListen = new ServerSocket(4321);
				clientSocket  = serverListen.accept();
				Log.d("DEBUG_FRIEND", "Connected to Friend");
				bw = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//>>>>>>>>>>>>>>>>>>>>>
			JSONObject tmp = new JSONObject();
			JSONParser parser = new JSONParser();
			String _response = new String();
			try {
				reply = new String((String) bw.readLine());
				tmp = (JSONObject) parser.parse(reply);
				clientSocket.close();
				//_response = (String) tmp.get("response");
				//event_id = Long.parseLong(string_event_id);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {

				e.printStackTrace();
			}

			/**
			 * 1. Join Event Reqeuest received
			 */
			if((tmp.get("request")).equals("join_event")){
				Log.d("Request in Async", "Join Event");
				// 1. Create event_id folder in SD card
				//Get the event id and check if not zero
				Long event_id = new Long((Long)tmp.get("event_id"));
				if(event_id != 0)
					result = Activity.RESULT_OK;
				MySQLiteHelper db_service = new MySQLiteHelper(getApplicationContext());
				Event make_event = new Event((String)tmp.get("event_name"), event_id, (String)tmp.get("publisher"), 0);
				db_service.add_event(make_event, getApplicationContext());

				//TODO: Create directory of the name u receive
				File direct = new File(Environment.getExternalStorageDirectory() + "/" + event_id.toString());

				if(!direct.exists())
				{
					if(direct.mkdir()) 
					{
						//directory is created;
					}
				}

				// 2. Send to server ==> request = joining_event && event_id && myUsername 
				JSONObject joiningEvent = new JSONObject();
				try{
					clientSocket = new Socket((String) b1.get("ip"), b1.getInt("port"));
					pw = new PrintWriter(clientSocket.getOutputStream());

					//Make the request to server
					joiningEvent.put("request", "joining_event");
					joiningEvent.put("event_id", event_id);
					joiningEvent.put("username", b1.get("username"));
					pw.write(joiningEvent.toString());
					pw.flush();
					bw.close();
					Log.d("ASYNC SERVICE - ", "INSIDE JOIN EVENT");
					bw = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					reply = new String((String) bw.readLine());
					Log.d("XXXXXXXXXXX REPLY FROM SERVER ASYNC SERVICE", reply);
					pw.close();
					bw.close();
					clientSocket.close();
					serverListen.close();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			
			/**
			 * 2. Receive a multicast message and store
			 */
			else if((tmp.get("request")).equals("subscriber_list")){
				//Retrieve ID
				Long event_id = new Long((Long)tmp.get("event_id"));
				Log.d("ASYNC TASK - MULTICAST RECEIVED","Event ID" + event_id );
				//>>>>>>>>>Parsing of json sub list
				JSONObject subList = new JSONObject((JSONObject) tmp.get("subscriber_list"));
				Log.d("ASYNC SERVICE SUB LIST", subList.toString());
				
				try{
					clientSocket.close();
					serverListen.close();
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
				Iterator<String> iterator = (Iterator<String>) subList.keySet().iterator();
				ArrayList<String> subArrayList = new ArrayList<String>();
				while (iterator.hasNext()){
					subArrayList.add((String) subList.get(iterator.next()));
				}
				db = new MySQLiteHelper(getApplicationContext());
				Event e = db.get_event_by_id(event_id, getApplicationContext());
				db.add_subscribers(e.getName(),subArrayList,getApplicationContext());
				//>>>>>>>>>Parsing of json sub list
				//Get the Subscriber List
//				String subscriberList = new String((String) tmp.get("subscriber_list"));
//				Log.d("ASYNC SERVICE SUB LIST", subscriberList);
				//StringTokenizer stoken = new StringTokenizer();
			}
		}
	}
} 
