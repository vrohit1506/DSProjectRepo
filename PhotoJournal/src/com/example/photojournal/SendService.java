package com.example.photojournal;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.simple.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
	protected Socket friendClient;
	protected JSONObject send;
	protected JSONObject response;
	private int result = Activity.RESULT_CANCELED;
	protected String mUsername;
	protected String reply;
	protected Long event_id;
	protected Long time_stamp;
	protected MySQLiteHelper db;
	protected String friendIP;
	final protected int friendPort = 4321;


	public SendService() {
		super(new String("Sending File"));

	}


	// Will be called asynchronously be Android
	@SuppressLint("UseValueOf")
	@SuppressWarnings("unchecked")
	@Override
	protected void onHandleIntent(Intent intent) {

		/* 
		 * GET THE BUNDLE WHICH CONTAINS
		 * 1. USERNAME, 2. IP, 3. PORT, 4. REQUEST, 5. EVENT NAME, 
		 */
		Bundle b1 = new Bundle(intent.getExtras());

		//Kind of request
		request = new String(b1.getString("request"));
		db = new MySQLiteHelper(getApplicationContext()); 


		Log.d("USERNAME-SERVICE-ABOVE", request);
		Log.d("USERNAME-SERVICE-ABOVE", b1.getString("username"));

		//GET ALL THE STUFF
		ip = new String(b1.getString("ip")); 
		port = b1.getInt("port");
		mUsername = new String(b1.getString("username"));

		Log.d("USERNAME-SERVICE-ABOVE", mUsername);

		/**
		 *  TYPES OF REQUESTS
		 */


		/**
		 * EVENT 1 ======> CREATE EVENT REQUEST
		 */
		// 1. Create New Event Request Processing
		if(request.equals("new_event")){

			try {
				client = new Socket(ip,port);
			} catch (UnknownHostException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			send = new JSONObject();
			response = new JSONObject();

			//Creating a new JSONObject to send
			send.put("request", "new_event");
			send.put("event_name", b1.getString("event_name"));
			send.put("publisher", mUsername);

			//Initialize streams
			PrintWriter pw = null;
			BufferedReader bw = null;

			try {
				pw = new PrintWriter(client.getOutputStream());
				bw = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch (IOException e1) {

				e1.printStackTrace();
			}

			//Sending request for a new event
			pw.write(send.toString());
			pw.flush();

			reply = null;

			//Wait for the response = Yes
			JSONParser parser = new JSONParser();
			JSONObject tmp = new JSONObject();
			event_id = new Long(0);
			String string_event_id = new String();
			do{
				try {
					reply = new String((String) bw.readLine());
					tmp = (JSONObject) parser.parse(reply);
					string_event_id = (String) tmp.get("response");
					event_id = Long.parseLong(string_event_id);
				} catch (IOException e) {

					e.printStackTrace();
				} catch (ParseException e) {

					e.printStackTrace();
				}
			}while(event_id == (long) 0);
			//!reply.equals("Yes"));
			Log.d("EVENT ID", event_id.toString());

			//Get the event id and check if not zero

			if(event_id != 0)
				result = Activity.RESULT_OK;
			MySQLiteHelper db_service = new MySQLiteHelper(getApplicationContext());
			Event make_event = new Event(b1.getString("event_name"), event_id, mUsername, 0);

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

			//Communicate back to the main activity the results
			Bundle extras = intent.getExtras();
			if (extras != null) {
				Messenger messenger = (Messenger) extras.get("MESSENGER");
				Message msg = Message.obtain();
				Bundle bundle = new Bundle();
				bundle.putInt("result", result);
				//bundle.putLong("event_id", event_id);
				bundle.putString("filename", direct.getName());
				msg.setData(bundle);
				try {
					messenger.send(msg);
				} catch (android.os.RemoteException e1) {
					Log.w(getClass().getName(), "Exception sending message", e1);
				}

			}

		} //End of the new event if



		/**
		 * 2=======> ADD IMAGE
		 */
		else if(request.equals("add_image")){


			try {
				client = new Socket(ip,port);
			} catch (UnknownHostException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			send = new JSONObject();

			//FILEPATH
			filename = new String(b1.getString("filepath"));
			Log.d("FILE PATH", filename);

			//INITIAL REQUEST TO OBTAIN TIME STAMP BEFORE SENDING IMAGE
			send.put("request", "get_time_stamp");
			send.put("event_id", ((Event) b1.getSerializable("event")).getEvent_id());

			//Set up the File Input streams
			PrintWriter pwriter = null;
			FileInputStream fstream = null;
			BufferedInputStream bstream = null;
			BufferedReader breader = null;
			BufferedWriter bufwriter = null;

			try{
				//Initialize the streams
				pwriter = new PrintWriter(client.getOutputStream());
				breader = new BufferedReader(new InputStreamReader(client.getInputStream()));

			}
			catch(Exception e){
				e.printStackTrace();
			}

			//SEND THE INITIAL REQUEST TO ADD IMAGE
			Log.d("XXXXXXXXXXX", send.toString());
			pwriter.write(send.toString());
			pwriter.flush();
			//RECEIVE THE LATEST TIME STAMP

			JSONParser parser = new JSONParser();
			JSONObject tmp = new JSONObject();
			time_stamp = new Long(0);
			//TODO: IS THE TIME STAMP SENT A STRING OR INTEGER??
			do{
				try {

					reply = new String((String) breader.readLine());
					tmp = (JSONObject) parser.parse(reply);
					String string_time_stamp = new String(tmp.get("response").toString());
					//((Double) tmp.get("response")).longValue();
					time_stamp = (new Double((Double.parseDouble(string_time_stamp)))).longValue();

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}while(time_stamp < 0);

			//CLOSING CONNECTION
			try {
				client.close();
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			//INCEREMENT TIME STAMP
			time_stamp = time_stamp+1;

			//PUT TIME STAMP IN DATABSE

			Event e = ((Event) b1.getSerializable("event"));
			b1.putLong("time_stamp", time_stamp);
			db.update_time_stamp(e.getName(),time_stamp,getApplicationContext());


			//MAKE DIRECTORY
			File direct = new File(Environment.getExternalStorageDirectory() + "/" + ((Event) b1.getSerializable("event")).getEvent_id() + "/" + time_stamp);
			if(!direct.exists())
			{
				if(direct.mkdir()) 
				{
					//directory is created;
				}
			}

			//STORE THE IMAGE SENT IN THE APPROPRIATE DIRECTORY
			File fileImage = new File(Environment.getExternalStorageDirectory() + "/" + ((Event) b1.getSerializable("event")).getEvent_id() + "/" + time_stamp  + "/1.jpg");
			File fileImageActual = new File(b1.getString("filepath"));
			//Size of file
			byte[] imageArray = new byte[(int) fileImageActual.length()];

			try{
				//Read File & Write Image
				fstream = new FileInputStream(fileImageActual);
				bstream = new BufferedInputStream(fstream);
				bstream.read(imageArray, 0, imageArray.length);
				FileOutputStream writeImage = new FileOutputStream(fileImage);
				writeImage.write(imageArray);
				fstream.close();
				bstream.close();
				writeImage.close();
			} catch ( IOException e1 ) {
				e1.printStackTrace();
			}
			Log.d("RESPONSE>>>>>>", tmp.toString());
			//STORE THE TAG-LINE SENT IN THE APPROPRIATE DIRECTORY
			File fileTagLine = new File(Environment.getExternalStorageDirectory() + "/" + ((Event) b1.getSerializable("event")).getEvent_id() + "/" + time_stamp +"/2.txt");


			try{
				//TODO: Have to generalize TAG LINE
				PrintWriter writeImage = new PrintWriter(fileTagLine);
				writeImage.write(b1.getString("tagline"));
				fstream.close();
				bstream.close();
				writeImage.close();
			} catch ( IOException e1 ) {
				e1.printStackTrace();
			}
			response = new JSONObject();

			/**
			 * CONNECTION 2
			 */

			try {
				client = new Socket(ip,port);
				pwriter = new PrintWriter(client.getOutputStream());
				breader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch (UnknownHostException e1) {

				e1.printStackTrace();
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			send.clear();
			send.put("request", "add_image");
			send.put("event_id", ((Event) b1.getSerializable("event")).getEvent_id());
			send.put("time_stamp", new Long(time_stamp).toString());
			send.put("tag_line", b1.getString("tagline"));


			//SEND THE FILE
			File file = new File(filename);
			try {
				fstream = new FileInputStream(filename);
				bstream = new BufferedInputStream(fstream);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			//Size of file
			byte[] filearray = new byte[(int)file.length() + 1];
			Log.d("XXXXXXXX DEBUG_SIZE LONG XXXXXXX", (new Long(file.length())).toString());
			Log.d("XXXXXXXX DEBUG_SIZE INTEGER XXXXXXX", (new Integer((int)file.length())).toString());
			try{
				//Read File
				bstream.read(filearray, 0, filearray.length);

				//Send File Size
				send.put("size",  filearray.length);
				//Log.d("DEBUG_SIZE", (new Long(filearray.length)).toString());;
				pwriter.write(send.toString());
				pwriter.flush();


				//ACCEPT A YES
				//do{
				try {

					reply = new String((String) breader.readLine());
					//							continue;

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//}while(!reply.equals("Yes"));

				//IF(YES)
				//Send the file 
				OutputStream os = client.getOutputStream();

				//SEND IMAGE
				Log.d("HEY BUNDLE","HEY BUNDLE");

				os.write(filearray, 0, filearray.length);
				os.flush();

				//WAIT FOR A YES
				try {
					reply = new String((String) breader.readLine());

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				//Close the stream realated objects
				bstream.close();
				pwriter.flush();
				pwriter.close();
				client.close();
			}
			catch(Exception e1){
				e1.printStackTrace();
			}
			Log.d("DEBUG_ECE", "IAM HERE JUST BELOW MESSENGER");
			//Communicate back to the main activity the results
			//Bundle extras = intent.getExtras();
			result = Activity.RESULT_OK;
			if (b1 != null) {
				Messenger messenger = (Messenger) b1.get("MESSENGER");
				Message msg = Message.obtain();
				//Bundle bundle = new Bundle();
				b1.putInt("result", result);
				b1.putString("event_IMAGE", "image");
				b1.putString("filename", filename);
				msg.setData(b1);
				try {
					Log.d("IAM GOING BACK", "SENDING THE MESSAGE TO PREVIOUS ACTIVITY");
					messenger.send(msg);
				} catch (android.os.RemoteException e1) {
					Log.w(getClass().getName(), "Exception sending message", e1);
				}

			}
		}


		else if(request.equals("add_comment")){
			Log.d("IN SERVICE", "SENDING COMMENT");
			//CONNECT TO SERVER
			try {
				client = new Socket(ip,port);
			} catch (UnknownHostException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

			//CREATING A JSON OBJECT
			long local_Event_Id = (new Long((((Event) b1.getSerializable("event")).getEvent_id())));
			send = new JSONObject();
			send.put("request", request);
			send.put("event_id", (new Long((((Event) b1.getSerializable("event")).getEvent_id()))).toString());
			send.put("time_stamp", b1.getInt("folder_number"));
			send.put("comment", (String)b1.getString("comment"));
			Log.d("DEBUG FOLDER NUMBER IN SERVICE", ((Integer)(b1.getInt("folder_number"))).toString());
			Log.d("DEBUG EVENT ID IN SERVICE", (new Long((((Event) b1.getSerializable("event")).getEvent_id()))).toString());
			//Log.d("COMMENT SERVICE", );
			int folder_number = b1.getInt("folder_number");

			//OBTAIN THE FILE NAME
			File folder = new File(Environment.getExternalStorageDirectory() + "/" +  local_Event_Id + "/" + folder_number);
			Log.d("DEBUG", folder.getAbsolutePath());
			File[] listOfFiles = folder.listFiles();
			Integer countFiles = new Integer(listOfFiles.length);
			Log.d("DEBUG FILE COUNT", countFiles.toString());

			//CREATE COMMENT FILE
			//STORE THE IMAGE SENT IN THE APPROPRIATE DIRECTORY
			String filePath = Environment.getExternalStorageDirectory() + "/" + ((Event) b1.getSerializable("event")).getEvent_id() + "/" + folder_number + "/" + countFiles  + ".txt";
			File fileComment = new File(filePath);
			try {
				if(fileComment.createNewFile()){
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filePath , true)));
					pw.println((String)b1.getString("comment"));
					pw.close();
				}
				else{
					PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
					pw.println((String)b1.getString("comment"));
					pw.close();
				}
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}

			/**
			 * Sending Initial comment packet
			 */
			//Set up the File Input streams
			PrintWriter pwriter = null;
			FileInputStream fstream = null;
			BufferedInputStream bstream = null;
			BufferedReader breader = null;
			BufferedWriter bufwriter = null;

			try{
				//Initialize the streams
				pwriter = new PrintWriter(client.getOutputStream());
				breader = new BufferedReader(new InputStreamReader(client.getInputStream()));

			}
			catch(Exception e){
				e.printStackTrace();
			}

			//SEND THE INITIAL REQUEST WITH COMMENT
			Log.d("XXXXXXXXXXX", send.toString());
			pwriter.write(send.toString());
			pwriter.flush();


			//WAIT FOR YES
			//do{
			try {

				reply = new String((String) breader.readLine()); 

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//}while(!reply.equals("Yes"));
			Log.d("IN SERVICE COMMENT", "Received a yes");
			try{
				client.close();
				pwriter.close();
				fstream.close();
				breader.close();
				bufwriter.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}

			//Communicate back to the main activity the results
			//			Bundle extras = intent.getExtras();
			result = Activity.RESULT_OK;
			if (b1 != null) {
				Log.d("IN SERVICE", "CHECKED BUNDLE NOT NULL");
				Messenger messenger = (Messenger) b1.get("MESSENGER");
				Message msg = Message.obtain();

				b1.putInt("result", result);
				//b1.putString("eventComment", );
				//bundle.putLong("event_id", event_id);
				//bundle.putString("filename", direct.getName());
				msg.setData(b1);
				try {
					Log.d("IN SERVICE", "SENDING COMMENT");
					messenger.send(msg);
				} catch (android.os.RemoteException e1) {
					Log.w(getClass().getName(), "Exception sending message", e1);
				}

			}
		}

		else if(request.equals("add_subscriber")){
			Log.d("ADD SUBSCRIBER", "ENTERED");
			try {
				client = new Socket(ip,port);
			} catch (UnknownHostException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			send = new JSONObject();
			response = new JSONObject();

			//Creating a new JSONObject to send
			send.put("request", request);
			send.put("event_id", ((Event) b1.getSerializable("event")).getEvent_id());
			send.put("publisher", mUsername);
			send.put("subscriber", b1.get("subscriber_name"));

			//Initialize streams
			PrintWriter pw = null;
			BufferedReader bw = null;

			try {
				pw = new PrintWriter(client.getOutputStream());
				bw = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			//Sending request for a new event
			pw.write(send.toString());
			pw.flush();

			reply = null;

			//Wait for the response = Yes
			JSONParser parser = new JSONParser();
			JSONObject tmp = new JSONObject();
			event_id = new Long(0);
			String _response = new String();

			try {
				reply = new String((String) bw.readLine());
				tmp = (JSONObject) parser.parse(reply);
				_response = (String) tmp.get("response");
				//event_id = Long.parseLong(string_event_id);
			} catch (IOException e) {

				e.printStackTrace();
			} catch (ParseException e) {

				e.printStackTrace();
			}

			//Close the connection with server
			try{
				client.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}

			//If response is Yes
			if(_response.equals("Yes")){
				/** Join Event Request
				 *  Send request to other phone
				 */
				JSONObject friendRequest = new JSONObject();
				try{
					reply = new String((String) bw.readLine());
					tmp = (JSONObject) parser.parse(reply);
					friendIP = new String((String) tmp.get("response"));
					Log.d("DEBUG_IP_SERVICE", friendIP);
					friendClient = new Socket(friendIP, friendPort);
					friendRequest.put("request", "join_event");
					friendRequest.put("event_id", ((Event) b1.getSerializable("event")).getEvent_id());
					friendRequest.put("event_name",((Event) b1.getSerializable("event")).getName());
					friendRequest.put("publisher", mUsername);
					//Initialize streams
					PrintWriter pwFriend = null;
					BufferedReader bwFriend = null;
					try {
						pwFriend = new PrintWriter(friendClient.getOutputStream());
						bwFriend = new BufferedReader(new InputStreamReader(friendClient.getInputStream()));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					//Sending request for a new event
					pwFriend.write(friendRequest.toString());
					pwFriend.flush();
					pw.flush();
					pw.close();
					bw.close();
					pwFriend.close();
					bwFriend.close();

					
					friendClient.close();
				}
				catch(Exception e){
					e.printStackTrace();
				}	
			}

			//If response is No will have to do something later
			else{
				
			}
			
			Log.d("EXIT ADD SUBSCRIBER", "Exited");
		}
		//Dummy Else>>>>>>>>>>>>>>>>
		//		else{
		//			
		//			try {
		//				client = new Socket(ip,port);
		//			} catch (UnknownHostException e) {
		//				
		//				e.printStackTrace();
		//			} catch (IOException e) {
		//				
		//				e.printStackTrace();
		//			}
		//			send = new JSONObject();
		//			response = new JSONObject();
		//			send.put("request", "new_event");
		//			send.put("event_name", intent.getStringExtra("event_name"));
		//			//TODO: Add a general form to set the username
		//			send.put("publisher", "rohit");
		//			PrintWriter pw = null;
		//			BufferedReader bw = null;
		//			try {
		//				pw = new PrintWriter(client.getOutputStream());
		//				bw = new BufferedReader(new InputStreamReader(client.getInputStream()));
		//			} catch (IOException e1) {
		//				
		//				e1.printStackTrace();
		//			}
		//			
		//			//sending request for a new event
		//			pw.write(send.toString());
		//			pw.flush();
		//			
		//			//Send event name and 
		//			pw.write(send.toString());
		//			pw.flush();
		//			String reply = null;
		//			//Wait for the response = Yes
		//			JSONParser parser = new JSONParser();
		//				JSONObject tmp = new JSONObject();
		//				long event_id = 0;
		////				do{
		//					try {
		//						reply = new String((String) bw.readLine());
		//						tmp = (JSONObject) parser.parse(reply);
		//						 event_id = (Long) tmp.get("response");
		//					} catch (IOException e) {
		//				
		//						e.printStackTrace();
		//					} catch (ParseException e) {
		//						
		//						e.printStackTrace();
		//					}
		////				}while(event_id != 0);//!reply.equals("Yes"));
		//				
		//			//Get the event id and check if not zero
		//			//int event_id = (Integer) tmp.get("event_id");
		//			if(event_id != 0)
		//				result = Activity.RESULT_OK;
		//
		//			//TODO: Create directory of the name u receive
		//			
		//			
		//			
		//			
		//			//Communicate back to the main activity the results
		//			Bundle extras = intent.getExtras();
		//			if (extras != null) {
		//				Messenger messenger = (Messenger) extras.get("MESSENGER");
		//				Message msg = Message.obtain();
		//				msg.arg1 = result;
		//				msg.arg2 = (int) event_id;
		//				msg.obj = "/storage/sdcard0/Download";//+ filename;
		//				try {
		//					messenger.send(msg);
		//				} catch (android.os.RemoteException e1) {
		//					Log.w(getClass().getName(), "Exception sending message", e1);
		//				}
		//			}
		//			
		//		}//?>>>>>>>>>>>>>>>>>>>>>>>Dummy Else

	}
}
