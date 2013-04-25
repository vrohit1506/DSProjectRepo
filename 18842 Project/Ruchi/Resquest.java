package com.hello.firstapplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;

public class Resquest {


	protected static PrintWriter pwriter = null;
	protected static JSONObject send;
	protected static BufferedReader breader = null;
	protected static OutputStream os = null;
	protected static String reply = null;

	public static void send_image(String image_path, Socket client, int photo_id, int time_stamp, int event_id, String tag_line) {
		try
		{
			send = new JSONObject();
			breader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			os = client.getOutputStream();

			File file = new File(image_path);
			byte[] filearray = new byte[(int)file.length() + 1];

			send.put("request", "add_image");
			send.put("event_id", event_id);
			send.put("time_stamp", time_stamp);
			send.put("photo_id",photo_id);
			send.put("tag_line", tag_line);
			send.put("size",  filearray.length);

			pwriter.write(send.toString());
			pwriter.flush();

			reply = new String((String) breader.readLine());
			if(!reply.equals("Yes"))
				return;

			os.write(filearray, 0, filearray.length);
			os.flush();

			reply = new String((String) breader.readLine());

			os.close();
			breader.close();
		}
		catch(IOException e){
			e.printStackTrace();	
		}
		catch(JSONException e){
			e.printStackTrace();
		}
	}

	public static void send_comment(int event_id,int time_stamp,int photo_id,String comment,Socket client)
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
		catch(JSONException e){
			e.printStackTrace();
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
		catch(JSONException e) {
			e.printStackTrace();
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
			int size = j.getInt("size");
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
			write_to_file(folder + "/2.txt", (String)j.getString("tag_line"));

		}
		catch(JSONException e){
			e.printStackTrace();
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}

