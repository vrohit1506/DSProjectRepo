package com.example.photojournal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.*;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;



public class SendService extends IntentService {
	protected String request;
	protected String ip;
	protected int port;
	protected String filename;
	protected File file;
	protected Socket client;
	protected JSONObject send;
	protected JSONObject response;
	private int result = Activity.RESULT_CANCELED;
	
	
	public SendService() {
		super(new String("Sending File"));
		
	}


	// Will be called asynchronously be Android
	@SuppressWarnings("unchecked")
	@Override
	protected void onHandleIntent(Intent intent) {

		//Kind of request
		request = new String(intent.getStringExtra("request"));
		
		//TODO: general form to set ip and stuff
		//Get the extras which were passed by the main activity
		ip = new String("128.237.222.255"); //new String(intent.getStringExtra("ip"));
		port = 1234;//Integer.parseInt(new String(intent.getStringExtra("port")));
		//filename = new String(intent.getStringExtra("filename"));

		//Different types of things can happen
		try {
			client = new Socket(ip,port);
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		send = new JSONObject();
		response = new JSONObject();
		send.put("request", "new_event");
		send.put("event_name", intent.getStringExtra("event_name"));
		//TODO: Add a general form to set the username
		send.put("publisher", "rohit");
		PrintWriter pw = null;
		BufferedReader bw = null;
		try {
			pw = new PrintWriter(client.getOutputStream());
			bw = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
		
		//sending request for a new event
		pw.write(send.toString());
		pw.flush();
		
		//Send event name and 
		pw.write(send.toString());
		pw.flush();
		String reply = null;
		//Wait for the response = Yes
		JSONParser parser = new JSONParser();
			JSONObject tmp = new JSONObject();
			long event_id = 0;
//			do{
				try {
					reply = new String((String) bw.readLine());
					tmp = (JSONObject) parser.parse(reply);
					 event_id = (Long) tmp.get("response");
				} catch (IOException e) {
			
					e.printStackTrace();
				} catch (ParseException e) {
					
					e.printStackTrace();
				}
//			}while(event_id != 0);//!reply.equals("Yes"));
			
		//Get the event id and check if not zero
		//int event_id = (Integer) tmp.get("event_id");
		if(event_id != 0)
			result = Activity.RESULT_OK;

		//TODO: Create directory of the name u receive
		
		
		
		
		//Communicate back to the main activity the results
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Messenger messenger = (Messenger) extras.get("MESSENGER");
			Message msg = Message.obtain();
			msg.arg1 = result;
			msg.arg2 = (int) event_id;
			msg.obj = "/storage/sdcard0/Download";//+ filename;
			try {
				messenger.send(msg);
			} catch (android.os.RemoteException e1) {
				Log.w(getClass().getName(), "Exception sending message", e1);
			}
		}
	}
}
