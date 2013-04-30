package com.example.photojournal;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import android.widget.Toast;


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
				bw = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				pw = new PrintWriter(clientSocket.getOutputStream(), true );
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
				Log.d("Received request",reply);
				tmp = (JSONObject) parser.parse(reply);

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

				//****** Create a log.txt *****
				File logText = new File(Environment.getExternalStorageDirectory() + "/" + event_id.toString() + "/" + "log.txt");
				try {
					logText.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// 2. Send to server ==> request = joining_event && event_id && myUsername 
				JSONObject joiningEvent = new JSONObject();
				try{
					clientSocket.close();
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
			 * 2. Get time stamp and return to the caller
			 */
			else if((tmp.get("request")).equals("get_time_stamp")){

				//Get event id

				Long event_id = new Long((Long)tmp.get("event_id"));
				Log.d("ASYNC SERVICE TIME STAMP",event_id.toString());
				Long timeStampSend = new Long(0);
				JSONObject timeStampObject = new JSONObject();
				//Call a static event method to get event timeStamp
				try {
					//bw.readLine();
					//Call static method to get time stamp
					timeStampSend = Event.getTimeStampFromLog(event_id);
					pw.flush();
					Log.d("ASYNC SERVICE TIME STAMP SENDING....", timeStampSend.toString());
					timeStampObject.put("response", timeStampSend);					
					pw.write(timeStampObject.toString());
					pw.write("\n");
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

			/**
			 * 3. Send Update to the requesting Node 
			 */
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
						if((counter == -1) || (counter == (endTS + 1)))
							break;
						
						try{
							//initial flush
							pw.flush();
							bw.close();
							pw.close();
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
								//File sendFile = new File(imagePath);
								Log.d("GET_UPDATE","Tagline = "+tagline);
								Log.d("GET_UPDATE","Photo_ID = "+identifyType[1]);
								Log.d("GET_UPDATE","TimeStamp = "+ counter);
								Log.d("GET_UPDATE", "calling send_image");
								Request_Async.send_image(imagePath, clientSocket,Long.parseLong(identifyType[1]) , counter, Integer.parseInt(event_id.toString()), tagline);
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
								Request_Async.send_comment(event_id, counter, Integer.parseInt(identifyNumber[0]), comment, clientSocket);
								counter++;
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
					JSONObject endOfTransfer = new JSONObject();
					endOfTransfer.put("request", "end_of_transfer");
					//pw.flush();
					pw.write(endOfTransfer.toString());
					pw.write("\n");
					logFileReader.close();
					bw.close();
					pw.close();
					clientSocket.close();
					serverListen.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}




			/**
			 * 2. Receive a multicast message and store
			 */
			//			else if((tmp.get("request")).equals("subscriber_list")){
			//				//Retrieve ID
			//				Log.d("ASYNC TASK--RECEIVED", "RESPONSE FROM SERVER" + tmp.get("response"));
			//				Long event_id = new Long((Long)tmp.get("event_id"));
			//				Log.d("ASYNC TASK - MULTICAST RECEIVED","Event ID" + event_id );
			//				//>>>>>>>>>Parsing of json sub list
			//				JSONObject subList = new JSONObject((JSONObject) tmp.get("subscriber_list"));
			//				Log.d("ASYNC SERVICE SUB LIST", subList.toString());
			//				
			//				try{
			//					pw = new PrintWriter(clientSocket.getOutputStream());
			//					pw.write("Received Sub List!! Thanks");
			//					pw.flush();
			//					pw.close();
			//					bw.close();
			//					try {
			//						clientSocket.close();
			//						serverListen.close();
			//					} catch (IOException e) {
			//						// TODO Auto-generated catch block
			//						e.printStackTrace();
			//					}
			//					
			//				}
			//				catch(Exception e){
			//					e.printStackTrace();
			//				}
			//				
			//				Iterator<String> iterator = (Iterator<String>) subList.keySet().iterator();
			//				ArrayList<String> subArrayList = new ArrayList<String>();
			//				while (iterator.hasNext()){
			//					subArrayList.add((String) subList.get(iterator.next()));
			//				}
			//				db = new MySQLiteHelper(getApplicationContext());
			//				Event e = db.get_event_by_id(event_id, getApplicationContext());
			//				db.add_subscribers(e.getName(),subArrayList,getApplicationContext());
			//				e = db.get_event(e.getName(), getApplicationContext());
			//				Log.d("ASYNC TASK", "TRYING TO TOAST");
			//				if(e!=null)
			//				{
			//					Toast.makeText(getApplicationContext(),
			//			            "Subscriber List =" + e.getSubcribers(), Toast.LENGTH_LONG)
			//			            .show();
			//				}
			//				else
			//					Log.d("ASYNC TASK--", "EVENT NOT FOUND");
			//				
			//				//>>>>>>>>>Parsing of json sub list
			//				//Get the Subscriber List
			//				String subscriberList = new String((String) tmp.get("subscriber_list"));
			//				Log.d("ASYNC SERVICE SUB LIST", subscriberList);
			//				//StringTokenizer stoken = new StringTokenizer();
			//			}
		}
	}
} 
