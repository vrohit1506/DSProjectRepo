package com.example.photojournal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

import android.os.Environment;
import android.util.Log;

public class Event implements Serializable {

	String name;
	Long event_id;
	String publisher;
	int time_stamp;
	String path;
	ArrayList<String> subscribers;

	public Event(){}
	
	public Event(String name,Long id,String publisher, int time_stamp) {
		this.name = name;
		this.event_id = id;
		this.publisher = publisher;
		this.time_stamp = time_stamp;
		subscribers = null;
	}
	
	public Event(String name,Long id,String publisher, int time_stamp,ArrayList<String> members) {
		this.name = name;
		this.event_id = id;
		this.publisher = publisher;
		this.time_stamp = time_stamp;
		subscribers = members;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getEvent_id() {
		return event_id;
	}

	public void setEvent_id(Long event_id) {
		this.event_id = event_id;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public int getTime_stamp() {
		return time_stamp;
	}

	public void setTime_stamp(int time_stamp) {
		this.time_stamp = time_stamp;
	}
	
	public ArrayList<String> getSubcribers() {
		return subscribers;
	}
	
	public static Long getTimeStampFromLog(Long event_id) throws FileNotFoundException{
		Long actual_time_stamp = new Long(0);
		Long countLines = new Long(0);
		File fileComment = new File(Environment.getExternalStorageDirectory() + "/" + event_id + "/" + "log.txt");
		Scanner lineCountHelper = new Scanner(fileComment);
		while(lineCountHelper.hasNextLine()){
			countLines++;
			lineCountHelper.nextLine();
		}
		//TODO: Whether I need to divde lineCount by 2. Check the actual log file structure
		Log.d("NUMBER OF LINES IN COMMENT FILE", countLines.toString());
		actual_time_stamp = countLines;
		lineCountHelper.close();
		return actual_time_stamp;
	}
}









//???????????
//
//import java.io.Serializable;
//
//
//public class Event implements Serializable {
//
//	String name;
//	long event_id;
//	String publisher;
//	long time_stamp;
//	
//	public Event(){}
//	public Event(String name,long id,String publisher, long time_stamp) {
//		this.publisher = publisher;
//		this.name = name;
//		this.event_id = id;
//		
//		this.time_stamp = time_stamp;
//	}
//
//	public String getName() {
//		return name;
//	}
//
//	public void setName(String name) {
//		this.name = name;
//	}
//
//	public long getEvent_id() {
//		return event_id;
//	}
//
//	public void setEvent_id(long event_id) {
//		this.event_id = event_id;
//	}
//
//	public String getPublisher() {
//		return publisher;
//	}
//
//	public void setPublisher(String publisher) {
//		this.publisher = publisher;
//	}
//
//	public long getTime_stamp() {
//		return time_stamp;
//	}
//
//	public void setTime_stamp(long time_stamp) {
//		this.time_stamp = time_stamp;
//	}
//	
//	
//}
