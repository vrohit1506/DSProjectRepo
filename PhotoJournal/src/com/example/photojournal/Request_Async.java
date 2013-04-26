package com.example.photojournal;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class Request_Async {


	protected static PrintWriter pwriter = null;
	protected static JSONObject send;
	protected static BufferedReader breader = null;
	protected static OutputStream os = null;
	protected static String reply = null;
	protected static FileInputStream fstream = null;
	protected static BufferedInputStream bstream = null;
	
	
	public static void send_image(String image_path, Socket client, long photo_id, long time_stamp, int event_id, String tag_line) {
		try
		{
			send = new JSONObject();
			breader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			pwriter = new PrintWriter(client.getOutputStream());

			File file = new File(image_path);
			byte[] filearray = new byte[(int)file.length() + 1];

			Log.d("SEND IMAGE",image_path);
			Log.d("SEND IMAGE",String.valueOf(file.length()));
			
			send.clear();
			send.put("request", "add_image");
			send.put("event_id", event_id);
			send.put("time_stamp", time_stamp);
			send.put("photo_id",photo_id);
			send.put("tag_line", tag_line);
			send.put("size",  filearray.length);
			
			pwriter.write(send.toString());
			pwriter.flush();
			
			
			//Log.d("Image Added Reply", reply);
			//do
			{
				reply = new String((String) breader.readLine());
				Log.d("SEND FILE reply",reply);
			
			}//while(!reply.equals("Yes"));

			fstream = new FileInputStream(image_path);
			bstream = new BufferedInputStream(fstream);

			bstream.read(filearray, 0, filearray.length);

			os = client.getOutputStream();
			os.write(filearray, 0, filearray.length);
			os.flush();

			reply = new String((String) breader.readLine());

			os.close();
			bstream.close();
			
			pwriter.close();
			breader.close();
			client.close();
			Log.d("SEND FILE","returning");
		}
		catch(IOException e){
			e.printStackTrace();	
		}
	}

	public static void send_comment(long event_id,long time_stamp,int photo_id,String comment,Socket client)
	{
		try
		{
			breader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			pwriter = new PrintWriter(client.getOutputStream());

			send = new JSONObject();
			send.put("request", "add_comment");
			send.put("event_id", event_id);
			send.put("time_stamp", time_stamp);
			send.put("comment", comment);
			send.put("photo_id",photo_id);

			pwriter.write(send.toString());
			pwriter.flush();

			reply = new String((String) breader.readLine());
			pwriter.close();
			breader.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	public static void receive_comment(JSONObject j,Socket client)
	{
		try
		{
			String comment = (String)j.get("comment");
			String event_id = (String)j.get("event_id");
			String photo_id = (String)j.get("photo_id");
			String file_path = Environment.getExternalStorageDirectory() + "/" +
					event_id + "/" + photo_id + "/" + "2"  + ".txt";
			write_to_file(file_path, comment);
			file_path = Environment.getExternalStorageDirectory() + "/" + event_id + "/" + "log.txt";
			String log_entry = "add_comment" + " " + photo_id +  " " + comment; 
			write_to_file(file_path, log_entry);

			send = new JSONObject();
			send.put("response", "Yes");
			pwriter = new PrintWriter(client.getOutputStream());
			pwriter.write(send.toString());
			pwriter.flush();
			pwriter.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void write_to_file(String file_path, String data)
	{	
		try
		{
			File file = new File(file_path);
			file.createNewFile();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file_path , true)));
			pw.println(data);
			pw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void receive_photo(JSONObject j,Socket client)
	{
		try
		{
			String time_stamp = (String)j.get("time_stamp");
			String event_id = (String)j.get("event_id");
			Integer size = (Integer) j.get("size");
			String folder = Environment.getExternalStorageDirectory() + "/" + event_id + "/" + time_stamp;
			File direct = new File(folder);
			direct.mkdir();
			File fileImage = new File(folder  + "/1.jpg");
			FileOutputStream writeImage = new FileOutputStream(fileImage);

			byte[] imageArray = new byte[size];
			DataInputStream in = new DataInputStream(client.getInputStream());
			in.read(imageArray, 0, size);
			writeImage.write(imageArray);
			writeImage.close();
			in.close();
			write_to_file(folder + "/2.txt", (String)j.get("tag_line"));

		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}

