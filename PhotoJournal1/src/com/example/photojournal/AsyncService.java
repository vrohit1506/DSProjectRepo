package com.example.photojournal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;


public class AsyncService extends IntentService {

	private ServerSocket serverListen;
	private Socket clientSocket;
	private String reply;
	private Bundle b1;
	private int result = Activity.RESULT_CANCELED;
	protected MySQLiteHelper db;
	public AsyncService() {
		super("AysncService");
	}

	// Will be called asynchronously be Android
	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle b1 = new Bundle(intent.getExtras());
		Bundle dialogBundle = new Bundle(intent.getExtras());

		while(true){
			//Initialize streams
			PrintWriter pw = null;
			BufferedReader bw = null;
			/**
			 * All Asynchronous Tasks are Handled here
			 */
			try {
				Log.d("ASYNC TASK", "Waiting for Connection");
				serverListen = new ServerSocket(4321);
				clientSocket  = serverListen.accept();
				Log.d("ASYNC TASK", "Conected to Someone");
				Log.d("DEBUG_FRIEND", "Connected to Friend");
				db = new MySQLiteHelper(getApplicationContext());
				bw = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				pw = new PrintWriter(clientSocket.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//>>>>>>>>>>>>>>>>>>>>>
			JSONObject tmp = new JSONObject();
			JSONParser parser = new JSONParser();
			//String _response = new String();
			try {
				Log.d("ASYN SERVICE","Before Read");
				reply = new String((String) bw.readLine());
				Log.d("ASYN SERVICE",reply);
				tmp = (JSONObject) parser.parse(reply);
				Log.d("ASYN SERVICE",tmp.toString());
				//				clientSocket.close();
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
				Log.d("ip value", (String) b1.get("ip"));
				
//				dialogBundle.putAll(b1);
				dialogBundle.putString("event_name", (String) tmp.get("event_name"));
				dialogBundle.putString("publisher", (String) tmp.get("publisher"));
				dialogBundle.putLong("event_id", (Long) tmp.get("event_id"));
				
				dialogBundle.putString("ip", (String) b1.get("ip"));
				Log.d("ip value", (String) dialogBundle.get("ip"));
				
				dialogBundle.putInt("port", b1.getInt("port"));
				dialogBundle.putString("username", (String) b1.get("username"));
				

				Long event_id = new Long((Long)tmp.get("event_id"));
				if(event_id != 0)
					result = Activity.RESULT_OK;

				if (result == Activity.RESULT_OK) {
					Intent dialogActivity = new Intent(this, DialogActivity.class);
					dialogActivity.putExtras(dialogBundle);
					dialogActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(dialogActivity);
				}
				
				try {
					Log.d("async", "closing");
					pw.close();
					bw.close();
					clientSocket.close();
					serverListen.close();
					Log.d("async", "closed");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}

			/**
			 * 2. Get time stamp and return to the caller
			 */
			else if((tmp.get("request")).equals("get_time_stamp")){
				//Get event id
				Log.d("ASYNC SERVICE","GET TIME STAMP");
				Long event_id = new Long((Long)tmp.get("event_id"));
				Long timeStampSend = new Long(0);
				JSONObject timeStampObject = new JSONObject();
				//Call a static event method to get event timeStamp
				try {
					//Call static method to get time stamp
					timeStampSend = Event.getTimeStampFromLog(event_id);
					timeStampObject.put("response", timeStampSend);
					pw.write(timeStampObject.toString());
					pw.write('\n');
					pw.flush();
					pw.close();
					bw.close();
					clientSocket.close();
					serverListen.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			else if((tmp.get("request")).equals("add_image"))
			{
				try
				{
					Log.d("ASYN SERVICE","ADD IMAGE");

					//update the time stamp in the database
					MySQLiteHelper db = new MySQLiteHelper(getApplicationContext());
					Event e = db.get_event_by_id((Long)tmp.get("event_id"), getApplicationContext());
					Long time_stamp = (Long)tmp.get("time_stamp");

					db.update_time_stamp(e.getName(),time_stamp ,getApplicationContext());

					Request_Async.receive_photo(tmp, clientSocket,pw);
					Log.d("ASYNC SERVICE", "photo received");
					clientSocket.close();
					serverListen.close();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}

			else if((tmp.get("request")).equals("add_comment"))
			{
				try
				{
					Log.d("ASYN SERVICE","ADD COMMENT");

					//update the time stamp in the database
					MySQLiteHelper db = new MySQLiteHelper(getApplicationContext());
					Event e = db.get_event_by_id((Long)tmp.get("event_id"), getApplicationContext());
					Long time_stamp = (Long)tmp.get("time_stamp");

					db.update_time_stamp(e.getName(),time_stamp ,getApplicationContext());

					Request_Async.receive_comment(tmp, clientSocket,pw);
					Log.d("ASYNC SERVICE", "comment received");
					pw.close();
					bw.close();
					clientSocket.close();
					serverListen.close();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			
			else if((tmp.get("request")).equals("get_update")){
				Log.d("Entered","GET_UPDATE");
				//Get the communication variables
				Long event_id = new Long((Long)tmp.get("event_id"));
				Long startTS = new Long((Long)tmp.get("start_time_stamp"));
				Long endTS = new Long((Long)tmp.get("end_time_stamp"));
				String logFilePath = new String(Environment.getExternalStorageDirectory() + "/" + event_id.toString() + "/" + "log.txt");
				Log.d("GET_UPDATE", logFilePath);
				File logFile = new File(logFilePath);
				BufferedReader logFileReader = null;
				//Loop to reach that startLiner
				try{
					//initialize
					logFileReader = new BufferedReader(new FileReader(logFile));
					String readLOG = null;
					//Log.d("GET_UPDATE","Read Log = "+readLOG);
					long counter = 1;
					//Reach the line
					Log.d("GET_UPDATE","counter " + counter);
					Log.d("GET_UPDATE","StartTS " + startTS);
					Log.d("GET_UPDATE","endTS " + endTS);
					if(counter == startTS)
						readLOG=logFileReader.readLine();

					while(counter != startTS){
						//Log.d("GET_UPDATE", "In Loop count = "+counter.toString());
						if((readLOG=logFileReader.readLine()) == null)
							counter = (long) -1;
						counter++;
					}
					//Loop to send all data and wait for reply
					while(true){
						Log.d("GET_UPDATE", "In while(true) loop....");
						Log.d("GET_UPDATE","counter " + counter);
						Log.d("GET_UPDATE","endTS " + endTS);
						
						if((counter == -1) || (counter > endTS))
							break;

						try{
							//initial flush
							pw.flush();
							//bw.close();
							//pw.close();
							//Identify what is the data_type
							String[] identifyType = readLOG.split(" ");//logFileReader.readLine().split(" ");
							Log.d("GET_UPDATE", identifyType[0]);
							Log.d("GET_UPDATE", identifyType[1]);
							if(identifyType[0].equals("add_image")){
								String basePath = new String(Environment.getExternalStorageDirectory() + "/" + event_id.toString() + "/" + identifyType[1]);
								String imagePath = new String(basePath + "/1.jpg");
								String commentsFile = new String(basePath + "/2.txt");
								File commentFile = new File(commentsFile);
								BufferedReader readCommentsFile = new BufferedReader(new FileReader(commentFile));
								String tagline = new String(readCommentsFile.readLine());
								readCommentsFile.close();
								//File sendFile = new File(imagePath);
								Log.d("GET_UPDATE","Tagline = "+tagline);
								Log.d("GET_UPDATE","Photo_ID = "+identifyType[1]);
								Log.d("GET_UPDATE","TimeStamp = "+ counter);
								Log.d("GET_UPDATE", "calling send_image");
								Request_Async.send_image(imagePath, clientSocket,Long.parseLong(identifyType[1]) , counter, Integer.parseInt(event_id.toString()), tagline, bw, pw);
								counter++;
								Log.d("GET_UPDATE", "SENT IMAGE & Counter = "+counter);
							}
							else if(identifyType[0].equals("add_comment")){
								String[] identifyNumber = identifyType[1].split(".");
								String basePath = new String(Environment.getExternalStorageDirectory() + "/" + event_id.toString() + "/" + identifyNumber[0]);
								String imagePath = new String(basePath + "/1,jpg");
								String commentsFile = new String(basePath + "/2.txt");
								File commentFile = new File(commentsFile);
								BufferedReader readCommentsFile = new BufferedReader(new FileReader(commentFile));
								int linesRead = 2;
								readCommentsFile.readLine();
								String comment = null;
								while(linesRead < Integer.parseInt(identifyNumber[1]))
								{
									if ((comment = readCommentsFile.readLine()) != null)
										break;

								}
								readCommentsFile.close();
								Request_Async.send_comment(event_id, counter, Integer.parseInt(identifyNumber[0]), comment, clientSocket,bw,pw);
								counter++;
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					JSONObject end = new JSONObject();
					end.put("request", "end_of_transfer");
					pw.write(end.toString());
					pw.write("\n");
					pw.flush();
					
					logFileReader.close();
					bw.close();
					pw.close();
					clientSocket.close();
					serverListen.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}			

		}
	}

} 
