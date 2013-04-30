package com.example.photojournal;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import org.json.simple.JSONObject;

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


	public static void send_image(String image_path, Socket client, long photo_id, long time_stamp, int event_id, String tag_line, BufferedReader br, PrintWriter pw) {
		try
		{
			send = new JSONObject();

			pwriter = pw;
			//pwriter = new PrintWriter(client.getOutputStream());

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
			pwriter.write('\n');
			pwriter.flush();

			Log.d("Image Added Reply", "request sent waiting for reply");

			breader = br;
			//breader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			Log.d("SEND IMAGE","after initializing buffered reader");
			reply = new String((String) breader.readLine());
			Log.d("SEND FILE reply",reply);


			fstream = new FileInputStream(image_path);
			bstream = new BufferedInputStream(fstream);
			bstream.read(filearray, 0, filearray.length);

			os = client.getOutputStream();
			os.write(filearray, 0, filearray.length);
			os.write('\n');
			os.flush();

			//	Log.d("SEND IMAGE", "Sending image waiting for last yes");
			//	reply = new String((String) breader.readLine());
			//	Log.d("SEND IMAGE", "REceived yes");

			os.close();
			bstream.close();
			fstream.close();
			//breader.close();
			//pwriter.close();
			//client.close();
			Log.d("SEND FILE","returning");
		}
		catch(IOException e){
			Log.d("ERROR",e.toString());	
		}

	}

	public static void receive_photo(JSONObject j,Socket client, PrintWriter pw)
	{
		try
		{
			Log.d("RECEIVE IMAGE","START");
			//add write to log
			pwriter = new PrintWriter(client.getOutputStream());

			Long time_stamp1 = (Long)j.get("time_stamp");
			String time_stamp = time_stamp1.toString();
			Long photo_id1 = (Long)j.get("photo_id");
			String photo_id = photo_id1.toString();
			Long id = (Long)j.get("event_id");
			String event_id = id.toString();
			Long size1 = (Long) j.get("size");
			int size = size1.intValue();
			Log.d("RECEIVE PHOTO",String.valueOf(size));

			pwriter.write("Yes");
			pwriter.write("\n");
			pwriter.flush();

			Log.d("RECEIVE PHOTO","Reply sent to the friend");


			byte[] imageArray = new byte[size];

			InputStream stream = client.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte buffer[] = new byte[1024];
			for(int s; (s=stream.read(buffer)) != -1; )
			{
				baos.write(buffer, 0, s);
			}
			imageArray = baos.toByteArray();


			//int count = stream.read(imageArray);
			//stream.close();
			//Log.d("RECEIVE IMAGE",String.val);
			String folder = Environment.getExternalStorageDirectory() + "/" + event_id + "/" + photo_id;
			File direct = new File(folder);
			direct.mkdir();
			File fileImage = new File(folder  + "/1.jpg");

			FileOutputStream writeImage = new FileOutputStream(fileImage);

			writeImage.write(imageArray);
			writeImage.close();

			//pwriter.write("Yes");
			//pwriter.write('\n');
			//pwriter.flush();

			write_to_file(folder + "/2.txt", (String)j.get("tag_line"));

			String file_path = Environment.getExternalStorageDirectory() + "/" + event_id + "/" + "log.txt";
			String log_entry = "add_image" + " " + photo_id; 

			write_to_file(file_path, log_entry);

			//pwriter.close();
			//client.close();
		}
		catch(FileNotFoundException e){
			Log.d("ERROR",e.toString());
		}
		catch(IOException e){
			Log.d("ERROR",e.toString());
		}
	}

	public static void write_to_file(String file_path, String data)
	{	
		try
		{
			File file = new File(file_path);
			file.createNewFile();
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file_path , true)));
			pw.write(data + '\n');
			pw.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}



	public static void send_comment(long event_id,long time_stamp,int photo_id,String comment,Socket client,BufferedReader bw,PrintWriter pw)
	{
		try
		{
			/*
			breader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			pwriter = new PrintWriter(client.getOutputStream());
			 */
			breader = bw;
			pwriter = pw;
			send = new JSONObject();
			send.put("request", "add_comment");
			send.put("event_id", event_id);
			send.put("time_stamp", time_stamp);
			send.put("comment", comment);
			send.put("photo_id",photo_id);

			pwriter.write(send.toString());
			pwriter.write("\n");
			pwriter.flush();

			reply = new String((String) breader.readLine());
			//pwriter.close();
			//breader.close();
			//client.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	public static void receive_comment(JSONObject j,Socket client, PrintWriter pw)
	{

		String comment = (String)j.get("comment");
		Long event_id1 = (Long)j.get("event_id");
		String event_id = String.valueOf(event_id1);
		Long photo_id1 = (Long)j.get("photo_id");
		String photo_id = String.valueOf(photo_id1);

		String file_path = Environment.getExternalStorageDirectory() + "/" +
				event_id + "/" + photo_id + "/" + "2.txt";

		int count_lines  = count_lines_for_comments(file_path);
		count_lines = count_lines - 1;

		write_to_file(file_path, comment);

		file_path = Environment.getExternalStorageDirectory() + "/" + event_id + "/" + "log.txt";
		String log_entry = "add_comment" + " " + photo_id + "." + (count_lines + 3); 

		write_to_file(file_path, log_entry);

		send = new JSONObject();

		send.put("response", "Yes");


		//pwriter = new PrintWriter(client.getOutputStream());
		pwriter = pw;
		pwriter.write(send.toString());
		pwriter.write('\n');
		pwriter.flush();
		//pwriter.close();
	}

	static int count_lines_for_comments(String filePath)
	{
		int countLines = 0;
		try
		{	
			File fileComment = new File(filePath);
			Scanner lineCountHelper = new Scanner(fileComment);
			while(lineCountHelper.hasNextLine())
			{
				countLines++;
				lineCountHelper.nextLine();
			}
			Log.d("NUMBER OF LINES IN COMMENT FILE", String.valueOf(countLines));
			lineCountHelper.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return countLines;
	}

}

