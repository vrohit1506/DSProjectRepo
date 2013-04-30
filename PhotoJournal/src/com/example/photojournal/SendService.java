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
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
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

			//***** log.txt is created here *******
			File logText = new File(Environment.getExternalStorageDirectory() + "/" + event_id.toString() + "/" + "log.txt");
			try {
				logText.createNewFile();
				client.close();
			} catch (IOException e) {

				e.printStackTrace();
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
			send.put("request", "get_photo_id");
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

			try {

				reply = new String((String) breader.readLine());
				tmp = (JSONObject) parser.parse(reply);
				String string_time_stamp = new String(tmp.get("response").toString());
				//((Double) tmp.get("response")).longValue();
				time_stamp = (new Double((Double.parseDouble(string_time_stamp)))).longValue();
				client.close();

			} catch (IOException e1) {

				e1.printStackTrace();
			} catch (ParseException e) {

				e.printStackTrace();
			}
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

			//			try {
			//				logText.createNewFile();
			//			} catch (IOException eLog) {
			//				
			//				eLog.printStackTrace();
			//			}

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

				PrintWriter writeImage = new PrintWriter(fileTagLine);
				writeImage.write(b1.getString("tagline")+"\n");
				fstream.close();
				bstream.close();
				writeImage.close();
			} catch ( IOException e1 ) {
				e1.printStackTrace();
			}
			//***Write the Image and Tagline into the log file
			//**** Try to create or refer the log file *******
			String imagePath = new String(Environment.getExternalStorageDirectory() + "/" + ((Event) b1.getSerializable("event")).getEvent_id() + "/" + "log.txt");
			File logText = new File(Environment.getExternalStorageDirectory() + "/" + ((Event) b1.getSerializable("event")).getEvent_id() + "/" + "log.txt");
			PrintWriter writeLog;
			try {
				writeLog = new PrintWriter(new BufferedWriter(new FileWriter(imagePath , true)));
				Long photo_id = time_stamp;
				writeLog.println("add_image " + photo_id.toString());
				writeLog.flush();
				writeLog.close();
				//writeLog.write("")

			} catch (FileNotFoundException e2) {

				e2.printStackTrace();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}


			response = new JSONObject();

			/**
			 * CONNECTION 2
			 */

			try{
				long photo_id = time_stamp;
				Event e_New = db.get_event_by_id(((Event) b1.getSerializable("event")).getEvent_id() , getApplicationContext());
				ArrayList<String> list = e_New.getSubcribers(); 
				client = new Socket(ip,port);
				Request_Async.send_image(filename, client,photo_id , 1 , (int)e_New.getEvent_id(), b1.getString("tagline"));
				/*
				if(list != null)
				{
				for(int i = 0; i<list.size();i++)
				{
					String sub = list.get(i);
					Log.d("SUBSCRIBER",sub);
				}
				}*/
			}
			catch (UnknownHostException e1) {

				e1.printStackTrace();
			} catch (IOException e1) {

				e1.printStackTrace();
			}
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
			Integer countLines = new Integer(0);

			try {
				client = new Socket(ip,port);
				String comment = (String)b1.getString("comment");
				long event_id = ((Event) b1.getSerializable("event")).getEvent_id();
				int folder_number = b1.getInt("folder_number");
				Request_Async.send_comment(event_id, 1, folder_number, comment,client);

				//OBTAIN THE FILE NAME
				File folder = new File(Environment.getExternalStorageDirectory() + "/" +  event_id + "/" + String.valueOf(folder_number));
				Log.d("DEBUG", folder.getAbsolutePath());
				File[] listOfFiles = folder.listFiles();
				Integer countFiles = new Integer(listOfFiles.length);
				Log.d("DEBUG FILE COUNT", countFiles.toString());

				//CREATE COMMENT FILE
				//STORE THE COMMENT SENT IN THE APPROPRIATE DIRECTORY
				String filePath = Environment.getExternalStorageDirectory() + "/" + ((Event) b1.getSerializable("event")).getEvent_id() + "/" + folder_number + "/" + countFiles  + ".txt";
				File fileComment = new File(filePath);

				//**** Try to create or refer the log file *******
				String logPath = Environment.getExternalStorageDirectory() + "/" + ((Event) b1.getSerializable("event")).getEvent_id() + "/" + "log.txt";
				File logText = new File(logPath);
				try {
					logText.createNewFile();
				} catch (IOException eLog) {

					eLog.printStackTrace();
				}

				try {
					if(fileComment.createNewFile()){
						PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filePath , true)));
						pw.write((String)b1.getString("comment")+"\n");
						Log.d("DEBUG FILE COUNT AFTER", "HERE IAM 1");
						pw.close();
						//>>>>
						//Count number of lines in comments file
						Scanner lineCountHelper = new Scanner(fileComment);
						while(lineCountHelper.hasNextLine()){
							countLines++;
							lineCountHelper.nextLine();
						}
						Log.d("NUMBER OF LINES IN COMMENT FILE", countLines.toString());
						lineCountHelper.close();
						//>>>>>

						Log.d("DEBUG FILE COUNT AFTER", "HERE IAM 2");
					}
					else{
						Log.d("DEBUG FILE COUNT AFTER", "HERE IAM 3");
						PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
						pw.write((String)b1.getString("comment")+"\n");
						pw.close();
						//>>>>
						//Count the number of lines in file
						Scanner lineCountHelper = new Scanner(fileComment);
						while(lineCountHelper.hasNextLine()){
							countLines++;
							lineCountHelper.nextLine();
						}

						Log.d("NUMBER OF LINES IN COMMENT FILE", countLines.toString());
						lineCountHelper.close();
						//>>>

						Log.d("DEBUG FILE COUNT AFTER", "HERE IAM 4");
					}
				} catch (IOException e3) {

					e3.printStackTrace();
				}

				//****Get the number of lines in the comments file and just store it in log file
				//Remove Tagline
				//There is somehow a new line in 2.txt coming between every comment
				//TODO: Check the file
				//countLines/=2;
				PrintWriter pwLogHelper = new PrintWriter(new BufferedWriter(new FileWriter(logPath , true)));
				pwLogHelper.write("add_comment " + folder_number + "." + countLines + "\n");
				pwLogHelper.flush();
				pwLogHelper.close();
			} catch (UnknownHostException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}


			//fileComment
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

		else if(request.equals("get_timeStamp")){
			Log.d("SEND SERVICE", "GETTING TIME STAMP");
			/*
			 * max_timeStamp is the largest timeStamp
			 */
			Integer timeStamp = new Integer(0);
			Event event = (Event) b1.getSerializable("EVENT_UPDATE");
			TimeStampData ob = db.multicast_time_stamp(event.getEvent_id(), b1.getString("ip"), b1.getInt("port"), getApplicationContext(), b1.getString("username"));
			File fileComment = new File( Environment.getExternalStorageDirectory() + "/" + event.getEvent_id() + "/" + "log.txt");  
			try {
				timeStamp = getCountNumberOfLines(fileComment);

			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			/**
			 * Got the Max Time Stamp and details of the node
			 * Step1: Check if Local TimeStamp < Received TimeStamp
			 * Step2: if True then perform actual get_update else break
			 * Step3: Get Update ==> JSONObject = {"request":"get_update","event_id":getID, "start_timeStamp":localTS+1,"end_timeStamp":maxTs};
			 * Step4: Send t request to the ob ip,port and keep connectionion alive until u dnt receive a end_request
			 * 
			 */
			Log.d("TESTING CODE PART..", "ENTERED");
			//Declare network variables we will need
			Socket friendUpdateSocket = null;
			BufferedReader friendUpdateReader = null;
			PrintWriter friendUpdateWriter = null;
			JSONObject updatePacketSend = new JSONObject();
			JSONObject updatePacketReceive = new JSONObject();
			JSONParser updateParser = new JSONParser();
			String updateReply = null;
			Log.d("GET_TIME_STAMP",timeStamp.toString());
			Log.d("GET OB.TIME_STAMP",ob.getTimeStamp().toString());
			if(ob.getTimeStamp() > timeStamp){
				while(true){
					Log.d("UPDATING Loop...","Entered");
					//Loop until u dnt receive end_of_transfer
					try{
						// Connect to the node you want update from
						friendUpdateSocket = new Socket(ob.getIp(), ob.getPort());
						friendUpdateReader = new BufferedReader(new InputStreamReader(friendUpdateSocket.getInputStream()));
						friendUpdateWriter = new PrintWriter(friendUpdateSocket.getOutputStream());
						//Create a request packet to the selected node
						updatePacketSend.put("request", "get_update");
						updatePacketSend.put("event_id", event.getEvent_id());
						Long startTS = new Long((timeStamp + 1));
						updatePacketSend.put("start_time_stamp", startTS);
						updatePacketSend.put("end_time_stamp", ob.getTimeStamp());
						Log.d("JSON OBJECT", updatePacketSend.toString());
						friendUpdateWriter.write(updatePacketSend.toString());
						friendUpdateWriter.write("\n");
						friendUpdateWriter.flush();
						//Read response
						Log.d("Waiting for response...", "ReadLine");
						reply = new String((String) friendUpdateReader.readLine());
						updatePacketReceive = (JSONObject) updateParser.parse(reply);
						if(updatePacketReceive.get("request").equals("add_image")){
							friendUpdateWriter.close();
							Request_Async.receive_photo(updatePacketReceive, friendUpdateSocket);
						}
						else if(updatePacketReceive.get("request").equals("add_comment")){
							friendUpdateWriter.close();
							Request_Async.receive_comment(updatePacketReceive, friendUpdateSocket);
						}
						else if(updatePacketReceive.get("request").equals("end_of_transfer")){
							break;
						} 
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
				try {
					friendUpdateSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			b1.putSerializable("object", ob);
			//Communicate back to main HOME
			Messenger messenger = (Messenger) b1.get("MESSENGER");
			Message msg = Message.obtain();
			msg.setData(b1);
			try {
				Log.d("IN SERVICE", "GOT TIME STAMP AND COMMUNICATION BACK TO HOME");
				messenger.send(msg);
			} catch (android.os.RemoteException e1) {
				Log.w(getClass().getName(), "Exception sending message", e1);
			}
			//finish();
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
		//			//
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
		//			//
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

	public int getCountNumberOfLines(File fileComment) throws FileNotFoundException
	{
		Integer countLines = new Integer(0);
		//>>>>
		//Count number of lines in comments file
		Scanner lineCountHelper = new Scanner(fileComment);
		while(lineCountHelper.hasNextLine()){
			countLines++;
			lineCountHelper.nextLine();
		}
		Log.d("NUMBER OF LINES IN COMMENT FILE", countLines.toString());
		lineCountHelper.close();
		return countLines;
		//>>>>>
	}
}
