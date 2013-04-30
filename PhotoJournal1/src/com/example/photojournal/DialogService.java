package com.example.photojournal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class DialogService extends IntentService {
	
	public DialogService() {
		super("DialogService");
	}

	private ServerSocket serverListen;
	private Socket clientSocket;
	private String reply;
	private Bundle inDialogService;
	
protected void onHandleIntent (Intent intent) {
	inDialogService = new Bundle(intent.getExtras());
	PrintWriter pw = null;
	BufferedReader bw = null;
	Log.d("Dialog Service", "Joining Event");
	// 1. Create event_id folder in SD card
	//Get the event id and check if not zero

	Long event_id = new Long((Long)inDialogService.get("event_id"));
	MySQLiteHelper db_service = new MySQLiteHelper(getApplicationContext());
	Event make_event = new Event((String)inDialogService.get("event_name"), event_id, (String)inDialogService.get("publisher"), 0);
	db_service.add_event(make_event, getApplicationContext());

	//TODO: Create directory of the name u receive
	File direct = new File(Environment.getExternalStorageDirectory() + "/" + event_id.toString());
	Log.d("hello", "i am here");
	if(!direct.exists())
	{
		Log.d("hello", "i am here");
		if(direct.mkdir()) 
		{
			Log.d("hello", "i am here");
			//directory is created;
		}
	}
	Log.d("hello", "i am here");
	//****** Create a log.txt *****
	File logText = new File(Environment.getExternalStorageDirectory() + "/" + event_id.toString() + "/" + "log.txt");
	try {
		Log.d("hello", "i am here");
		logText.createNewFile();
		Log.d("hello", "i am here");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	Log.d("hello", "i am here");
	// 2. Send to server ==> request = joining_event && event_id && myUsername 
	JSONObject joiningEvent = new JSONObject();
	try{
		//clientSocket.close();
		clientSocket = new Socket((String) inDialogService.get("ip"), inDialogService.getInt("port"));
		pw = new PrintWriter(clientSocket.getOutputStream());

		//Make the request to server
		joiningEvent.put("request", "joining_event");
		joiningEvent.put("event_id", event_id);
		joiningEvent.put("username", inDialogService.get("username"));
		pw.write(joiningEvent.toString());					
		pw.flush();
		//bw.close();
		Log.d("ASYNC SERVICE - ", "INSIDE JOIN EVENT");
		bw = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		Log.d("ASYNC SERVICE - ", "Got input stream");
		reply = new String((String) bw.readLine());
		Log.d("XXXXXXXXXXX REPLY FROM SERVER ASYNC SERVICE", reply);
		pw.close();
		bw.close();
		clientSocket.close();
		//serverListen.close();
	}
	catch(Exception e){
		e.printStackTrace();
	}
}
}
